package de.neofonie.common.cinderella.config.xml.condition;

import javax.servlet.http.HttpServletRequest;

/**
 * matches if all condition matches. If empty, it ever matches.
 */
public class And extends ConditionList implements Condition {

    @Override
    public boolean matches(HttpServletRequest request) {
        for (Condition condition : getConditions()) {
            boolean matches = condition.matches(request);
            if (!matches) {
                return false;
            }
        }
        return true;
    }
}
