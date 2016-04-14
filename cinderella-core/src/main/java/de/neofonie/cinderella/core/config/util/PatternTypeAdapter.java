package de.neofonie.cinderella.core.config.util;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.regex.Pattern;

@XmlTransient
public class PatternTypeAdapter extends XmlAdapter<String, Pattern> {

    @Override
    public Pattern unmarshal(String v) throws Exception {
        return v == null || v.isEmpty() ? null : Pattern.compile(v);
    }

    @Override
    public String marshal(Pattern v) throws Exception {
        return v == null ? null : v.pattern();
    }
}
