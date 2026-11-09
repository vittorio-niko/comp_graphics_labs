package org.bsu.graphics.image_processing.core;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public final class ImageUtils {

    private ImageUtils() {
    }

    public static BufferedImage toRgb(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_RGB) {
            return src;
        }
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, src.getWidth(), src.getHeight());
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return dst;
    }

    public static BufferedImage copy(BufferedImage src) {
        return fromPixels(pixels(src), src.getWidth(), src.getHeight());
    }

    public static int[] pixels(BufferedImage img) {
        int w = img.getWidth();
        return img.getRGB(0, 0, w, img.getHeight(), null, 0, w);
    }

    public static BufferedImage fromPixels(int[] pixels, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, w, h, pixels, 0, w);
        return img;
    }

    public static int red(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    public static int green(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    public static int blue(int rgb) {
        return rgb & 0xFF;
    }

    public static int rgb(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }

    public static int clamp(int v) {
        return v < 0 ? 0 : Math.min(v, 255);
    }

    public static int clamp(double v) {
        return clamp((int) Math.round(v));
    }

    public static int luma(int rgb) {
        return clamp(0.299 * red(rgb) + 0.587 * green(rgb) + 0.114 * blue(rgb));
    }

    public static int[] luminance(BufferedImage img) {
        int[] p = pixels(img);
        int[] y = new int[p.length];
        for (int i = 0; i < p.length; i++) {
            y[i] = luma(p[i]);
        }
        return y;
    }

    public static double[] luminanceAsDouble(BufferedImage img) {
        int[] y = luminance(img);
        double[] d = new double[y.length];
        for (int i = 0; i < y.length; i++) {
            d[i] = y[i];
        }
        return d;
    }

    public static BufferedImage fromGray(int[] gray, int w, int h) {
        int[] p = new int[gray.length];
        for (int i = 0; i < gray.length; i++) {
            int v = clamp(gray[i]);
            p[i] = rgb(v, v, v);
        }
        return fromPixels(p, w, h);
    }

    public static BufferedImage toGray(BufferedImage img) {
        return fromGray(luminance(img), img.getWidth(), img.getHeight());
    }

    public static BufferedImage applyLut(BufferedImage src, int[] lutR, int[] lutG, int[] lutB) {
        int[] p = pixels(src);
        for (int i = 0; i < p.length; i++) {
            int c = p[i];
            p[i] = rgb(lutR[red(c)], lutG[green(c)], lutB[blue(c)]);
        }
        return fromPixels(p, src.getWidth(), src.getHeight());
    }

    public static boolean isGray(BufferedImage img) {
        for (int c : pixels(img)) {
            if (red(c) != green(c) || green(c) != blue(c)) {
                return false;
            }
        }
        return true;
    }
}
