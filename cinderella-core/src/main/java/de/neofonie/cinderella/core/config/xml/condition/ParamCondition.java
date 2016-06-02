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

package de.neofonie.cinderella.core.config.xml.condition;

import de.neofonie.cinderella.core.config.util.PatternTypeAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.regex.Pattern;

@XmlAccessorType(XmlAccessType.FIELD)
public class ParamCondition implements Condition {

    @NotNull
    @XmlAttribute(required = true)
    private String name;
    @NotNull
    @XmlJavaTypeAdapter(PatternTypeAdapter.class)
    @XmlValue
    private Pattern value;

    public ParamCondition() {
    }

    public ParamCondition(String name, Pattern value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        String[] headers = request.getParameterValues(name);
        if (headers == null) {
            return false;
        }
        for (String v : headers) {
            if (value.matcher(v).find()) {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Pattern getValue() {
        return value;
    }

    public void setValue(Pattern value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ParamCondition{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
