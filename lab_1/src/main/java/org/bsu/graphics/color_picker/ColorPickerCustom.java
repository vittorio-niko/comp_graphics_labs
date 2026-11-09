package org.bsu.graphics.color_picker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

public class ColorPickerCustom extends JPanel {

    private static final int PADDING = 10;
    private static final double INNER_RATIO = 0.7;
    private static final int MARKER_SIZE = 10;

    private float hue = 0f;
    private float saturation = 1f;
    private float brightness = 1f;

    private boolean draggingHue = false;
    private boolean draggingSb = false;

    private final JPanel bottomPanel = new JPanel(new BorderLayout());

    private final JSpinner hSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 359, 1));
    private final JSpinner sSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JSpinner vSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JSpinner rSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
    private final JSpinner gSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
    private final JSpinner bSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
    private final JSpinner cSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JSpinner mSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JSpinner ySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JSpinner kSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));

    private boolean updating = false;

    public ColorPickerCustom() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400, 500));
        setBackground(Color.WHITE);

        JPanel inputs = new JPanel(new GridLayout(3, 8, 6, 4));
        inputs.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        inputs.add(new JLabel("H°", SwingConstants.RIGHT));
        inputs.add(hSpinner);
        inputs.add(new JLabel("S%", SwingConstants.RIGHT));
        inputs.add(sSpinner);
        inputs.add(new JLabel("V%", SwingConstants.RIGHT));
        inputs.add(vSpinner);
        inputs.add(new JLabel());
        inputs.add(new JLabel());
        inputs.add(new JLabel("R", SwingConstants.RIGHT));
        inputs.add(rSpinner);
        inputs.add(new JLabel("G", SwingConstants.RIGHT));
        inputs.add(gSpinner);
        inputs.add(new JLabel("B", SwingConstants.RIGHT));
        inputs.add(bSpinner);
        inputs.add(new JLabel());
        inputs.add(new JLabel());
        inputs.add(new JLabel("C%", SwingConstants.RIGHT));
        inputs.add(cSpinner);
        inputs.add(new JLabel("M%", SwingConstants.RIGHT));
        inputs.add(mSpinner);
        inputs.add(new JLabel("Y%", SwingConstants.RIGHT));
        inputs.add(ySpinner);
        inputs.add(new JLabel("K%", SwingConstants.RIGHT));
        inputs.add(kSpinner);

        bottomPanel.add(inputs, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        hSpinner.addChangeListener(e -> onHsvInput());
        sSpinner.addChangeListener(e -> onHsvInput());
        vSpinner.addChangeListener(e -> onHsvInput());
        rSpinner.addChangeListener(e -> onRgbInput());
        gSpinner.addChangeListener(e -> onRgbInput());
        bSpinner.addChangeListener(e -> onRgbInput());
        cSpinner.addChangeListener(e -> onCmykInput());
        mSpinner.addChangeListener(e -> onCmykInput());
        ySpinner.addChangeListener(e -> onCmykInput());
        kSpinner.addChangeListener(e -> onCmykInput());

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                double dist = Math.hypot(e.getX() - centerX(), e.getY() - centerY());
                draggingHue = dist >= innerRadius() && dist <= outerRadius();
                draggingSb = dist < innerRadius();
                onMouse(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                onMouse(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggingHue = false;
                draggingSb = false;
            }
        };
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        updateControls(true);
    }

    private int centerX() {
        return getWidth() / 2;
    }

    private int centerY() {
        return (getHeight() - bottomPanel.getHeight()) / 2;
    }

    private int outerRadius() {
        int areaHeight = getHeight() - bottomPanel.getHeight();
        return Math.max(Math.min(getWidth(), areaHeight) / 2 - PADDING, 1);
    }

    private int innerRadius() {
        return (int) (outerRadius() * INNER_RATIO);
    }

    private int squareHalf() {
        return (int) (innerRadius() / Math.sqrt(2));
    }

    private static float clamp(double v) {
        return (float) Math.max(0.0, Math.min(1.0, v));
    }

    private void onMouse(MouseEvent e) {
        double dx = e.getX() - centerX();
        double dy = e.getY() - centerY();

        if (draggingHue && (dx != 0 || dy != 0)) {
            double angle = Math.toDegrees(Math.atan2(-dy, dx));
            if (angle < 0) {
                angle += 360.0;
            }
            hue = (float) (angle / 360.0);
            if (hue >= 1f) {
                hue = 0f;
            }
        } else if (draggingSb) {
            int half = squareHalf();
            saturation = clamp((dx + half) / (half * 2.0));
            brightness = clamp(1.0 - (dy + half) / (half * 2.0));
        } else {
            return;
        }

        updateControls(true);
        repaint();
    }

    private void onHsvInput() {
        if (updating) {
            return;
        }
        hue = (Integer) hSpinner.getValue() / 360f;
        saturation = (Integer) sSpinner.getValue() / 100f;
        brightness = (Integer) vSpinner.getValue() / 100f;
        updateControls(true);
        repaint();
    }

    private void onRgbInput() {
        if (updating) {
            return;
        }
        setFromRgb(new Color(
                (Integer) rSpinner.getValue(),
                (Integer) gSpinner.getValue(),
                (Integer) bSpinner.getValue()));
        updateControls(true);
        repaint();
    }

    private void onCmykInput() {
        if (updating) {
            return;
        }
        setFromRgb(fromCmykSpinners());
        updateControls(false);
        repaint();
    }

    private void setFromRgb(Color c) {
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        if (hsb[1] > 0f && hsb[2] > 0f) {
            hue = hsb[0];
        }
        saturation = hsb[1];
        brightness = hsb[2];
    }

    private void updateControls(boolean updateCmyk) {
        Color c = Color.getHSBColor(hue, saturation, brightness);
        int h = Math.round(hue * 360) % 360;
        int s = Math.round(saturation * 100);
        int v = Math.round(brightness * 100);

        updating = true;
        hSpinner.setValue(h);
        sSpinner.setValue(s);
        vSpinner.setValue(v);
        rSpinner.setValue(c.getRed());
        gSpinner.setValue(c.getGreen());
        bSpinner.setValue(c.getBlue());
        if (updateCmyk) {
            setCmykSpinners(c);
        }
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = centerX();
        int cy = centerY();
        int outerRadius = outerRadius();
        int innerRadius = innerRadius();
        int half = squareHalf();

        int outerSize = outerRadius * 2;
        for (int h = 0; h < 360; h++) {
            g2d.setColor(Color.getHSBColor(h / 360f, 1f, 1f));
            g2d.fillArc(cx - outerRadius, cy - outerRadius,
                    outerSize, outerSize, h, 2);
        }

        Shape innerCircle = new Ellipse2D.Double(
                cx - innerRadius, cy - innerRadius,
                innerRadius * 2.0, innerRadius * 2.0);
        g2d.setColor(getBackground());
        g2d.fill(innerCircle);
        g2d.setColor(Color.DARK_GRAY);
        g2d.draw(innerCircle);

        Color pureHue = Color.getHSBColor(hue, 1f, 1f);
        g2d.setPaint(new GradientPaint(
                cx - half, cy, Color.WHITE,
                cx + half, cy, pureHue));
        g2d.fillRect(cx - half, cy - half, half * 2, half * 2);

        g2d.setPaint(new GradientPaint(
                cx, cy - half, new Color(0, 0, 0, 0),
                cx, cy + half, new Color(0, 0, 0, 255)));
        g2d.fillRect(cx - half, cy - half, half * 2, half * 2);

        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRect(cx - half, cy - half, half * 2, half * 2);

        double markerRadius = (outerRadius + innerRadius) / 2.0;
        double angleRad = Math.toRadians(hue * 360.0);
        drawMarker(g2d,
                (int) Math.round(cx + Math.cos(angleRad) * markerRadius),
                (int) Math.round(cy - Math.sin(angleRad) * markerRadius));

        drawMarker(g2d,
                (int) Math.round(cx - half + saturation * half * 2.0),
                (int) Math.round(cy - half + (1 - brightness) * half * 2.0));
    }

    private void drawMarker(Graphics2D g2d, int x, int y) {
        int r = MARKER_SIZE / 2;
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3f));
        g2d.drawOval(x - r, y - r, MARKER_SIZE, MARKER_SIZE);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawOval(x - r, y - r, MARKER_SIZE, MARKER_SIZE);
    }
}
