package de.neofonie.cinderella.core.config.xml.condition;

import de.neofonie.cinderella.core.config.util.PatternTypeAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Enumeration;
import java.util.regex.Pattern;

@XmlAccessorType(XmlAccessType.FIELD)
public class RequestHeaderCondition implements Condition {

    @NotNull
    private String name;
    @NotNull
    @XmlJavaTypeAdapter(PatternTypeAdapter.class)
    private Pattern value;

    public RequestHeaderCondition() {
    }

    public RequestHeaderCondition(String name, Pattern value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        Enumeration<String> headers = request.getHeaders(name);
        if (headers == null) {
            return false;
        }
        while (headers.hasMoreElements()) {
            String v = headers.nextElement();
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
        return "RequestHeaderCondition{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
