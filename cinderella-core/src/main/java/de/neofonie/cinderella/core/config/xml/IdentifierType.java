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

package de.neofonie.cinderella.core.config.xml;

import de.neofonie.cinderella.core.RequestUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * Type, which will be used to identified a user.
 */
public enum IdentifierType {

    /**
     * User will be identified by Session-ID. If he hasnt send a session-id, this identifier cannot be used.
     */
    SESSION {
        @Override
        public String getIdentifier(HttpServletRequest request) {
            return RequestUtil.getSessionId(request);
        }

        @Override
        public boolean accept(HttpServletRequest request) {
            return RequestUtil.hasSession(request);
        }
    },
    /**
     * User will be identified by IP. The first not empty value will be used {@link RequestUtil}
     * - request-Header "X-Forwarded-For"
     * - request-Header "Proxy-Client-IP"
     * - request-Header "WL-Proxy-Client-IP"
     * - request-Header "HTTP_CLIENT_IP"
     * - request-Header "HTTP_X_FORWARDED_FOR"
     * - the remote-ip of the connection in request.getRemoteAddr
     */
    IP {
        @Override
        public String getIdentifier(HttpServletRequest request) {
            return RequestUtil.getClientIpAddr(request);
        }

        @Override
        public boolean accept(HttpServletRequest request) {
            return true;
        }
    };

    public abstract String getIdentifier(HttpServletRequest request);

    public abstract boolean accept(HttpServletRequest request);
}
