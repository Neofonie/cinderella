package de.neofonie.common.cinderella.config.xml.condition;

import de.neofonie.common.cinderella.RequestUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * Checks if the user has a session.
 */
public class Session implements Condition {

    private boolean session;

    @Override
    public boolean matches(HttpServletRequest request) {
        boolean hasSession = RequestUtil.hasSession(request);
        return session == hasSession;
    }
}
