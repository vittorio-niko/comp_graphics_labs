package org.bsu.graphics.image_processing.ui;

import java.awt.image.BufferedImage;
import java.util.List;

interface ImageHost {

    record NamedImage(String name, BufferedImage image) {
    }

    BufferedImage currentSource();

    BufferedImage requireSource();

    void showResult(BufferedImage image, String caption);

    void setStatus(String text);

    void showComparison(String title, List<NamedImage> images, int columns, boolean withHistograms);
}
