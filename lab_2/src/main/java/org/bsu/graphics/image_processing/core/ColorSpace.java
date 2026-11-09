package org.bsu.graphics.image_processing.core;

public final class ColorSpace {

    private ColorSpace() {
    }

    public static void rgbToHsv(int r, int g, int b, double[] hsv) {
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        double delta = max - min;

        double h;
        if (delta == 0) {
            h = 0;
        } else if (max == r) {
            h = 60 * (((g - b) / delta) % 6);
        } else if (max == g) {
            h = 60 * ((b - r) / delta + 2);
        } else {
            h = 60 * ((r - g) / delta + 4);
        }
        if (h < 0) {
            h += 360;
        }
        hsv[0] = h;
        hsv[1] = max == 0 ? 0 : delta / max;
        hsv[2] = max;
    }

    public static int hsvToRgb(double h, double s, double v) {
        double c = v * s;
        double hp = h / 60.0;
        double x = c * (1 - Math.abs(hp % 2 - 1));
        double r;
        double g;
        double b;
        if (hp < 1) {
            r = c; g = x; b = 0;
        } else if (hp < 2) {
            r = x; g = c; b = 0;
        } else if (hp < 3) {
            r = 0; g = c; b = x;
        } else if (hp < 4) {
            r = 0; g = x; b = c;
        } else if (hp < 5) {
            r = x; g = 0; b = c;
        } else {
            r = c; g = 0; b = x;
        }
        double m = v - c;
        return ImageUtils.rgb(ImageUtils.clamp(r + m), ImageUtils.clamp(g + m), ImageUtils.clamp(b + m));
    }
}
