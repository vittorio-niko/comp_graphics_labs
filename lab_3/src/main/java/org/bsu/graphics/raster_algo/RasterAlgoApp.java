package org.bsu.graphics.raster_algo;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.bsu.graphics.raster_algo.ui.MainFrame;

public class RasterAlgoApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            new MainFrame().setVisible(true);
        });
    }
}
