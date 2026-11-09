package org.bsu.graphics.image_processing.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class ImagePanel extends JComponent {

    private static final Color BACKGROUND = new Color(0x2B2B2B);

    private BufferedImage image;
    private final String placeholder;

    public ImagePanel(String placeholder) {
        this.placeholder = placeholder;
        setPreferredSize(new Dimension(420, 320));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && image != null) {
                    showActualSize();
                }
            }
        });
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (image == null) {
            g.setColor(Color.LIGHT_GRAY);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(placeholder, (getWidth() - fm.stringWidth(placeholder)) / 2, getHeight() / 2);
        } else {
            double scale = Math.min((double) getWidth() / image.getWidth(), (double) getHeight() / image.getHeight());
            int w = (int) Math.round(image.getWidth() * scale);
            int h = (int) Math.round(image.getHeight() * scale);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, scale >= 1
                    ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                    : RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
        }
        g.dispose();
    }

    private void showActualSize() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                image.getWidth() + " × " + image.getHeight() + " (1:1)");
        dialog.add(new JScrollPane(new JLabel(new ImageIcon(image))), BorderLayout.CENTER);
        dialog.setSize(Math.min(image.getWidth() + 40, 1400), Math.min(image.getHeight() + 60, 900));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
