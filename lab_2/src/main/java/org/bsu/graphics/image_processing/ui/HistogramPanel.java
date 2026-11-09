package org.bsu.graphics.image_processing.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Locale;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.bsu.graphics.image_processing.core.Histogram;

public class HistogramPanel extends JPanel {

    private enum Mode {
        LUMA("Яркость Y"), RGB("Каналы R, G, B");

        private final String title;

        Mode(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private final JComboBox<Mode> modeBox = new JComboBox<>(Mode.values());
    private final JCheckBox cdfBox = new JCheckBox("CDF");
    private final JLabel stats = new JLabel(" ");
    private final Canvas canvas = new Canvas();
    private Histogram histogram;

    public HistogramPanel() {
        super(new BorderLayout());
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        controls.add(new JLabel("Гистограмма:"));
        controls.add(modeBox);
        controls.add(cdfBox);
        modeBox.addActionListener(e -> canvas.repaint());
        cdfBox.addActionListener(e -> canvas.repaint());
        add(controls, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
        add(stats, BorderLayout.SOUTH);
    }

    public void setImage(BufferedImage image) {
        histogram = image == null ? null : Histogram.of(image);
        if (histogram == null) {
            stats.setText(" ");
        } else {
            int[] y = histogram.channel(Histogram.Channel.LUMA);
            stats.setText(String.format(Locale.US, " Y: min %d, max %d, среднее %.1f, σ %.1f",
                    Histogram.min(y), Histogram.max(y), Histogram.mean(y), Histogram.std(y)));
        }
        canvas.repaint();
    }

    private class Canvas extends JComponent {

        private static final int PAD_X = 8;
        private static final int PAD_TOP = 6;
        private static final int PAD_BOTTOM = 16;

        Canvas() {
            setPreferredSize(new Dimension(300, 130));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());

            int x0 = PAD_X;
            int plotW = getWidth() - 2 * PAD_X;
            int plotH = getHeight() - PAD_TOP - PAD_BOTTOM;
            int base = PAD_TOP + plotH;

            g.setColor(Color.GRAY);
            g.drawLine(x0, base, x0 + plotW, base);
            for (int v : new int[]{0, 64, 128, 192, 255}) {
                int x = x0 + v * plotW / 255;
                g.drawLine(x, base, x, base + 3);
                g.drawString(String.valueOf(v), Math.min(x - 6, getWidth() - 22), base + 14);
            }
            if (histogram == null || plotW <= 0 || plotH <= 0) {
                g.dispose();
                return;
            }

            if (modeBox.getSelectedItem() == Mode.LUMA) {
                int[] h = histogram.channel(Histogram.Channel.LUMA);
                double max = maxOf(h);
                g.setColor(new Color(70, 70, 70));
                for (int i = 0; i < h.length; i++) {
                    int xa = x0 + i * plotW / 256;
                    int xb = x0 + (i + 1) * plotW / 256;
                    int bh = (int) Math.round(h[i] / max * plotH);
                    g.fillRect(xa, base - bh, Math.max(1, xb - xa), bh);
                }
            } else {
                int[] r = histogram.channel(Histogram.Channel.RED);
                int[] gr = histogram.channel(Histogram.Channel.GREEN);
                int[] b = histogram.channel(Histogram.Channel.BLUE);
                double max = Math.max(maxOf(r), Math.max(maxOf(gr), maxOf(b)));
                drawCurve(g, r, max, new Color(220, 30, 30, 200), x0, plotW, plotH, base);
                drawCurve(g, gr, max, new Color(20, 160, 20, 200), x0, plotW, plotH, base);
                drawCurve(g, b, max, new Color(30, 60, 230, 200), x0, plotW, plotH, base);
            }

            if (cdfBox.isSelected()) {
                long[] cdf = Histogram.cumulative(histogram.channel(Histogram.Channel.LUMA));
                double total = histogram.total();
                Path2D path = new Path2D.Double();
                for (int i = 0; i < cdf.length; i++) {
                    double x = x0 + (i + 0.5) * plotW / 256.0;
                    double y = base - cdf[i] / total * plotH;
                    if (i == 0) {
                        path.moveTo(x, y);
                    } else {
                        path.lineTo(x, y);
                    }
                }
                g.setColor(new Color(230, 120, 0));
                g.setStroke(new BasicStroke(1.8f));
                g.draw(path);
            }
            g.dispose();
        }

        private void drawCurve(Graphics2D g, int[] h, double max, Color color, int x0, int plotW, int plotH,
                               int base) {
            Path2D path = new Path2D.Double();
            for (int i = 0; i < h.length; i++) {
                double x = x0 + (i + 0.5) * plotW / 256.0;
                double y = base - h[i] / max * plotH;
                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }
            g.setColor(color);
            g.setStroke(new BasicStroke(1.3f));
            g.draw(path);
        }

        private double maxOf(int[] h) {
            int m = 1;
            for (int v : h) {
                m = Math.max(m, v);
            }
            return m;
        }
    }
}
