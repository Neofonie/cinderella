package de.neofonie.common.cinderella.config.xml.condition;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * matches if any condition matches. If empty, it never matches.
 */
public class OrCondition extends ConditionList implements Condition {

    @Override
    public boolean matches(HttpServletRequest request) {
        return anyMatches(request, getConditions());
    }

    public static boolean anyMatches(HttpServletRequest request, Collection<Condition> conditions) {
        if (conditions == null) {
            return false;
        }
        for (Condition condition : conditions) {
            if (condition.matches(request)) {
                return true;
            }
        }
        return false;
    }
}
