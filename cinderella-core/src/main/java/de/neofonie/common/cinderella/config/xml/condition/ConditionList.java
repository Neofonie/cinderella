package de.neofonie.common.cinderella.config.xml.condition;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import java.util.Collections;
import java.util.List;

/**
 * abstract class containing a list of conditions
 */
public abstract class ConditionList {

    @Valid
    @XmlElements(value = {
            @XmlElement(name = "requestPath", type = RequestPath.class),
            @XmlElement(name = "and", type = AndCondition.class),
            @XmlElement(name = "or", type = OrCondition.class),
            @XmlElement(name = "session", type = Session.class),
            @XmlElement(name = "ip", type = IpCondition.class)
    })
    private List<Condition> conditions;

    protected ConditionList() {
    }

    protected ConditionList(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public List<Condition> getConditions() {
        return conditions == null ? Collections.emptyList() : Collections.unmodifiableList(conditions);
    }

    void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }
}
