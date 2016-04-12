package de.neofonie.common.cinderella.config.xml.condition;

import javax.servlet.http.HttpServletRequest;

/**
 * matches if any condition matches. If empty, it never matches.
 */
public class Or extends ConditionList implements Condition {

    @Override
    public boolean matches(HttpServletRequest request) {
        for (Condition condition : getConditions()) {
            boolean matches = condition.matches(request);
            if (matches) {
                return true;
            }
        }
        return false;
    }
}
