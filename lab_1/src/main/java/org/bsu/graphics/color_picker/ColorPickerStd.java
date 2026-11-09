package org.bsu.graphics.color_picker;

import com.bric.colorpicker.ColorPicker;

import javax.swing.*;
import java.awt.*;

public class ColorPickerStd extends JPanel {

    private final JLabel colorBox;
    private final JSpinner cSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JSpinner mSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JSpinner ySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JSpinner kSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));

    private final ColorPicker colorPicker = new ColorPicker(true, true);
    private boolean updating = false;

    public ColorPickerStd() {
        setLayout(new BorderLayout());

        JPanel bottomPanel = new JPanel(new GridLayout(3, 1));

        JLabel label = new JLabel("Текущий цвет", SwingConstants.CENTER);
        colorBox = new JLabel("Цвет не выбран", SwingConstants.CENTER);
        colorBox.setOpaque(true);
        colorBox.setBackground(Color.WHITE);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        colorBox.setPreferredSize(new Dimension(200, 50));

        bottomPanel.add(label);
        bottomPanel.add(colorBox);
        JPanel cmykPanel = new JPanel(new GridLayout(1, 8, 6, 4));
        cmykPanel.add(new JLabel("C%", SwingConstants.RIGHT));
        cmykPanel.add(cSpinner);
        cmykPanel.add(new JLabel("M%", SwingConstants.RIGHT));
        cmykPanel.add(mSpinner);
        cmykPanel.add(new JLabel("Y%", SwingConstants.RIGHT));
        cmykPanel.add(ySpinner);
        cmykPanel.add(new JLabel("K%", SwingConstants.RIGHT));
        cmykPanel.add(kSpinner);
        bottomPanel.add(cmykPanel);

        cSpinner.addChangeListener(e -> onCmykInput());
        mSpinner.addChangeListener(e -> onCmykInput());
        ySpinner.addChangeListener(e -> onCmykInput());
        kSpinner.addChangeListener(e -> onCmykInput());

        colorPicker.addColorListener(colorModel -> {
            Color c = colorModel.getColor();
            colorBox.setText("Выбран цвет: R - %d, G - %d, B - %d".formatted(
                    c.getRed(), c.getGreen(), c.getBlue()
            ));
            colorBox.setBackground(c);
            if (!updating) {
                updating = true;
                setCmykSpinners(c);
                updating = false;
            }
        });

        updating = true;
        setCmykSpinners(colorPicker.getColor());
        updating = false;

        add(colorPicker, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void onCmykInput() {
        if (updating) {
            return;
        }
        updating = true;
        colorPicker.setColor(fromCmykSpinners());
        updating = false;
    }

    private static int[] toCmyk(Color c) {
        double r = c.getRed() / 255.0;
        double g = c.getGreen() / 255.0;
        double b = c.getBlue() / 255.0;
        double k = 1 - Math.max(r, Math.max(g, b));
        double cy = 0, m = 0, y = 0;
        if (k < 1) {
            cy = (1 - r - k) / (1 - k);
            m = (1 - g - k) / (1 - k);
            y = (1 - b - k) / (1 - k);
        }
        return new int[]{
                (int) Math.round(cy * 100), (int) Math.round(m * 100),
                (int) Math.round(y * 100), (int) Math.round(k * 100)};
    }

    private Color fromCmykSpinners() {
        double c = (Integer) cSpinner.getValue() / 100.0;
        double m = (Integer) mSpinner.getValue() / 100.0;
        double y = (Integer) ySpinner.getValue() / 100.0;
        double k = (Integer) kSpinner.getValue() / 100.0;
        return new Color(
                (int) Math.round(255 * (1 - c) * (1 - k)),
                (int) Math.round(255 * (1 - m) * (1 - k)),
                (int) Math.round(255 * (1 - y) * (1 - k)));
    }

    private void setCmykSpinners(Color color) {
        int[] cmyk = toCmyk(color);
        cSpinner.setValue(cmyk[0]);
        mSpinner.setValue(cmyk[1]);
        ySpinner.setValue(cmyk[2]);
        kSpinner.setValue(cmyk[3]);
    }
}
