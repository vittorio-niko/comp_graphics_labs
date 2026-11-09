package org.bsu.graphics.image_processing;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.bsu.graphics.image_processing.ui.MainFrame;

public class ImageProcessingApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            new MainFrame().setVisible(true);
        });
    }
}
