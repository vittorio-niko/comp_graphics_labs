package org.bsu.graphics.color_picker;

import javax.swing.*;
import java.awt.*;

// Color Picker (lab #1/1)
// Includes standard library (ColorPickerStd) and
// custom (ColorPickerCustom) solutions
public class ColorPickerApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Color Picker App");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            JPanel pickersPanel = new JPanel(new GridLayout(1, 2, 10, 0));

            ColorPickerCustom customPicker = new ColorPickerCustom();
            ColorPickerStd standardPicker = new ColorPickerStd();

            pickersPanel.add(customPicker);
            pickersPanel.add(standardPicker);

            frame.add(pickersPanel, BorderLayout.CENTER);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
