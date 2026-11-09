package org.bsu.graphics.image_processing.core;

import java.awt.image.BufferedImage;

public final class ContrastOps {

    private ContrastOps() {
    }

    public enum LinearMode {
        COMMON("Общий диапазон для R, G, B"),
        PER_CHANNEL("Отдельно для каждого канала");

        private final String title;

        LinearMode(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public enum EqualizationMode {
        GRAY("Полутоновое (по яркости Y)"),
        RGB("Цветное: каждый канал R, G, B"),
        HSV("Цветное: только V в HSV");

        private final String title;

        EqualizationMode(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public record StretchResult(BufferedImage image, int[] lo, int[] hi) {
    }

    public static StretchResult linearStretch(BufferedImage src, double clipPercent, LinearMode mode) {
        Histogram h = Histogram.of(src);
        int[] lo = new int[3];
        int[] hi = new int[3];
        int[][] luts = new int[3][];

        if (mode == LinearMode.COMMON) {
            int[] sum = new int[Histogram.LEVELS];
            for (int i = 0; i < Histogram.LEVELS; i++) {
                sum[i] = h.channel(Histogram.Channel.RED)[i]
                        + h.channel(Histogram.Channel.GREEN)[i]
                        + h.channel(Histogram.Channel.BLUE)[i];
            }
            int[] b = bounds(sum, clipPercent);
            int[] lut = stretchLut(b[0], b[1]);
            for (int c = 0; c < 3; c++) {
                lo[c] = b[0];
                hi[c] = b[1];
                luts[c] = lut;
            }
        } else {
            Histogram.Channel[] channels = {Histogram.Channel.RED, Histogram.Channel.GREEN, Histogram.Channel.BLUE};
            for (int c = 0; c < 3; c++) {
                int[] b = bounds(h.channel(channels[c]), clipPercent);
                lo[c] = b[0];
                hi[c] = b[1];
                luts[c] = stretchLut(b[0], b[1]);
            }
        }
        return new StretchResult(ImageUtils.applyLut(src, luts[0], luts[1], luts[2]), lo, hi);
    }

    static int[] bounds(int[] hist, double clipPercent) {
        long total = 0;
        for (int v : hist) {
            total += v;
        }
        long clip = Math.round(total * clipPercent / 100.0);

        int lo = 0;
        long acc = 0;
        for (; lo < 255; lo++) {
            acc += hist[lo];
            if (acc > clip) {
                break;
            }
        }
        int hi = 255;
        acc = 0;
        for (; hi > 0; hi--) {
            acc += hist[hi];
            if (acc > clip) {
                break;
            }
        }
        return new int[]{lo, hi};
    }

    static int[] stretchLut(int lo, int hi) {
        int[] lut = new int[Histogram.LEVELS];
        for (int i = 0; i < lut.length; i++) {
            lut[i] = hi > lo ? ImageUtils.clamp((i - lo) * 255.0 / (hi - lo)) : i;
        }
        return lut;
    }

    public static BufferedImage equalize(BufferedImage src, EqualizationMode mode) {
        return switch (mode) {
            case GRAY -> equalizeGray(src);
            case RGB -> equalizeRgb(src);
            case HSV -> equalizeHsv(src);
        };
    }

    public static int[] equalizationLut(int[] hist) {
        long[] cdf = Histogram.cumulative(hist);
        long n = cdf[cdf.length - 1];
        long cdfMin = 0;
        for (long v : cdf) {
            if (v > 0) {
                cdfMin = v;
                break;
            }
        }
        int[] lut = new int[hist.length];
        for (int i = 0; i < lut.length; i++) {
            lut[i] = n == cdfMin ? i : ImageUtils.clamp((cdf[i] - cdfMin) * 255.0 / (n - cdfMin));
        }
        return lut;
    }

    public static BufferedImage equalizeGray(BufferedImage src) {
        int[] y = ImageUtils.luminance(src);
        int[] hist = new int[Histogram.LEVELS];
        for (int v : y) {
            hist[v]++;
        }
        int[] lut = equalizationLut(hist);
        for (int i = 0; i < y.length; i++) {
            y[i] = lut[y[i]];
        }
        return ImageUtils.fromGray(y, src.getWidth(), src.getHeight());
    }

    public static BufferedImage equalizeRgb(BufferedImage src) {
        Histogram h = Histogram.of(src);
        return ImageUtils.applyLut(src,
                equalizationLut(h.channel(Histogram.Channel.RED)),
                equalizationLut(h.channel(Histogram.Channel.GREEN)),
                equalizationLut(h.channel(Histogram.Channel.BLUE)));
    }

    public static BufferedImage equalizeHsv(BufferedImage src) {
        int[] p = ImageUtils.pixels(src);
        double[][] hsv = new double[p.length][3];
        int[] hist = new int[Histogram.LEVELS];
        for (int i = 0; i < p.length; i++) {
            int c = p[i];
            ColorSpace.rgbToHsv(ImageUtils.red(c), ImageUtils.green(c), ImageUtils.blue(c), hsv[i]);
            hist[(int) hsv[i][2]]++;
        }
        int[] lut = equalizationLut(hist);
        for (int i = 0; i < p.length; i++) {
            p[i] = ColorSpace.hsvToRgb(hsv[i][0], hsv[i][1], lut[(int) hsv[i][2]]);
        }
        return ImageUtils.fromPixels(p, src.getWidth(), src.getHeight());
    }
}
