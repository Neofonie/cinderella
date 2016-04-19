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

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class DefaultColorGeneratorImpl implements ColorGenerator {

    private final float minSaturation;
    private final float maxSaturation;
    private final float minBrightness;
    private final float maxBrightness;

    DefaultColorGeneratorImpl(float minSaturation, float maxSaturation, float minBrightness, float maxBrightness) {
        this.minSaturation = minSaturation;
        this.maxSaturation = maxSaturation;
        this.minBrightness = minBrightness;
        this.maxBrightness = maxBrightness;
    }

    @Override
    public Color createRandomColor() {
        float h = ThreadLocalRandom.current().nextFloat();
        float s = (float) ThreadLocalRandom.current().nextDouble(minSaturation, maxSaturation);
        float b = (float) ThreadLocalRandom.current().nextDouble(minBrightness, maxBrightness);
        return Color.getHSBColor(h, s, b);
    }
}
