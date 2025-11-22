package com.auroratms.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;

public class ImagePreprocessor {

    public static BufferedImage preprocess(BufferedImage src) {
        BufferedImage gray = toGray(src);
        BufferedImage contrast = increaseContrast(gray);
        BufferedImage sharp = sharpen(contrast);
        return binarize(sharp);
    }

    private static BufferedImage toGray(BufferedImage src) {
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = dest.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return dest;
    }

    private static BufferedImage increaseContrast(BufferedImage img) {
        RescaleOp op = new RescaleOp(1.5f, -25, null);
        return op.filter(img, null);
    }

    private static BufferedImage sharpen(BufferedImage img) {
        float[] kernel = {
                0f, -1f, 0f,
                -1f, 5f, -1f,
                0f, -1f, 0f
        };
        ConvolveOp op = new ConvolveOp(new Kernel(3, 3, kernel));
        return op.filter(img, null);
    }

    private static BufferedImage binarize(BufferedImage img) {
        BufferedImage bin = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = bin.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return bin;
    }
}
