package org.bsu.graphics.raster_algo.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.swing.JPanel;
import org.bsu.graphics.raster_algo.core.Algorithm;

class GridPanel extends JPanel {

    static final int MIN_SCALE = 2;
    static final int MAX_SCALE = 80;

    private static final Color PIXEL = new Color(70, 130, 220);
    private static final Color GRID = new Color(215, 215, 215);
    private static final Color IDEAL = new Color(220, 40, 40);

    private int scale = 20;
    private int offX;
    private int offY;
    private List<Point> pixels = List.of();
    private Algorithm algorithm;
    private int[] params;
    private Point drag;
    private IntConsumer scaleListener = s -> { };
    private Consumer<String> cursorListener = s -> { };

    GridPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(900, 700));
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                drag = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                drag = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (drag != null) {
                    offX += e.getX() - drag.x;
                    offY += e.getY() - drag.y;
                    drag = e.getPoint();
                    repaint();
                }
                reportCursor(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                reportCursor(e);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int delta = Math.max(1, scale / 5);
                setScaleAt(e.getWheelRotation() < 0 ? scale + delta : scale - delta, e.getX(), e.getY());
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addMouseWheelListener(mouse);
    }

    void onScaleChanged(IntConsumer listener) {
        scaleListener = listener;
    }

    void onCursorMoved(Consumer<String> listener) {
        cursorListener = listener;
    }

    int getScale() {
        return scale;
    }

    void setScale(int s) {
        setScaleAt(s, getWidth() / 2, getHeight() / 2);
    }

    void resetView() {
        offX = 0;
        offY = 0;
        repaint();
    }

    void setFigure(Algorithm algorithm, int[] params, Collection<Point> pixels) {
        this.algorithm = algorithm;
        this.params = params.clone();
        this.pixels = List.copyOf(pixels);
        repaint();
    }

    void clear() {
        algorithm = null;
        params = null;
        pixels = List.of();
        repaint();
    }

    private void setScaleAt(int s, int mx, int my) {
        s = Math.max(MIN_SCALE, Math.min(MAX_SCALE, s));
        if (s == scale) {
            return;
        }
        double wx = (mx - originX()) / (double) scale;
        double wy = (originY() - my) / (double) scale;
        scale = s;
        offX = (int) Math.round(mx - wx * s) - getWidth() / 2;
        offY = (int) Math.round(my + wy * s) - getHeight() / 2;
        scaleListener.accept(s);
        repaint();
    }

    private void reportCursor(MouseEvent e) {
        long x = Math.round((e.getX() - originX()) / (double) scale);
        long y = Math.round((originY() - e.getY()) / (double) scale);
        cursorListener.accept("Курсор над пикселем (" + x + ", " + y + ")");
    }

    private int originX() {
        return getWidth() / 2 + offX;
    }

    private int originY() {
        return getHeight() / 2 + offY;
    }

    private int edgeX(int i) {
        return originX() + (int) Math.floor((i - 0.5) * scale);
    }

    private int edgeY(int j) {
        return originY() - (int) Math.floor((j - 0.5) * scale);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();
        int ox = originX();
        int oy = originY();
        int xMin = (int) Math.floor(-ox / (double) scale) - 1;
        int xMax = (int) Math.ceil((w - ox) / (double) scale) + 1;
        int yMin = (int) Math.floor((oy - h) / (double) scale) - 1;
        int yMax = (int) Math.ceil(oy / (double) scale) + 1;

        g2.setColor(PIXEL);
        for (Point p : pixels) {
            if (p.x >= xMin && p.x <= xMax && p.y >= yMin && p.y <= yMax) {
                int left = edgeX(p.x);
                int top = edgeY(p.y + 1);
                g2.fillRect(left, top, edgeX(p.x + 1) - left, edgeY(p.y) - top);
            }
        }

        if (scale >= 5) {
            g2.setColor(GRID);
            for (int i = xMin; i <= xMax + 1; i++) {
                int x = edgeX(i);
                g2.drawLine(x, 0, x, h);
            }
            for (int j = yMin; j <= yMax + 1; j++) {
                int y = edgeY(j);
                g2.drawLine(0, y, w, y);
            }
        }

        g2.setColor(Color.BLACK);
        g2.drawLine(0, oy, w, oy);
        g2.drawLine(ox, 0, ox, h);
        g2.fillPolygon(new int[]{w - 1, w - 11, w - 11}, new int[]{oy, oy - 5, oy + 5}, 3);
        g2.fillPolygon(new int[]{ox, ox - 5, ox + 5}, new int[]{0, 10, 10}, 3);
        g2.drawString("x", w - 14, oy - 8);
        g2.drawString("y", ox + 8, 14);

        FontMetrics fm = g2.getFontMetrics();
        int step = labelStep();
        int labelY = Math.max(fm.getAscent() + 2, Math.min(h - 4, oy + fm.getAscent() + 4));
        for (int x = Math.floorDiv(xMin, step) * step; x <= xMax; x += step) {
            if (x == 0) {
                continue;
            }
            int cx = ox + x * scale;
            g2.drawLine(cx, oy - 3, cx, oy + 3);
            String s = Integer.toString(x);
            g2.drawString(s, cx - fm.stringWidth(s) / 2, labelY);
        }
        for (int y = Math.floorDiv(yMin, step) * step; y <= yMax; y += step) {
            if (y == 0) {
                continue;
            }
            int cy = oy - y * scale;
            g2.drawLine(ox - 3, cy, ox + 3, cy);
            String s = Integer.toString(y);
            int labelX = Math.max(2, Math.min(w - fm.stringWidth(s) - 2, ox + 6));
            g2.drawString(s, labelX, cy + fm.getAscent() / 2 - 1);
        }
        g2.drawString("0", ox + 4, oy + fm.getAscent() + 2);

        if (algorithm != null) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setColor(IDEAL);
            g2.setStroke(new BasicStroke(1.5f));
            if (algorithm.isCircle()) {
                double r = params[2] * (double) scale;
                g2.draw(new Ellipse2D.Double(ox + params[0] * (double) scale - r,
                        oy - params[1] * (double) scale - r, 2 * r, 2 * r));
            } else {
                g2.draw(new Line2D.Double(ox + params[0] * (double) scale, oy - params[1] * (double) scale,
                        ox + params[2] * (double) scale, oy - params[3] * (double) scale));
            }
        }
        g2.dispose();
    }

    private int labelStep() {
        int step = 1;
        int[] factors = {2, 5, 10};
        int base = 1;
        while (step * scale < 40) {
            for (int f : factors) {
                step = base * f;
                if (step * scale >= 40) {
                    break;
                }
            }
            base *= 10;
        }
        return step;
    }
}
