package de.neofonie.cinderella.core.captcha.generator;

import java.awt.image.BufferedImage;

public interface CaptchaGenerator {

    BufferedImage generateCaptcha(String text);
}
