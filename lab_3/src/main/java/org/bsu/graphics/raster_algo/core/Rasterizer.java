package org.bsu.graphics.raster_algo.core;

public final class Rasterizer {

    private Rasterizer() {
    }

    public static void step(int x1, int y1, int x2, int y2, PixelSink sink) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        if (dx == 0 && dy == 0) {
            sink.plot(x1, y1);
            return;
        }
        if (Math.abs(dx) >= Math.abs(dy)) {
            double k = (double) dy / dx;
            double b = y1 - k * x1;
            int sx = dx > 0 ? 1 : -1;
            for (int x = x1; ; x += sx) {
                sink.plot(x, (int) Math.round(k * x + b));
                if (x == x2) {
                    break;
                }
            }
        } else {
            double k = (double) dx / dy;
            double b = x1 - k * y1;
            int sy = dy > 0 ? 1 : -1;
            for (int y = y1; ; y += sy) {
                sink.plot((int) Math.round(k * y + b), y);
                if (y == y2) {
                    break;
                }
            }
        }
    }

    public static void dda(int x1, int y1, int x2, int y2, PixelSink sink) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (steps == 0) {
            sink.plot(x1, y1);
            return;
        }
        double incX = (double) dx / steps;
        double incY = (double) dy / steps;
        double x = x1;
        double y = y1;
        for (int i = 0; i <= steps; i++) {
            sink.plot((int) Math.round(x), (int) Math.round(y));
            x += incX;
            y += incY;
        }
    }

    public static void bresenhamLine(int x1, int y1, int x2, int y2, PixelSink sink) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x2 >= x1 ? 1 : -1;
        int sy = y2 >= y1 ? 1 : -1;
        boolean steep = dy > dx;
        int a = steep ? dy : dx;
        int b = steep ? dx : dy;
        int e = 2 * b - a;
        int x = x1;
        int y = y1;
        for (int i = 0; i <= a; i++) {
            sink.plot(x, y);
            if (e >= 0) {
                if (steep) {
                    x += sx;
                } else {
                    y += sy;
                }
                e -= 2 * a;
            }
            e += 2 * b;
            if (steep) {
                y += sy;
            } else {
                x += sx;
            }
        }
    }

    public static void bresenhamCircle(int xc, int yc, int r, PixelSink sink) {
        int x = 0;
        int y = r;
        int d = 3 - 2 * r;
        while (x <= y) {
            sink.plot(xc + x, yc + y);
            sink.plot(xc - x, yc + y);
            sink.plot(xc + x, yc - y);
            sink.plot(xc - x, yc - y);
            sink.plot(xc + y, yc + x);
            sink.plot(xc - y, yc + x);
            sink.plot(xc + y, yc - x);
            sink.plot(xc - y, yc - x);
            if (d >= 0) {
                d += 4 * (x - y) + 10;
                y--;
            } else {
                d += 4 * x + 6;
            }
            x++;
        }
    }
}
