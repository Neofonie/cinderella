package de.neofonie.common.cinderella.config.xml.condition;

import de.neofonie.common.cinderella.config.util.PatternTypeAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class RequestHeaderCondition implements Condition {

    @XmlValue
    @NotNull
    @XmlJavaTypeAdapter(PatternTypeAdapter.class)
    private String name;
    @XmlValue
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
}
