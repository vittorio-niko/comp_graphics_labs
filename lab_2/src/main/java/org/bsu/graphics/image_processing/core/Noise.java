package org.bsu.graphics.image_processing.core;

import java.awt.image.BufferedImage;
import java.util.Random;

public final class Noise {

    private Noise() {
    }

    public static BufferedImage gaussian(BufferedImage src, double sigma, long seed) {
        Random rnd = new Random(seed);
        int[] p = ImageUtils.pixels(src);
        for (int i = 0; i < p.length; i++) {
            int c = p[i];
            double n = rnd.nextGaussian() * sigma;
            p[i] = ImageUtils.rgb(
                    ImageUtils.clamp(ImageUtils.red(c) + n),
                    ImageUtils.clamp(ImageUtils.green(c) + n),
                    ImageUtils.clamp(ImageUtils.blue(c) + n));
        }
        return ImageUtils.fromPixels(p, src.getWidth(), src.getHeight());
    }

    public static BufferedImage saltAndPepper(BufferedImage src, double density, long seed) {
        Random rnd = new Random(seed);
        int[] p = ImageUtils.pixels(src);
        for (int i = 0; i < p.length; i++) {
            if (rnd.nextDouble() < density) {
                p[i] = rnd.nextBoolean() ? 0xFFFFFF : 0;
            }
        }
        return ImageUtils.fromPixels(p, src.getWidth(), src.getHeight());
    }
}
