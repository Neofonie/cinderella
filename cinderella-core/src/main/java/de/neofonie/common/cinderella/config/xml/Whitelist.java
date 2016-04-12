package de.neofonie.common.cinderella.config.xml;

import de.neofonie.common.cinderella.config.xml.condition.ConditionList;

/**
 * A list of Conditions. If at least one condition matches, the request will be ignored for ddos-scanning. The List is also equal to {@link de.neofonie.common.cinderella.config.xml.condition.Or}.
 */
public class Whitelist extends ConditionList {

}
