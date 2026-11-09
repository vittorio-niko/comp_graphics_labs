package org.bsu.graphics.image_processing.ui;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Locale;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.bsu.graphics.image_processing.core.ContrastOps;
import org.bsu.graphics.image_processing.core.ContrastOps.EqualizationMode;
import org.bsu.graphics.image_processing.core.ContrastOps.LinearMode;
import org.bsu.graphics.image_processing.core.ContrastOps.StretchResult;

class ContrastPanel extends JPanel {

    private final ImageHost host;
    private final JSpinner clipSpinner = new JSpinner(new SpinnerNumberModel(0.5, 0.0, 20.0, 0.5));
    private final JComboBox<LinearMode> linearModeBox = new JComboBox<>(LinearMode.values());
    private final JComboBox<EqualizationMode> eqModeBox = new JComboBox<>(EqualizationMode.values());

    ContrastPanel(ImageHost host) {
        super(new BorderLayout());
        this.host = host;

        Form linear = new Form("Линейное контрастирование");
        linear.row("Отсечение, % с каждой стороны:", clipSpinner);
        linear.row("Каналы:", linearModeBox);
        JButton applyLinear = new JButton("Применить линейное контрастирование");
        applyLinear.addActionListener(e -> applyLinear());
        linear.full(applyLinear);

        Form eq = new Form("Эквализация гистограммы");
        eq.row("Способ:", eqModeBox);
        JButton applyEq = new JButton("Применить эквализацию");
        applyEq.addActionListener(e -> applyEqualization());
        eq.full(applyEq);

        Form compare = new Form("Сравнение");
        JButton compareButton = new JButton("Сравнить все методы…");
        compareButton.addActionListener(e -> compareAll());
        compare.full(compareButton);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(linear);
        top.add(eq);
        top.add(compare);
        add(top, BorderLayout.NORTH);
    }

    private double clip() {
        return ((Number) clipSpinner.getValue()).doubleValue();
    }

    private void applyLinear() {
        BufferedImage src = host.requireSource();
        if (src == null) {
            return;
        }
        LinearMode mode = (LinearMode) linearModeBox.getSelectedItem();
        StretchResult r = ContrastOps.linearStretch(src, clip(), mode);
        host.showResult(r.image(), String.format(Locale.US, "Линейное контрастирование (%.1f%%, %s)",
                clip(), mode.toString().toLowerCase(Locale.ROOT)));
        host.setStatus("Линейное контрастирование: " + describeBounds(r));
    }

    private void applyEqualization() {
        BufferedImage src = host.requireSource();
        if (src == null) {
            return;
        }
        EqualizationMode mode = (EqualizationMode) eqModeBox.getSelectedItem();
        host.showResult(ContrastOps.equalize(src, mode), "Эквализация: " + mode);
        host.setStatus("Эквализация гистограммы выполнена: " + mode);
    }

    private void compareAll() {
        BufferedImage src = host.requireSource();
        if (src == null) {
            return;
        }
        String clip = String.format(Locale.US, "%.1f%%", clip());
        host.showComparison("Сравнение методов повышения контраста", List.of(
                new ImageHost.NamedImage("Исходное", src),
                new ImageHost.NamedImage("Линейное, общий диапазон (" + clip + ")",
                        ContrastOps.linearStretch(src, clip(), LinearMode.COMMON).image()),
                new ImageHost.NamedImage("Линейное, по каналам (" + clip + ")",
                        ContrastOps.linearStretch(src, clip(), LinearMode.PER_CHANNEL).image()),
                new ImageHost.NamedImage("Эквализация: полутоновая (Y)", ContrastOps.equalizeGray(src)),
                new ImageHost.NamedImage("Эквализация: каналы R, G, B", ContrastOps.equalizeRgb(src)),
                new ImageHost.NamedImage("Эквализация: V в HSV", ContrastOps.equalizeHsv(src))
        ), 3, true);
    }

    private static String describeBounds(StretchResult r) {
        if (r.lo()[0] == r.lo()[1] && r.lo()[1] == r.lo()[2] && r.hi()[0] == r.hi()[1] && r.hi()[1] == r.hi()[2]) {
            return String.format("[%d; %d] → [0; 255]", r.lo()[0], r.hi()[0]);
        }
        return String.format("R [%d; %d], G [%d; %d], B [%d; %d] → [0; 255]",
                r.lo()[0], r.hi()[0], r.lo()[1], r.hi()[1], r.lo()[2], r.hi()[2]);
    }
}
