package de.neofonie.common.cinderella.config.util;

import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpAddress {

    private final InetAddress inetAddress;

    private IpAddress(String value) {
        try {
            inetAddress = InetAddress.getByName(value);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(getErrorMessage(value), e);
        }
    }

    public static IpAddress valueOf(String value) {

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(getErrorMessage(value));
        }
        return new IpAddress(value);
    }

    private static String getErrorMessage(String value) {
        return String.format("'%s' is not a valid IP-Adress", value);
    }

    public boolean isLesserEqualThan(IpAddress ipAddress) {
        Preconditions.checkNotNull(ipAddress);
        BigInteger address = getAddress();
        BigInteger otherAddress = ipAddress.getAddress();
//        Preconditions.checkArgument(address.bitLength() == otherAddress.bitLength(), "mixed IP4 with IP6");
        return address.bitLength() == otherAddress.bitLength() && address.compareTo(otherAddress) <= 0;
    }

    public BigInteger getAddress() {
        return new BigInteger(inetAddress.getAddress());
    }

    @Override
    public int hashCode() {
        return getAddress().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IpAddress ipAddress = (IpAddress) o;

        return getAddress().equals(ipAddress.getAddress());
    }

    @Override
    public String toString() {
        String string = inetAddress.toString();
        if (string.startsWith("/")) {
            return string.substring(1);
        }
        return string;
    }
}
