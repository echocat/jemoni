package org.echocat.jemoni.jmx.support;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static java.net.InetAddress.getByAddress;
import static java.net.InetAddress.getByName;

@ThreadSafe
@Immutable
public class InetAddressMatcher {

    @Nonnull
    public static InetAddressMatcher matcher(@Nonnull String host) throws IllegalArgumentException {
        return matcher(host, 0);
    }

    @Nonnull
    public static InetAddressMatcher matcher(@Nonnull String host, @Nonnegative int bits) throws IllegalArgumentException {
        return new InetAddressMatcher(host, bits);
    }

    @Nonnull
    public static InetAddressMatcher matcher(@Nonnull InetAddress address) throws IllegalArgumentException {
        return matcher(address, 0);
    }

    @Nonnull
    public static InetAddressMatcher matcher(@Nonnull InetAddress address, @Nonnegative int bits) throws IllegalArgumentException {
        return new InetAddressMatcher(address, bits);
    }

    private final InetAddress _address;
    private final int _bits;
    private final int _numberOfFirstBitsThatHaveToMatch;
    private final InetAddress _mask;

    public InetAddressMatcher(@Nonnull String host, @Nonnegative int bits) throws IllegalArgumentException {
        this(resolveHost(host), bits);
    }

    public InetAddressMatcher(@Nonnull InetAddress address, @Nonnegative int bits) {
        _address = address;
        _bits = bits;
        _mask = buildMaskFrom(address, bits);
        _numberOfFirstBitsThatHaveToMatch = (_mask.getAddress().length * 8) - _bits;
    }

    public boolean matches(@Nullable String host) throws IllegalArgumentException {
        return host != null && matches(resolveHost(host));
    }

    public boolean matches(@Nullable InetAddress that) {
        return that != null && matches(that.getAddress());
    }

    public boolean matches(@Nullable byte[] address) {
        final boolean result;
        if (address != null) {
            final byte[] mask = _mask.getAddress();
            if (address.length == mask.length) {
                result = matches(address, mask);
            } else {
                result = false;
            }
        } else {
            result = false;
        }
        return result;
    }

    protected boolean matches(@Nonnull byte[] left, @Nonnull byte[] right) {
        boolean result = left.length == right.length;
        for (int c = 0; result && c < _numberOfFirstBitsThatHaveToMatch; c++) {
            final int i = c / 8;
            final int bit = 1 << (c % 8);
            result = (left[i] & bit) == (right[i] & bit);
        }
        return result;
    }

    @Nonnull
    public InetAddress getAddress() {
        return _address;
    }

    @Nonnegative
    public int getBits() {
        return _bits;
    }

    @Nonnull
    public InetAddress getMask() {
        return _mask;
    }

    @Nonnull
    protected InetAddress buildMaskFrom(@Nonnull InetAddress address, @Nonnegative int lastMaskedBits) {
        try {
            return getByAddress(buildMaskFrom(address.getAddress(), lastMaskedBits));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Could not create a mask from " + address.getHostAddress() + "/" + _bits, e);
        }
    }

    @Nonnull
    protected byte[] buildMaskFrom(@Nonnull byte[] address, @Nonnegative int lastMaskedBits) {
        final byte[] mask = new byte[address.length];
        final int numberOfTotalBits = mask.length * 8;
        for (int c = 0; c < numberOfTotalBits - lastMaskedBits; c++) {
            final int i = c / 8;
            final int bit = (1 << (c % 8));
            if ((address[i] & bit) == bit) {
                mask[i] = (byte) (mask[i] | (1 << (c % 8)));
            }
        }
        return mask;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof InetAddressMatcher)) {
            result = false;
        } else {
            final InetAddressMatcher that = (InetAddressMatcher) o;
            result = getMask().equals(that.getMask()) && getBits() == that.getBits();
        }
        return result;
    }

    @Override
    public int hashCode() {
        return getMask().hashCode() * getBits();
    }

    @Override
    public String toString() {
        return getAddress().getHostAddress() + "/" + _bits + "(" + getMask().getHostAddress() + ")";
    }

    @Nonnull
    protected static InetAddress resolveHost(@Nonnull String host) throws IllegalArgumentException {
        try {
            return getByName(host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("The given host could not be resolved.", e);
        }
    }

}
