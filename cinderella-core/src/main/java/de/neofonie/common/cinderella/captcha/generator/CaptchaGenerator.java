package de.neofonie.common.cinderella.captcha.generator;

import java.awt.image.BufferedImage;

/**
 * Created by paasch on 07.04.16.
 */
public interface CaptchaGenerator {

    BufferedImage generateCaptcha(String text);
}
