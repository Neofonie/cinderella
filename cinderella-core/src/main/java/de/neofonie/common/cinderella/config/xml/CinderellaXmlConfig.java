package de.neofonie.common.cinderella.config.xml;

import de.neofonie.common.cinderella.config.CinderellaConfig;
import de.neofonie.common.cinderella.config.xml.condition.OrCondition;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CinderellaXmlConfig implements CinderellaConfig {

    public static final Class[] CLASSES = new Class[]{CinderellaXmlConfig.class};
    public static final String NAMESPACE = "http://www.neofonie.de/xsd/cinderella.xsd";
    //    @XmlElementWrapper(name = "whitelist")
    @Valid
    private Whitelist whitelist;
    @XmlElement(name = "rule")
    @XmlElementWrapper
    @Valid
    private List<Rule> rules;
    @XmlAttribute(required = true)
    private long whitelistMinutes;
    @XmlAttribute(required = true)
    private long blacklistMinutes;

    public List<Rule> getRules() {
        return rules;
    }

    void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    void setWhitelist(Whitelist whitelist) {
        this.whitelist = whitelist;
    }

    @Override
    public String toString() {
        return "CinderellaXmlConfig{" +
                "whitelist=" + whitelist +
                ", rules=" + rules.stream().map(s -> s.toString()).collect(Collectors.joining(",")) +
                ", whitelistMinutes=" + whitelistMinutes +
                ", blacklistMinutes=" + blacklistMinutes +
                '}';
    }

    @Override
    public List<Rule> getMatches(HttpServletRequest httpServletRequest) {
        if (whitelist != null && OrCondition.anyMatches(httpServletRequest, whitelist.getConditions())) {
            return Collections.emptyList();
        }

        if (rules == null) {
            return Collections.emptyList();
        }
        return rules
                .stream()
                .filter(rule -> rule.matches(httpServletRequest))
                .collect(Collectors.toList());
    }

    @Override
    public long getBlacklistMinutes() {
        return blacklistMinutes;
    }

    @Override
    public long getWhitelistMinutes() {
        return whitelistMinutes;
    }

    public void validate() {

        Set<String> ids = new HashSet<>();
        for (Rule rule : rules) {
            if (!ids.add(rule.getId())) {
                throw new IllegalArgumentException(String.format("Duplicate Id '%s' occured", rule.getId()));
            }
        }
    }
}
