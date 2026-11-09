package org.bsu.graphics.image_processing.core;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.IntUnaryOperator;
import javax.imageio.ImageIO;

public final class TestImageGenerator {

    private TestImageGenerator() {
    }

    public static void main(String[] args) throws IOException {
        File dir = new File(args.length > 0 ? args[0] : "images");
        for (File f : generateAll(dir)) {
            System.out.println(f.getPath());
        }
    }

    public static List<File> generateAll(File dir) throws IOException {
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new IOException("Не удалось создать каталог " + dir);
        }
        BufferedImage scene = scene(640, 420);
        BufferedImage shapes = shapes(512, 384);

        Map<String, BufferedImage> images = new LinkedHashMap<>();
        images.put("contrast_reference_color.png", scene);
        images.put("contrast_low_gray.png", mapChannels(ImageUtils.toGray(scene),
                v -> 100 + v * 55 / 255, v -> 100 + v * 55 / 255, v -> 100 + v * 55 / 255));
        images.put("contrast_dark_color.png", mapChannels(scene,
                v -> v * 70 / 255, v -> v * 70 / 255, v -> v * 70 / 255));
        images.put("contrast_washed_out_color.png", mapChannels(scene,
                v -> 150 + v * 95 / 255, v -> 150 + v * 95 / 255, v -> 150 + v * 95 / 255));
        images.put("contrast_underwater_color.png", mapChannels(scene,
                v -> 10 + v * 70 / 255, v -> 40 + v * 140 / 255, v -> 70 + v * 170 / 255));
        images.put("contrast_outliers_gray.png", withOutliers(
                mapChannels(ImageUtils.toGray(scene), v -> 90 + v * 70 / 255, v -> 90 + v * 70 / 255,
                        v -> 90 + v * 70 / 255)));

        images.put("points_isolated.png", points(480, 320));
        images.put("lines_thin.png", lines(512, 384));
        images.put("edges_shapes.png", shapes);
        images.put("edges_shapes_noisy.png", Noise.gaussian(shapes, 20, 7));

        List<File> files = new ArrayList<>();
        for (Map.Entry<String, BufferedImage> e : images.entrySet()) {
            File f = new File(dir, e.getKey());
            ImageIO.write(e.getValue(), "png", f);
            files.add(f);
        }
        return files;
    }

    static BufferedImage scene(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setPaint(new GradientPaint(0, 0, new Color(40, 90, 200), 0, h * 0.6f, new Color(250, 200, 150)));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(255, 230, 60));
        g.fillOval(w - 170, 40, 90, 90);

        g.setColor(new Color(90, 90, 110));
        g.fillPolygon(new Polygon(new int[]{0, 150, 300, 450, 640, 640, 0},
                new int[]{260, 110, 230, 120, 250, 300, 300}, 7));
        g.setColor(Color.WHITE);
        g.fillPolygon(new Polygon(new int[]{125, 150, 175}, new int[]{140, 110, 140}, 3));
        g.fillPolygon(new Polygon(new int[]{420, 450, 480}, new int[]{155, 120, 155}, 3));

        g.setColor(new Color(60, 140, 50));
        g.fillRect(0, 290, w, h - 290);
        g.setPaint(new GradientPaint(0, 330, new Color(30, 110, 170), 0, h, new Color(10, 40, 80)));
        g.fillRect(330, 330, w - 330, h - 330);

        g.setColor(new Color(200, 40, 30));
        g.fillRect(90, 250, 120, 90);
        g.setColor(new Color(110, 60, 30));
        g.fillPolygon(new Polygon(new int[]{80, 150, 220}, new int[]{250, 195, 250}, 3));
        g.setColor(new Color(240, 220, 120));
        g.fillRect(115, 275, 25, 25);
        g.fillRect(165, 275, 25, 25);
        g.setColor(new Color(70, 40, 20));
        g.fillRect(140, 300, 22, 40);

        g.setColor(new Color(20, 90, 30));
        for (int i = 0; i < 4; i++) {
            g.fillOval(240 + i * 22, 245 - (i % 2) * 15, 40, 60);
        }
        g.setColor(new Color(220, 60, 160));
        g.fillOval(30, 360, 30, 30);
        g.setColor(new Color(250, 140, 20));
        g.fillOval(70, 370, 25, 25);
        g.dispose();
        return Noise.gaussian(img, 3, 1);
    }

    static BufferedImage shapes(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(gray(60));
        g.fillRect(0, 0, w, h);
        g.setColor(gray(180));
        g.fillRect(40, 40, 160, 110);
        g.setColor(gray(120));
        g.fillOval(260, 30, 140, 140);
        g.setColor(gray(230));
        g.fillPolygon(new Polygon(new int[]{60, 140, 220}, new int[]{340, 190, 340}, 3));
        g.setColor(gray(95));
        g.fillOval(270, 220, 200, 110);
        g.setPaint(new GradientPaint(420, 0, gray(60), 500, 0, gray(240)));
        g.fillRect(420, 30, 80, 150);
        g.setColor(gray(200));
        g.setStroke(new BasicStroke(3));
        g.drawOval(330, 245, 80, 60);
        g.dispose();
        return img;
    }

    static BufferedImage points(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(gray(100));
        g.fillRect(0, 0, w, h);
        g.setColor(gray(160));
        g.fillRect(40, 40, 120, 80);
        g.setColor(gray(60));
        g.fillOval(300, 180, 120, 100);
        g.setColor(gray(140));
        g.fillRect(220, 60, 6, 120);
        g.dispose();

        img = Noise.gaussian(img, 3, 3);
        Random rnd = new Random(42);
        int[] levels = {255, 255, 255, 200, 160, 0};
        for (int i = 0; i < 24; i++) {
            int x = 10 + rnd.nextInt(w - 20);
            int y = 10 + rnd.nextInt(h - 20);
            int v = levels[i % levels.length];
            img.setRGB(x, y, ImageUtils.rgb(v, v, v));
        }
        return img;
    }

    static BufferedImage lines(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(gray(30));
        g.fillRect(0, 0, w, h);
        g.setColor(gray(220));
        g.drawLine(20, 30, 230, 30);
        g.drawLine(20, 60, 230, 60);
        g.drawLine(260, 20, 260, 200);
        g.drawLine(300, 20, 300, 200);
        g.drawLine(20, 100, 140, 220);
        g.drawLine(60, 100, 180, 220);
        g.drawLine(340, 200, 490, 50);
        g.drawLine(370, 200, 490, 80);
        g.drawLine(20, 250, 200, 360);
        g.setColor(gray(160));
        g.fillRect(240, 260, 240, 12);
        g.drawRect(260, 300, 200, 60);
        g.dispose();
        return Noise.gaussian(img, 4, 5);
    }

    private static BufferedImage withOutliers(BufferedImage img) {
        Random rnd = new Random(11);
        for (int i = 0; i < 300; i++) {
            int v = rnd.nextBoolean() ? 255 : 0;
            img.setRGB(rnd.nextInt(img.getWidth()), rnd.nextInt(img.getHeight()), ImageUtils.rgb(v, v, v));
        }
        return img;
    }

    private static BufferedImage mapChannels(BufferedImage src, IntUnaryOperator r, IntUnaryOperator g,
                                             IntUnaryOperator b) {
        int[] lr = new int[256];
        int[] lg = new int[256];
        int[] lb = new int[256];
        for (int i = 0; i < 256; i++) {
            lr[i] = ImageUtils.clamp(r.applyAsInt(i));
            lg[i] = ImageUtils.clamp(g.applyAsInt(i));
            lb[i] = ImageUtils.clamp(b.applyAsInt(i));
        }
        return ImageUtils.applyLut(src, lr, lg, lb);
    }

    private static Color gray(int v) {
        return new Color(v, v, v);
    }
}
