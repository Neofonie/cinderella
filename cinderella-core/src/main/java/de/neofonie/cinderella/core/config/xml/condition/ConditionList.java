package de.neofonie.cinderella.core.config.xml.condition;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import java.util.Collections;
import java.util.List;

/**
 * abstract class containing a list of conditions
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ConditionList {

    @Valid
    @XmlElements(value = {
            @XmlElement(name = "requestPath", type = RequestPath.class),
            @XmlElement(name = "and", type = AndCondition.class),
            @XmlElement(name = "or", type = OrCondition.class),
            @XmlElement(name = "session", type = Session.class),
            @XmlElement(name = "ip", type = IpCondition.class),
            @XmlElement(name = "header", type = RequestHeaderCondition.class)
    })
    private List<Condition> conditions;

    public ConditionList() {
    }

    public ConditionList(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public List<Condition> getConditions() {
        return conditions == null ? Collections.emptyList() : Collections.unmodifiableList(conditions);
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public String toString() {
        return "ConditionList{" +
                "conditions=" + conditions +
                '}';
    }
}
