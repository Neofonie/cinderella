package de.neofonie.cinderella.core.config.xml.condition;

import de.neofonie.cinderella.core.RequestUtil;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Checks if the user has a session.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Session implements Condition {

    @XmlAttribute(required = true)
    private boolean session;

    @Override
    public boolean matches(HttpServletRequest request) {
        boolean hasSession = RequestUtil.hasSession(request);
        return session == hasSession;
    }

    public boolean isSession() {
        return session;
    }

    public void setSession(boolean session) {
        this.session = session;
    }

    @Override
    public String toString() {
        return "Session{" +
                "session=" + session +
                '}';
    }
}
