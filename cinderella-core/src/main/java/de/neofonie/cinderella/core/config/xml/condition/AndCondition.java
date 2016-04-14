package de.neofonie.cinderella.core.config.xml.condition;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * matches if all condition matches. If empty, it ever matches.
 */
public class AndCondition extends ConditionList implements Condition {

    @Override
    public boolean matches(HttpServletRequest request) {
        return allMatches(request, getConditions());
    }

    public static boolean allMatches(HttpServletRequest request, Collection<Condition> conditions) {
        if (conditions == null) {
            return true;
        }
        for (Condition condition : conditions) {
            if (!condition.matches(request)) {
                return false;
            }
        }
        return true;
    }
}
