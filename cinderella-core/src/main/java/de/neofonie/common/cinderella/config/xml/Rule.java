package de.neofonie.common.cinderella.config.xml;

import de.neofonie.common.cinderella.config.xml.condition.Condition;
import de.neofonie.common.cinderella.config.xml.condition.ConditionList;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.List;
import java.util.stream.Collectors;

/**
 * if all conditions matches and the {@link IdentifierType} is valid for the current request, the counter for this rule will be increased.
 *
 * If then the counter is greater than {@linkplain requests} in the last {@link minutes}, the request will be regarded as DDOS.
 *
 * After {@link minutes}, the counter will be reseted to zero.
 *
 * The {@link id} must be unique for all rules. If the id for a rule is changed, the counter will be lost or the counter of another, former rule could be used.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Rule extends ConditionList implements Condition {

    @XmlAttribute(required = true)
    private String id;
    @XmlAttribute(required = true)
    private IdentifierType identifierType;
    @XmlAttribute(required = true)
    private long requests;
    @XmlAttribute(required = true)
    private long minutes;

    public Rule() {
    }

    public Rule(List<Condition> conditions, String id, IdentifierType identifierType, long requests, long minutes) {
        super(conditions);
        this.id = id;
        this.identifierType = identifierType;
        this.requests = requests;
        this.minutes = minutes;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        if (!identifierType.accept(request)) {
            return false;
        }
        for (Condition condition : getConditions()) {
            boolean matches = condition.matches(request);
            if (!matches) {
                return false;
            }
        }
        return true;
    }

    public String getId() {
        return id;
    }

    public IdentifierType getIdentifierType() {
        return identifierType;
    }

    public long getRequests() {
        return requests;
    }

    public long getMinutes() {
        return minutes;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "conditions=" + getConditions().stream().map(s -> s.toString()).collect(Collectors.joining(",")) +
                '}';
    }
}
