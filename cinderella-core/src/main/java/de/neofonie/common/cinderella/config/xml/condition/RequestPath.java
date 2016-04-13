package de.neofonie.common.cinderella.config.xml.condition;

import de.neofonie.common.cinderella.config.util.PatternTypeAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.regex.Pattern;

/**
 * matches, if the request-path is like the regex-pattern, see {@link java.util.regex.Pattern}
 */
@XmlRootElement(name = "requestPath")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestPath implements Condition {

    @XmlValue
    @NotNull
    @XmlJavaTypeAdapter(PatternTypeAdapter.class)
    private Pattern regex;

    public RequestPath() {
    }

    public RequestPath(String regex) {
        this.regex = Pattern.compile(regex);
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        return regex.matcher(request.getRequestURI()).find();
    }

    @Override
    public String toString() {
        return "RequestPath{" +
                "regex='" + regex + '\'' +
                '}';
    }
}

