package org.bsu.graphics.raster_algo.core;

public enum Algorithm {

    STEP("Пошаговый алгоритм", false),
    DDA("Алгоритм ЦДА", false),
    BRESENHAM_LINE("Алгоритм Брезенхема (отрезок)", false),
    BRESENHAM_CIRCLE("Алгоритм Брезенхема (окружность)", true);

    private final String title;
    private final boolean circle;

    Algorithm(String title, boolean circle) {
        this.title = title;
        this.circle = circle;
    }

    public boolean isCircle() {
        return circle;
    }

    public void run(int[] p, PixelSink sink) {
        switch (this) {
            case STEP -> Rasterizer.step(p[0], p[1], p[2], p[3], sink);
            case DDA -> Rasterizer.dda(p[0], p[1], p[2], p[3], sink);
            case BRESENHAM_LINE -> Rasterizer.bresenhamLine(p[0], p[1], p[2], p[3], sink);
            case BRESENHAM_CIRCLE -> Rasterizer.bresenhamCircle(p[0], p[1], p[2], sink);
        }
    }

    @Override
    public String toString() {
        return title;
    }
}
