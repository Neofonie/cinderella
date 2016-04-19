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

import de.neofonie.cinderella.core.RequestUtil;
import de.neofonie.cinderella.core.config.util.IpAddress;
import de.neofonie.cinderella.core.config.util.IpAddressRange;
import de.neofonie.cinderella.core.config.util.IpAddressRangeTypeAdapter;
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
