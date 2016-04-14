package de.neofonie.common.cinderella.config.xml.condition;

import de.neofonie.common.cinderella.RequestUtil;
import de.neofonie.common.cinderella.config.util.IpAddress;
import de.neofonie.common.cinderella.config.util.IpAddressRange;
import de.neofonie.common.cinderella.config.util.IpAddressRangeTypeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class IpCondition implements Condition {

    private static final Logger logger = LoggerFactory.getLogger(IpCondition.class);
    @XmlValue
    @NotNull
    @XmlJavaTypeAdapter(IpAddressRangeTypeAdapter.class)
    private IpAddressRange ipAddressRange;

    @Override
    public boolean matches(HttpServletRequest request) {
        try {
            String clientIpAddr = RequestUtil.getClientIpAddr(request);
            IpAddress ipAddress = IpAddress.valueOf(clientIpAddr);
            return ipAddressRange.contains(ipAddress);
        } catch (IllegalArgumentException e) {
            logger.info(e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("", e);
            }
            return false;
        }
    }

    public IpAddressRange getIpAddressRange() {
        return ipAddressRange;
    }

    public void setIpAddressRange(IpAddressRange ipAddressRange) {
        this.ipAddressRange = ipAddressRange;
    }

    @Override
    public String toString() {
        return "IpCondition{" +
                "ipAddressRange=" + ipAddressRange +
                '}';
    }
}
