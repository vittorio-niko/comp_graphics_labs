package org.bsu.graphics.image_processing.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ImageView extends JPanel {

    private final JLabel title = new JLabel();
    private final ImagePanel imagePanel;
    private final HistogramPanel histogramPanel;
    private BufferedImage image;

    public ImageView(String caption, String placeholder, boolean withHistogram) {
        super(new BorderLayout(0, 4));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        setCaption(caption);
        imagePanel = new ImagePanel(placeholder);
        histogramPanel = withHistogram ? new HistogramPanel() : null;
        add(title, BorderLayout.NORTH);
        add(imagePanel, BorderLayout.CENTER);
        if (histogramPanel != null) {
            add(histogramPanel, BorderLayout.SOUTH);
        }
    }

    public void setCaption(String caption) {
        title.setText(caption);
        title.setToolTipText(caption);
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        imagePanel.setImage(image);
        if (histogramPanel != null) {
            histogramPanel.setImage(image);
        }
    }

    public BufferedImage getImage() {
        return image;
    }
}
