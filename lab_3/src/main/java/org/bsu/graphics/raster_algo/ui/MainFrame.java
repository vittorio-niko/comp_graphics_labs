package org.bsu.graphics.raster_algo.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import org.bsu.graphics.raster_algo.core.Algorithm;

public class MainFrame extends JFrame {

    private static final int LIMIT = 1000;

    private final GridPanel grid = new GridPanel();
    private final JComboBox<Algorithm> algorithm = new JComboBox<>(Algorithm.values());
    private final JSpinner x1 = spinner(-8, -LIMIT);
    private final JSpinner y1 = spinner(-3, -LIMIT);
    private final JSpinner x2 = spinner(12, -LIMIT);
    private final JSpinner y2 = spinner(6, -LIMIT);
    private final JSpinner xc = spinner(0, -LIMIT);
    private final JSpinner yc = spinner(0, -LIMIT);
    private final JSpinner radius = spinner(10, 0);
    private final JSpinner scale = new JSpinner(new SpinnerNumberModel(grid.getScale(),
            GridPanel.MIN_SCALE, GridPanel.MAX_SCALE, 1));
    private final JLabel pixelCount = new JLabel("—");
    private final JLabel time = new JLabel("—");
    private final JLabel status = new JLabel(" ");
    private final List<JComponent> lineRows = new ArrayList<>();
    private final List<JComponent> circleRows = new ArrayList<>();

    public MainFrame() {
        super("Базовые растровые алгоритмы");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Form params = new Form("Параметры");
        params.row("Алгоритм", algorithm);
        addRow(params, lineRows, "x1", x1);
        addRow(params, lineRows, "y1", y1);
        addRow(params, lineRows, "x2", x2);
        addRow(params, lineRows, "y2", y2);
        addRow(params, circleRows, "xc", xc);
        addRow(params, circleRows, "yc", yc);
        addRow(params, circleRows, "R", radius);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton build = new JButton("Построить");
        JButton clear = new JButton("Очистить");
        buttons.add(build);
        buttons.add(clear);
        params.full(buttons);

        Form view = new Form("Вид");
        view.row("Масштаб, px", scale);
        JButton reset = new JButton("Начало координат в центр");
        view.full(reset);

        Form result = new Form("Результат");
        result.row("Пикселей", pixelCount);
        result.row("Время", time);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(params);
        top.add(view);
        top.add(result);

        JPanel side = new JPanel(new BorderLayout());
        side.add(top, BorderLayout.NORTH);
        side.setPreferredSize(new Dimension(430, 700));

        JSplitPane main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, grid, side);
        main.setResizeWeight(1.0);
        add(main, BorderLayout.CENTER);

        status.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        add(status, BorderLayout.SOUTH);

        algorithm.addActionListener(e -> updateRows());
        build.addActionListener(e -> build());
        clear.addActionListener(e -> clear());
        reset.addActionListener(e -> grid.resetView());
        scale.addChangeListener(e -> grid.setScale((Integer) scale.getValue()));
        grid.onScaleChanged(scale::setValue);
        grid.onCursorMoved(status::setText);
        updateRows();

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1400, screen.width - 40), Math.min(900, screen.height - 60));
        setLocationRelativeTo(null);
    }

    private static JSpinner spinner(int value, int min) {
        return new JSpinner(new SpinnerNumberModel(value, min, LIMIT, 1));
    }

    private static void addRow(Form form, List<JComponent> group, String label, JComponent component) {
        group.add(form.row(label, component));
        group.add(component);
    }

    private void updateRows() {
        boolean circle = selected().isCircle();
        lineRows.forEach(c -> c.setVisible(!circle));
        circleRows.forEach(c -> c.setVisible(circle));
        revalidate();
    }

    private Algorithm selected() {
        return (Algorithm) algorithm.getSelectedItem();
    }

    private void build() {
        Algorithm a = selected();
        int[] p = a.isCircle()
                ? new int[]{value(xc), value(yc), value(radius)}
                : new int[]{value(x1), value(y1), value(x2), value(y2)};
        Set<Point> pixels = new LinkedHashSet<>();
        a.run(p, (x, y) -> pixels.add(new Point(x, y)));
        grid.setFigure(a, p, pixels);
        pixelCount.setText(Integer.toString(pixels.size()));

        long[] sum = {0};
        long start = System.nanoTime();
        a.run(p, (x, y) -> sum[0] += x + y);
        long elapsed = System.nanoTime() - start;
        time.setText(String.format(Locale.US, "%.3f мкс", elapsed / 1000.0));
        status.setText(a + ": построено");
    }

    private void clear() {
        grid.clear();
        pixelCount.setText("—");
        time.setText("—");
    }

    private static int value(JSpinner s) {
        return (Integer) s.getValue();
    }
}
