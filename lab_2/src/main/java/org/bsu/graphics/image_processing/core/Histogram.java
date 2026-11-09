package org.bsu.graphics.image_processing.core;

import java.awt.image.BufferedImage;

public final class Histogram {

    public static final int LEVELS = 256;

    public enum Channel {
        LUMA, RED, GREEN, BLUE
    }

    private final int[] red = new int[LEVELS];
    private final int[] green = new int[LEVELS];
    private final int[] blue = new int[LEVELS];
    private final int[] luma = new int[LEVELS];
    private final long total;

    private Histogram(BufferedImage img) {
        int[] p = ImageUtils.pixels(img);
        for (int c : p) {
            red[ImageUtils.red(c)]++;
            green[ImageUtils.green(c)]++;
            blue[ImageUtils.blue(c)]++;
            luma[ImageUtils.luma(c)]++;
        }
        total = p.length;
    }

    public static Histogram of(BufferedImage img) {
        return new Histogram(img);
    }

    public int[] channel(Channel channel) {
        return switch (channel) {
            case LUMA -> luma;
            case RED -> red;
            case GREEN -> green;
            case BLUE -> blue;
        };
    }

    public long total() {
        return total;
    }

    public static long[] cumulative(int[] hist) {
        long[] cdf = new long[hist.length];
        long acc = 0;
        for (int i = 0; i < hist.length; i++) {
            acc += hist[i];
            cdf[i] = acc;
        }
        return cdf;
    }

    public static int min(int[] hist) {
        for (int i = 0; i < hist.length; i++) {
            if (hist[i] > 0) {
                return i;
            }
        }
        return 0;
    }

    public static int max(int[] hist) {
        for (int i = hist.length - 1; i >= 0; i--) {
            if (hist[i] > 0) {
                return i;
            }
        }
        return 0;
    }

    public static double mean(int[] hist) {
        long n = 0;
        double sum = 0;
        for (int i = 0; i < hist.length; i++) {
            n += hist[i];
            sum += (double) i * hist[i];
        }
        return n == 0 ? 0 : sum / n;
    }

    public static double std(int[] hist) {
        double m = mean(hist);
        long n = 0;
        double sum = 0;
        for (int i = 0; i < hist.length; i++) {
            n += hist[i];
            sum += (i - m) * (i - m) * hist[i];
        }
        return n == 0 ? 0 : Math.sqrt(sum / n);
    }
}
