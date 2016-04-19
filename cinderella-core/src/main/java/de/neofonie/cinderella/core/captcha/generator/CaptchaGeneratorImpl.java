/*
 *
 * The MIT License (MIT)
 * Copyright (c) 2016 Neofonie GmbH
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package de.neofonie.cinderella.core.captcha.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

public class CaptchaGeneratorImpl implements CaptchaGenerator {

    private static final Logger logger = LoggerFactory.getLogger(CaptchaGeneratorImpl.class);
    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    private int fontSize = 20;
    private int width = 200;
    private int height = 100;
    private ColorGenerator backgroundColor = new DefaultColorGeneratorImpl(0.0f, 0.1f, 0.9f, 1.0f);
    private ColorGenerator textColor = new DefaultColorGeneratorImpl(0.5f, 1.0f, 0.0f, 0.3f);
    private ColorGenerator noiseColor = new DefaultColorGeneratorImpl(0.0f, 1.0f, 0.5f, 0.8f);
    private int noise = 30;
    private int padding = 1;

    @Override
    public BufferedImage generateCaptcha(String text) {
        BufferedImage buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphic = buffer.createGraphics();
        try {
            fillBackground(graphic);
            appendNoise(graphic);
            appendText(graphic, text);
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            graphic.dispose();
        }
        return buffer;
    }

    private void appendNoise(Graphics2D graphic) {
        for (int i = 0; i < noise; i++) {
            graphic.setColor(noiseColor.createRandomColor());
            graphic.setStroke(new BasicStroke((float) RANDOM.nextDouble(2, 5)));
            int x = RANDOM.nextInt(1, width);
            int y = RANDOM.nextInt(1, height);
            int x2 = (int) (x + rand(10));
            int y2 = (int) (y + rand(10));
            graphic.drawLine(x, y, x2, y2);
        }
    }

    private void fillBackground(Graphics2D graphic) {
        graphic.setColor(backgroundColor.createRandomColor());
        graphic.fillRect(0, 0, width, height);
    }

    private void appendText(Graphics2D graphic, String text) {
        Font font = new Font("Arial", Font.BOLD, fontSize);
        graphic.setFont(font);
        graphic.setColor(textColor.createRandomColor());
        Rectangle2D stringBounds = font.getStringBounds(text, graphic.getFontRenderContext());
        int x = RANDOM.nextInt(1, width - (int) stringBounds.getWidth() - padding * text.length());
        int y = RANDOM.nextInt((int) stringBounds.getHeight(), height - (int) stringBounds.getHeight());
        for (char c : text.toCharArray()) {
            graphic.setColor(textColor.createRandomColor());
            String str = Character.toString(c);
            Rectangle2D charBounds = font.getStringBounds(str, graphic.getFontRenderContext());
            AffineTransform affineTransform =
                    AffineTransform
                            .getRotateInstance(rand(0.1), x + charBounds.getCenterX(), y + charBounds.getCenterY());
            affineTransform.concatenate(AffineTransform.getShearInstance(rand(0.01), rand(0.01)));
            graphic.setTransform(affineTransform);
            graphic.drawString(str, x, y);

            x += (int) charBounds.getWidth() + padding;
        }
    }

    private double rand(double sigma) {
        return RANDOM.nextGaussian() * sigma;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setBackgroundColor(ColorGenerator backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setTextColor(ColorGenerator textColor) {
        this.textColor = textColor;
    }

    public void setNoiseColor(ColorGenerator noiseColor) {
        this.noiseColor = noiseColor;
    }

    public void setNoise(int noise) {
        this.noise = noise;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

}
