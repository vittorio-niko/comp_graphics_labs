package org.bsu.graphics.image_processing.core;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public final class Segmentation {

    private Segmentation() {
    }

    public static final double[][] POINT_MASK = {
            {-1, -1, -1},
            {-1, 8, -1},
            {-1, -1, -1}};

    public static final double[][] SOBEL_X = {
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}};

    public static final double[][] SOBEL_Y = {
            {-1, -2, -1},
            {0, 0, 0},
            {1, 2, 1}};

    public enum LineDirection {
        HORIZONTAL("Горизонтальные ( — )", new double[][]{
                {-1, -1, -1},
                {2, 2, 2},
                {-1, -1, -1}}),
        PLUS_45("+45° ( / )", new double[][]{
                {-1, -1, 2},
                {-1, 2, -1},
                {2, -1, -1}}),
        VERTICAL("Вертикальные ( | )", new double[][]{
                {-1, 2, -1},
                {-1, 2, -1},
                {-1, 2, -1}}),
        MINUS_45("−45° ( \\ )", new double[][]{
                {2, -1, -1},
                {-1, 2, -1},
                {-1, -1, 2}}),
        ALL("Все направления", null);

        private final String title;
        private final double[][] mask;

        LineDirection(String title, double[][] mask) {
            this.title = title;
            this.mask = mask;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public record Detection(double[] response, int width, int height, double max) {

        public static Detection of(double[] response, int width, int height) {
            double max = 0;
            for (double v : response) {
                max = Math.max(max, v);
            }
            return new Detection(response, width, height, max);
        }

        public double threshold(double percent) {
            return max * percent / 100.0;
        }

        public boolean[] mask(double percent) {
            double t = threshold(percent);
            boolean[] m = new boolean[response.length];
            for (int i = 0; i < m.length; i++) {
                m[i] = response[i] > 0 && response[i] >= t;
            }
            return m;
        }
    }

    public static Detection points(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        double[] r = filter(ImageUtils.luminanceAsDouble(src), w, h, POINT_MASK);
        for (int i = 0; i < r.length; i++) {
            r[i] = Math.abs(r[i]);
        }
        return Detection.of(r, w, h);
    }

    public static Detection lines(BufferedImage src, LineDirection direction) {
        int w = src.getWidth();
        int h = src.getHeight();
        double[] f = ImageUtils.luminanceAsDouble(src);
        double[] result = new double[f.length];
        for (LineDirection d : LineDirection.values()) {
            if (d.mask == null || (direction != LineDirection.ALL && d != direction)) {
                continue;
            }
            double[] r = filter(f, w, h, d.mask);
            for (int i = 0; i < r.length; i++) {
                result[i] = Math.max(result[i], r[i]);
            }
        }
        return Detection.of(result, w, h);
    }

    public static Detection edges(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        double[] f = ImageUtils.luminanceAsDouble(src);
        double[] gx = filter(f, w, h, SOBEL_X);
        double[] gy = filter(f, w, h, SOBEL_Y);
        double[] m = new double[f.length];
        for (int i = 0; i < m.length; i++) {
            m[i] = Math.hypot(gx[i], gy[i]);
        }
        return Detection.of(m, w, h);
    }

    public static double[] filter(double[] f, int w, int h, double[][] mask) {
        double[] out = new double[f.length];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double sum = 0;
                for (int j = 0; j < 3; j++) {
                    int row = clampIndex(y + j - 1, h) * w;
                    for (int i = 0; i < 3; i++) {
                        sum += mask[j][i] * f[row + clampIndex(x + i - 1, w)];
                    }
                }
                out[y * w + x] = sum;
            }
        }
        return out;
    }

    private static int clampIndex(int i, int n) {
        return i < 0 ? 0 : (i >= n ? n - 1 : i);
    }

    public static BufferedImage renderMask(boolean[] mask, int w, int h) {
        int[] p = new int[mask.length];
        for (int i = 0; i < p.length; i++) {
            p[i] = mask[i] ? 0xFFFFFF : 0;
        }
        return ImageUtils.fromPixels(p, w, h);
    }

    public static BufferedImage renderOverlay(BufferedImage src, boolean[] mask, boolean circleMarks) {
        int w = src.getWidth();
        int[] y = ImageUtils.luminance(src);
        int[] p = new int[y.length];
        for (int i = 0; i < p.length; i++) {
            int v = (int) (y[i] * 0.75);
            p[i] = mask[i] ? 0xFF0000 : ImageUtils.rgb(v, v, v);
        }
        BufferedImage out = ImageUtils.fromPixels(p, w, src.getHeight());
        if (circleMarks) {
            Graphics2D g = out.createGraphics();
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(1.5f));
            int marked = 0;
            for (int i = 0; i < mask.length && marked < 2000; i++) {
                if (mask[i]) {
                    g.drawOval(i % w - 6, i / w - 6, 12, 12);
                    marked++;
                }
            }
            g.dispose();
        }
        return out;
    }

    public static int count(boolean[] mask) {
        int n = 0;
        for (boolean b : mask) {
            if (b) {
                n++;
            }
        }
        return n;
    }
}
