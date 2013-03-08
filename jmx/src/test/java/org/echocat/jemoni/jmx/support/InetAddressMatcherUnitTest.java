package org.echocat.jemoni.jmx.support;

import org.junit.Test;

import static org.echocat.jemoni.jmx.support.InetAddressMatcher.matcher;
import static org.echocat.jomon.testing.Assert.assertThat;
import static org.echocat.jomon.testing.BaseMatchers.is;

public class InetAddressMatcherUnitTest {

    @Test
    public void testMatchesIpv60Bits() throws Exception {
        assertThat(matcher("6666:6666:6666:6666:6666:6666:6666:6666", 0).matches("6666:6666:6666:6666:6666:6666:6666:6666"), is(true));
        assertThat(matcher("6666:6666:6666:6666:6666:6666:6666:6666", 0).matches("6666:6666:6666:6666:6666:6666:6666:6677"), is(false));
    }

    @Test
    public void testMatchesIpv6With8Bits() throws Exception {
        assertThat(matcher("6666:6666:6666:6666:6666:6666:6666:6666", 8).matches("6666:6666:6666:6666:6666:6666:6666:6677"), is(true));
        assertThat(matcher("6666:6666:6666:6666:6666:6666:6666:6666", 8).matches("6666:6666:6666:6666:6666:6666:6666:7777"), is(false));
    }

    @Test
    public void testMatchesIpv6With16Bits() throws Exception {
        assertThat(matcher("6666:6666:6666:6666:6666:6666:6666:6666", 16).matches("6666:6666:6666:6666:6666:6666:6666:7777"), is(true));
        assertThat(matcher("6666:6666:6666:6666:6666:6666:6666:6666", 16).matches("6666:6666:6666:6666:6666:6666:6677:7777"), is(false));
    }

    @Test
    public void testMatchesIpv6With24Bits() throws Exception {
        assertThat(matcher("6666:6666:6666:6666:6666:6666:6666:6666", 24).matches("6666:6666:6666:6666:6666:6666:6677:7777"), is(true));
        assertThat(matcher("6666:6666:6666:6666:6666:6666:6666:6666", 24).matches("6666:6666:6666:6666:6666:6666:7777:7777"), is(false));
    }

    @Test
    public void testMatchesIpv6With32Bits() throws Exception {
        assertThat(matcher("6666:6666:6666:6666:6666:6666:6666:6666", 32).matches("6666:6666:6666:6666:6666:6666:7777:7777"), is(true));
        assertThat(matcher("6666:6666:6666:6666:6666:6666:6666:6666", 32).matches("6666:6666:6666:6666:6666:6677:7777:7777"), is(false));
    }

    @Test
    public void testMatchesIpv6With40Bits() throws Exception {
        assertThat(matcher("6666:6666:6666:6666:6666:6666:6666:6666", 40).matches("6666:6666:6666:6666:6666:6677:7777:7777"), is(true));
        assertThat(matcher("6666:6666:6666:6666:6666:6666:6666:6666", 40).matches("6666:6666:6666:6666:6666:7777:7777:7777"), is(false));
    }

    @Test
    public void testMatchesIpv6With128Bits() throws Exception {
        assertThat(matcher("6666:6666:6666:6666:6666:6666:6666:6666", 128).matches("7777:7777:7777:7777:7777:7777:7777:7777"), is(true));
    }

    @Test
    public void testMatchesIpv4With0Bits() throws Exception {
        assertThat(matcher("6.6.6.6", 0).matches("6.6.6.6"), is(true));
        assertThat(matcher("6.6.6.6", 0).matches("6.6.6.7"), is(false));
    }

    @Test
    public void testMatchesIpv4With8Bits() throws Exception {
        assertThat(matcher("6.6.6.6", 8).matches("6.6.6.7"), is(true));
        assertThat(matcher("6.6.6.6", 8).matches("6.6.7.7"), is(false));
    }

    @Test
    public void testMatchesIpv4With16Bits() throws Exception {
        assertThat(matcher("6.6.6.6", 16).matches("6.6.7.7"), is(true));
        assertThat(matcher("6.6.6.6", 16).matches("6.7.7.7"), is(false));
    }

    @Test
    public void testMatchesIpv4With24Bits() throws Exception {
        assertThat(matcher("6.6.6.6", 24).matches("7.7.7.7"), is(false));
    }

    @Test
    public void testMatchesLocalhost() throws Exception {
        assertThat(matcher("localhost", 0).matches("127.0.0.1"), is(true));
    }

}
