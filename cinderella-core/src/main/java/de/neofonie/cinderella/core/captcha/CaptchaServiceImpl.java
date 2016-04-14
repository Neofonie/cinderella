package de.neofonie.cinderella.core.captcha;

import de.neofonie.cinderella.core.captcha.generator.CaptchaGenerator;
import de.neofonie.cinderella.core.captcha.text.TextGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class CaptchaServiceImpl implements CaptchaService {

    public static final String SESSION_ATTRIBUTE = "CAPTCHA";
    private TextGenerator textGenerator;
    private CaptchaGenerator captchaGenerator;

    @Override
    public void writeCaptchaToResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String text = textGenerator.generateText();
        request.getSession().setAttribute(SESSION_ATTRIBUTE, text);

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/png");
        BufferedImage bufferedImage = captchaGenerator.generateCaptcha(text);
        ServletOutputStream responseOutputStream = response.getOutputStream();
        ImageIO.write(bufferedImage, "png", responseOutputStream);
        responseOutputStream.flush();
        responseOutputStream.close();
    }

    @Override
    public boolean isValid(HttpServletRequest request, String userCaptchaResponse) {
        HttpSession session = request.getSession();
        boolean valid = userCaptchaResponse != null
                && userCaptchaResponse.equals(session.getAttribute(SESSION_ATTRIBUTE));
        if (valid) {
            session.removeAttribute(SESSION_ATTRIBUTE);
        }
        return valid;
    }

    @Autowired
    public void setTextGenerator(TextGenerator textGenerator) {
        this.textGenerator = textGenerator;
    }

    @Autowired
    public void setCaptchaGenerator(CaptchaGenerator captchaGenerator) {
        this.captchaGenerator = captchaGenerator;
    }
}
