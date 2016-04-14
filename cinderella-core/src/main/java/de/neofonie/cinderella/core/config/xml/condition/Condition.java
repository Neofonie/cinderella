package de.neofonie.cinderella.core.config.xml.condition;

import javax.servlet.http.HttpServletRequest;

/**
 * a condition
 */
public interface Condition {

    public boolean matches(HttpServletRequest request);
}
