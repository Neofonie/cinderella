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
