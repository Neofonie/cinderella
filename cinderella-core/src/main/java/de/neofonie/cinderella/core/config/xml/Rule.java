/*
 *
 * The MIT License (MIT)
 * Copyright (c) 2016 Neofonie GmbH
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package de.neofonie.cinderella.core.config.xml;

import de.neofonie.cinderella.core.config.xml.condition.AndCondition;
import de.neofonie.cinderella.core.config.xml.condition.Condition;
import de.neofonie.cinderella.core.config.xml.condition.ConditionList;

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
        return AndCondition.allMatches(request, getConditions());
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

    public String getRuleKey(HttpServletRequest request) {
        final String identifier = getIdentifierType().getIdentifier(request);
        return identifier + '_' + getId();
    }
}
