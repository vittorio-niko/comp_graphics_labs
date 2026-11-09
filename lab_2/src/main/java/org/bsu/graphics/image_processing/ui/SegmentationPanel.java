package org.bsu.graphics.image_processing.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import org.bsu.graphics.image_processing.core.Segmentation;
import org.bsu.graphics.image_processing.core.Segmentation.Detection;
import org.bsu.graphics.image_processing.core.Segmentation.LineDirection;

class SegmentationPanel extends JPanel {

    private enum Method {
        POINTS("Обнаружение точек", 90),
        LINES("Обнаружение линий", 60),
        EDGES("Обнаружение перепадов яркости (Собель)", 25);

        private final String title;
        private final int defaultThreshold;

        Method(String title, int defaultThreshold) {
            this.title = title;
            this.defaultThreshold = defaultThreshold;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private enum Output {
        BINARY("Бинарное"),
        OVERLAY("Наложение на исходное");

        private final String title;

        Output(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private final ImageHost host;
    private final JComboBox<Method> methodBox = new JComboBox<>(Method.values());
    private final JComboBox<LineDirection> directionBox = new JComboBox<>(LineDirection.values());
    private final JSlider thresholdSlider = new JSlider(0, 100, Method.POINTS.defaultThreshold);
    private final JLabel thresholdLabel = new JLabel();
    private final JComboBox<Output> outputBox = new JComboBox<>(Output.values());
    private final JLabel directionLabel;

    private BufferedImage cachedSource;
    private String cachedKey;
    private Detection cachedDetection;
    private boolean adjusting;

    SegmentationPanel(ImageHost host) {
        super(new BorderLayout());
        this.host = host;

        Form form = new Form("Параметры");
        form.row("Метод:", methodBox);
        directionLabel = form.row("Направление:", directionBox);
        thresholdSlider.setMajorTickSpacing(25);
        thresholdSlider.setPaintTicks(true);
        form.row("Порог T, % от max:", thresholdSlider);
        form.full(thresholdLabel);
        form.row("Вывод:", outputBox);
        JButton apply = new JButton("Применить");
        apply.addActionListener(e -> apply());
        form.full(apply);
        add(form, BorderLayout.NORTH);

        methodBox.addActionListener(e -> {
            adjusting = true;
            thresholdSlider.setValue(selectedMethod().defaultThreshold);
            adjusting = false;
            updateControls();
            parametersChanged();
        });
        directionBox.addActionListener(e -> parametersChanged());
        outputBox.addActionListener(e -> parametersChanged());
        thresholdSlider.addChangeListener(e -> {
            updateThresholdLabel();
            if (!thresholdSlider.getValueIsAdjusting()) {
                parametersChanged();
            }
        });
        updateControls();
    }

    private Method selectedMethod() {
        return (Method) methodBox.getSelectedItem();
    }

    private void parametersChanged() {
        if (!adjusting && host.currentSource() != null) {
            apply();
        }
    }

    private void updateControls() {
        boolean lines = selectedMethod() == Method.LINES;
        directionLabel.setVisible(lines);
        directionBox.setVisible(lines);
        updateThresholdLabel();
        revalidate();
    }

    private void updateThresholdLabel() {
        String text = "T = " + thresholdSlider.getValue() + "% от максимального отклика";
        if (cachedDetection != null) {
            text += String.format(Locale.US, " (≈ %.1f)", cachedDetection.threshold(thresholdSlider.getValue()));
        }
        thresholdLabel.setText(text);
    }

    private Detection detect(BufferedImage src) {
        Method m = selectedMethod();
        LineDirection dir = (LineDirection) directionBox.getSelectedItem();
        String key = m + "|" + dir;
        if (src != cachedSource || !Objects.equals(key, cachedKey)) {
            cachedDetection = switch (m) {
                case POINTS -> Segmentation.points(src);
                case LINES -> Segmentation.lines(src, dir);
                case EDGES -> Segmentation.edges(src);
            };
            cachedSource = src;
            cachedKey = key;
        }
        return cachedDetection;
    }

    private void apply() {
        BufferedImage src = host.requireSource();
        if (src == null) {
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            Detection d = detect(src);
            int pct = thresholdSlider.getValue();
            boolean[] mask = d.mask(pct);
            Output out = (Output) outputBox.getSelectedItem();
            BufferedImage result = out == Output.BINARY
                    ? Segmentation.renderMask(mask, d.width(), d.height())
                    : Segmentation.renderOverlay(src, mask, selectedMethod() == Method.POINTS);
            host.showResult(result, caption() + " — " + out);
            updateThresholdLabel();
            host.setStatus(String.format(Locale.US, "%s: max = %.1f, T = %.1f, найдено пикселей: %d",
                    caption(), d.max(), d.threshold(pct), Segmentation.count(mask)));
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private String caption() {
        return switch (selectedMethod()) {
            case POINTS -> "Точки";
            case LINES -> "Линии: " + directionBox.getSelectedItem();
            case EDGES -> "Перепады (Собель)";
        };
    }
}
