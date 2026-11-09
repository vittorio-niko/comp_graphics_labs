package org.bsu.graphics.image_processing.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.bsu.graphics.image_processing.core.ImageUtils;
import org.bsu.graphics.image_processing.core.Noise;
import org.bsu.graphics.image_processing.core.TestImageGenerator;

public class MainFrame extends JFrame implements ImageHost {

    private final ImageView sourceView = new ImageView("Исходное изображение",
            "Файл → Открыть… (Ctrl+O)", true);
    private final ImageView resultView = new ImageView("Результат", "Выберите метод справа", true);
    private final JLabel status = new JLabel(" ");

    private BufferedImage loaded;
    private BufferedImage source;
    private BufferedImage result;
    private File currentDir = defaultDirectory();
    private long noiseSeed = 1;

    public MainFrame() {
        super("Обработка изображений — гистограммы, контраст, сегментация");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setJMenuBar(createMenu());

        JSplitPane images = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sourceView, resultView);
        images.setResizeWeight(0.5);
        images.setContinuousLayout(true);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Гистограмма и контраст", WidthTrackingPanel.scroll(new ContrastPanel(this)));
        tabs.addTab("Сегментация", WidthTrackingPanel.scroll(new SegmentationPanel(this)));
        tabs.setPreferredSize(new Dimension(400, 600));

        JSplitPane main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, images, tabs);
        main.setResizeWeight(1.0);
        add(main, BorderLayout.CENTER);

        status.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        add(status, BorderLayout.SOUTH);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1500, screen.width - 40), Math.min(900, screen.height - 60));
        setLocationRelativeTo(null);
    }

    private JMenuBar createMenu() {
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("Файл");
        file.add(item("Открыть…", KeyEvent.VK_O, this::openImage));
        file.add(item("Сохранить результат…", KeyEvent.VK_S, this::saveResult));
        file.addSeparator();
        file.add(item("Сгенерировать тестовые изображения…", 0, this::generateTestImages));
        file.addSeparator();
        file.add(item("Выход", KeyEvent.VK_Q, this::dispose));
        bar.add(file);

        JMenu image = new JMenu("Изображение");
        image.add(item("Результат → исходное", KeyEvent.VK_R, this::resultToSource));
        image.add(item("Вернуть загруженное изображение", KeyEvent.VK_Z, this::resetSource));
        image.addSeparator();
        image.add(item("Перевести в оттенки серого", 0,
                () -> modifySource(ImageUtils::toGray, "Переведено в оттенки серого")));
        image.add(item("Добавить гауссов шум…", 0, this::addGaussianNoise));
        image.add(item("Добавить шум «соль-перец»…", 0, this::addSaltAndPepper));
        bar.add(image);
        return bar;
    }

    private static JMenuItem item(String title, int key, Runnable action) {
        JMenuItem item = new JMenuItem(title);
        if (key != 0) {
            item.setAccelerator(KeyStroke.getKeyStroke(key, InputEvent.CTRL_DOWN_MASK));
        }
        item.addActionListener(e -> action.run());
        return item;
    }

    @Override
    public BufferedImage currentSource() {
        return source;
    }

    @Override
    public BufferedImage requireSource() {
        if (source == null) {
            JOptionPane.showMessageDialog(this, "Сначала откройте изображение (Файл → Открыть…).",
                    "Нет изображения", JOptionPane.INFORMATION_MESSAGE);
        }
        return source;
    }

    @Override
    public void showResult(BufferedImage image, String caption) {
        result = image;
        resultView.setImage(image);
        resultView.setCaption("Результат: " + caption);
    }

    @Override
    public void setStatus(String text) {
        status.setText(text);
    }

    @Override
    public void showComparison(String title, List<NamedImage> images, int columns, boolean withHistograms) {
        new CompareDialog(this, title, images, columns, withHistograms).setVisible(true);
    }

    private void openImage() {
        JFileChooser chooser = new JFileChooser(currentDir);
        chooser.setFileFilter(new FileNameExtensionFilter("Изображения (png, jpg, bmp, gif, tif)",
                "png", "jpg", "jpeg", "bmp", "gif", "tif", "tiff"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File f = chooser.getSelectedFile();
        currentDir = f.getParentFile();
        try {
            BufferedImage img = ImageIO.read(f);
            if (img == null) {
                throw new IOException("формат не поддерживается");
            }
            loaded = ImageUtils.toRgb(img);
            setSource(loaded, f.getName());
            showResult(null, "");
            resultView.setCaption("Результат");
            setStatus(String.format(Locale.US, "Открыто: %s (%d × %d, %s)", f.getPath(), img.getWidth(),
                    img.getHeight(), ImageUtils.isGray(loaded) ? "полутоновое" : "цветное"));
        } catch (IOException ex) {
            error("Не удалось открыть " + f.getName() + ": " + ex.getMessage());
        }
    }

    private void saveResult() {
        if (result == null) {
            error("Нет результата для сохранения.");
            return;
        }
        JFileChooser chooser = new JFileChooser(currentDir);
        chooser.setFileFilter(new FileNameExtensionFilter("PNG", "png"));
        chooser.setSelectedFile(new File(currentDir, "result.png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File f = chooser.getSelectedFile();
        if (!f.getName().toLowerCase(Locale.ROOT).matches(".*\\.(png|jpg|jpeg|bmp)$")) {
            f = new File(f.getPath() + ".png");
        }
        String format = f.getName().substring(f.getName().lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        try {
            ImageIO.write(result, format.equals("jpeg") ? "jpg" : format, f);
            setStatus("Сохранено: " + f.getPath());
        } catch (IOException ex) {
            error("Не удалось сохранить: " + ex.getMessage());
        }
    }

    private void generateTestImages() {
        JFileChooser chooser = new JFileChooser(currentDir);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Каталог для тестовых изображений");
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            List<File> files = TestImageGenerator.generateAll(chooser.getSelectedFile());
            currentDir = chooser.getSelectedFile();
            setStatus("Создано изображений: " + files.size() + " в " + currentDir.getPath());
        } catch (IOException ex) {
            error("Ошибка генерации: " + ex.getMessage());
        }
    }

    private void resultToSource() {
        if (result == null) {
            error("Нет результата.");
            return;
        }
        setSource(result, "результат предыдущей обработки");
    }

    private void resetSource() {
        if (loaded != null) {
            setSource(loaded, "загруженное изображение");
        }
    }

    private void addGaussianNoise() {
        String s = JOptionPane.showInputDialog(this, "СКО шума σ (0–100):", "20");
        Double sigma = parse(s, 0, 100);
        if (sigma != null) {
            modifySource(img -> Noise.gaussian(img, sigma, noiseSeed++),
                    String.format(Locale.US, "Добавлен гауссов шум, σ = %.1f", sigma));
        }
    }

    private void addSaltAndPepper() {
        String s = JOptionPane.showInputDialog(this, "Доля искажённых пикселей, % (0–50):", "2");
        Double pct = parse(s, 0, 50);
        if (pct != null) {
            modifySource(img -> Noise.saltAndPepper(img, pct / 100.0, noiseSeed++),
                    String.format(Locale.US, "Добавлен шум «соль-перец», %.1f%%", pct));
        }
    }

    private void modifySource(UnaryOperator<BufferedImage> op, String message) {
        BufferedImage src = requireSource();
        if (src != null) {
            setSource(op.apply(src), message.toLowerCase(Locale.ROOT));
            setStatus(message);
        }
    }

    private void setSource(BufferedImage img, String description) {
        source = img;
        sourceView.setImage(img);
        sourceView.setCaption("Исходное: " + description);
    }

    private Double parse(String s, double min, double max) {
        if (s == null) {
            return null;
        }
        try {
            double v = Double.parseDouble(s.trim().replace(',', '.'));
            if (v >= min && v <= max) {
                return v;
            }
        } catch (NumberFormatException ignored) {
        }
        error("Введите число от " + min + " до " + max + ".");
        return null;
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private static File defaultDirectory() {
        File images = new File("images");
        return images.isDirectory() ? images.getAbsoluteFile() : new File(System.getProperty("user.dir"));
    }
}
