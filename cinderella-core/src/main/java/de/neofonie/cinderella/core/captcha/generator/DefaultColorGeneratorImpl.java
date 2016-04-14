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
