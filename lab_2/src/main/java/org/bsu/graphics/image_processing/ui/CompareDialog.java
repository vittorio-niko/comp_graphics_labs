package org.bsu.graphics.image_processing.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

class CompareDialog extends JDialog {

    CompareDialog(Window owner, String title, List<ImageHost.NamedImage> images, int columns,
                  boolean withHistograms) {
        super(owner, title, ModalityType.MODELESS);
        int rows = (images.size() + columns - 1) / columns;
        JPanel grid = new JPanel(new GridLayout(rows, columns, 6, 6));
        for (ImageHost.NamedImage item : images) {
            ImageView view = new ImageView(item.name(), "", withHistograms);
            view.setImage(item.image());
            view.setPreferredSize(new Dimension(380, withHistograms ? 460 : 300));
            grid.add(view);
        }
        add(new JScrollPane(grid));
        pack();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(getWidth(), screen.width - 60), Math.min(getHeight(), screen.height - 80));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
}
