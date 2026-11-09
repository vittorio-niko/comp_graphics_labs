package org.bsu.graphics.raster_algo.core;

@FunctionalInterface
public interface PixelSink {

    void plot(int x, int y);
}
