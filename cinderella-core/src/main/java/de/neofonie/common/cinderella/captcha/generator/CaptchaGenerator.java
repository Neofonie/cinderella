package de.neofonie.common.cinderella.captcha.generator;

import java.awt.image.BufferedImage;

public interface CaptchaGenerator {

    BufferedImage generateCaptcha(String text);
}
