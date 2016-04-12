package de.neofonie.common.cinderella.captcha;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by paasch on 07.04.16.
 */
public interface CaptchaService {

    void writeCaptchaToResponse(HttpServletRequest request, HttpServletResponse response) throws IOException;

    boolean isValid(HttpServletRequest request, String userCaptchaResponse);
}
