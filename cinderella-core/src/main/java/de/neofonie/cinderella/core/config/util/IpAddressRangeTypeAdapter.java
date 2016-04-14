package de.neofonie.cinderella.core.config.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class IpAddressRangeTypeAdapter extends XmlAdapter<String, IpAddressRange> {

    @Override
    public IpAddressRange unmarshal(String v) throws Exception {
        return IpAddressRange.valueOf(v);
    }

    @Override
    public String marshal(IpAddressRange v) throws Exception {
        return v.toString();
    }
}
