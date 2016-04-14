package de.neofonie.cinderella.core;

import de.neofonie.cinderella.core.captcha.CaptchaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public class CinderellaFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(CinderellaFilter.class);

    private CinderellaService cinderellaService;
    private CaptchaService captchaService;
    private String ddosJsp;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        logger.debug("check ddos");
        if (!(req instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(req, response);
            return;
        }
        HttpServletRequest request = (HttpServletRequest) req;
        if ("/jcaptcha.png".equals(request.getRequestURI())) {
            captchaService.writeCaptchaToResponse(request, (HttpServletResponse) response);
            return;
        }
        String userCaptchaResponse = request.getParameter("captcha");
        if (userCaptchaResponse != null && captchaService.isValid(request, userCaptchaResponse)) {
            cinderellaService.whitelist(request);
            chain.doFilter(request, response);
            return;
        }
        if (!cinderellaService.isDdos(request)) {
            chain.doFilter(request, response);
            return;
        }
        logger.info(String.format("suspicious request %s", request.getRequestURI()));
        request.getRequestDispatcher(ddosJsp).include(request, response);
    }

    @Override
    public void destroy() {

    }

    @Autowired
    public void setCinderellaService(CinderellaService cinderellaService) {
        this.cinderellaService = cinderellaService;
    }

    @Required
    public void setDdosJsp(String ddosJsp) {
        this.ddosJsp = ddosJsp;
    }

    @Autowired
    public void setCaptchaService(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }
}
