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
    private int responseCode = 429;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        logger.debug("check ddos");
        if (!(req instanceof HttpServletRequest) || !(resp instanceof HttpServletResponse)) {
            chain.doFilter(req, resp);
            return;
        }
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        if ("/jcaptcha.png".equals(request.getRequestURI())) {
            captchaService.writeCaptchaToResponse(request, response);
            return;
        }
        String userCaptchaResponse = request.getParameter("captcha");
        if (userCaptchaResponse != null && captchaService.isValid(request, userCaptchaResponse)) {
            cinderellaService.whitelist(request);
            chain.doFilter(request, resp);
            return;
        }
        final String clientIpAddr = RequestUtil.getClientIpAddr(request);
        final ActionEnum actionEnum = cinderellaService.getAction(request);
        switch (actionEnum) {
            case NO_DDOS:
                chain.doFilter(request, resp);
                return;
            case DDOS:
                logger.info(String.format("suspicious request %s (%s)", request.getRequestURI(), clientIpAddr));
                response.setStatus(responseCode);
                request.getRequestDispatcher(ddosJsp).include(request, resp);
                return;
            case NO_RESPONSE:
                logger.info(String.format("suspicious request %s (%s) - send no response", request.getRequestURI(), clientIpAddr));
                response.setStatus(responseCode);
                return;
        }
        logger.error(String.format("action %s not supported", actionEnum));
        chain.doFilter(request, resp);
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

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}
