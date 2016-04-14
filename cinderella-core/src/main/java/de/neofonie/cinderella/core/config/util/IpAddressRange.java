package de.neofonie.cinderella.core.config.util;

import com.google.common.base.Preconditions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class IpAddressRange {

    private static final Pattern RANGE_PATTERN1 = Pattern.compile("(.+)-(.+)");

    public static IpAddressRange valueOf(String value) {
        if (value == null) {
            throw new IllegalArgumentException(getErrorMessage(value));
        }
        try {
            Matcher matcher = RANGE_PATTERN1.matcher(value);
            if (matcher.matches()) {
                IpAddress start = IpAddress.valueOf(matcher.group(1));
                IpAddress end = IpAddress.valueOf(matcher.group(2));
                if (start.equals(end)) {
                    return ofIp(start);
                }
                return create(start, end);
            }
            return ofIp(IpAddress.valueOf(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(getErrorMessage(value), e);
        }
    }

    public static IpAddressRange create(IpAddress start, IpAddress end) {
        return new RealRange(start, end);
    }

    public static IpAddressRange ofIp(IpAddress ipAddress) {
        return new SingleRange(ipAddress);
    }

    private static String getErrorMessage(String value) {
        return String.format("'%s' is not a valid IP-Range", value);
    }

    public abstract boolean contains(IpAddress ipAddress);

    private static class SingleRange extends IpAddressRange {

        private final IpAddress ipAddress;

        private SingleRange(IpAddress ipAddress) {
            Preconditions.checkNotNull(ipAddress);
            this.ipAddress = ipAddress;
        }

        @Override
        public String toString() {
            return ipAddress.toString();
        }

        @Override
        public boolean contains(IpAddress ipAddress) {
            Preconditions.checkNotNull(ipAddress);
            return this.ipAddress.equals(ipAddress);
        }
    }

    private static class RealRange extends IpAddressRange {

        private final IpAddress start;
        private final IpAddress end;

        private RealRange(IpAddress start, IpAddress end) {
            Preconditions.checkNotNull(start);
            Preconditions.checkNotNull(end);
            this.start = start;
            this.end = end;
            Preconditions.checkArgument(start.getClass().equals(end.getClass()));
            Preconditions.checkArgument(start.isLesserEqualThan(end));
        }

        @Override
        public String toString() {
            return start + "-" + end;
        }

        @Override
        public boolean contains(IpAddress ipAddress) {
            Preconditions.checkNotNull(ipAddress);
            return start.isLesserEqualThan(ipAddress) && ipAddress.isLesserEqualThan(end);
        }
    }
}
