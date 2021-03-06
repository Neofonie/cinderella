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

import de.neofonie.cinderella.core.config.CinderellaConfig;
import de.neofonie.cinderella.core.config.xml.condition.OrCondition;

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
    @XmlAttribute(required = false)
    private Long noResponseThreshould;

    @Override
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

    @Override
    public long getNoResponseThreshould() {
        if (noResponseThreshould == null) {
            return 0L;
        }
        return noResponseThreshould;
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
