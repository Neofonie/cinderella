package de.neofonie.common.cinderella.config.xml;

import de.neofonie.common.cinderella.config.xml.condition.Condition;
import de.neofonie.common.cinderella.config.xml.condition.ConditionList;

import java.util.List;

/**
 * A list of Conditions. If at least one condition matches, the request will be ignored for ddos-scanning. The List is also equal to {@link de.neofonie.common.cinderella.config.xml.condition.OrCondition}.
 */
public class Whitelist extends ConditionList {

    public Whitelist() {
    }

    public Whitelist(List<Condition> conditions) {
        super(conditions);
    }
}
