package org.echocat.jemoni.jmx.support;

import org.echocat.jemoni.jmx.support.ServletHealth.ScopeMapping;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.servlet.ServletRequest;
import java.util.Collection;
import java.util.List;

import static org.echocat.jemoni.jmx.support.InetAddressMatcher.matcher;
import static org.echocat.jomon.runtime.CollectionUtils.asList;
import static org.echocat.jomon.testing.Assert.assertThat;
import static org.echocat.jomon.testing.BaseMatchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class AllowedHostsServletHealthInterceptorUnitTest {

    @Test
    public void testWithNoExcludesOrIncludes() throws Exception {
        final AllowedHostsServletHealthInterceptor interceptor = interceptor();
        assertThat(interceptor.isRecordAllowed(requestFor("7.7.7.7"), mock(ScopeMapping.class), null), is(true));
        assertThat(interceptor.isRecordAllowed(requestFor("6.6.6.6"), mock(ScopeMapping.class), null), is(true));
        assertThat(interceptor.isRecordAllowed(requestFor("6666:6666:6666:6666:6666:6666:6666:7777"), mock(ScopeMapping.class), null), is(true));
        assertThat(interceptor.isRecordAllowed(requestFor("6666:6666:6666:6666:6666:6666:6666:6677"), mock(ScopeMapping.class), null), is(true));
    }

    @Test
    public void testWithExcludesOnly() throws Exception {
        final AllowedHostsServletHealthInterceptor interceptor = interceptor();
        interceptor.setExcludesPattern("6.6.6.6,6666:6666:6666:6666:6666:6666:6666:6666/8");
        assertThat(interceptor.isRecordAllowed(requestFor("7.7.7.7"), mock(ScopeMapping.class), null), is(true));
        assertThat(interceptor.isRecordAllowed(requestFor("6.6.6.6"), mock(ScopeMapping.class), null), is(false));
        assertThat(interceptor.isRecordAllowed(requestFor("6666:6666:6666:6666:6666:6666:6666:7777"), mock(ScopeMapping.class), null), is(true));
        assertThat(interceptor.isRecordAllowed(requestFor("6666:6666:6666:6666:6666:6666:6666:6677"), mock(ScopeMapping.class), null), is(false));
    }

    @Test
    public void testWithIncludesOnly() throws Exception {
        final AllowedHostsServletHealthInterceptor interceptor = interceptor();
        interceptor.setIncludesPattern("7.7.7.7,7777:7777:7777:7777:7777:7777:7777:7777/8");
        assertThat(interceptor.isRecordAllowed(requestFor("7.7.7.7"), mock(ScopeMapping.class), null), is(true));
        assertThat(interceptor.isRecordAllowed(requestFor("6.6.6.6"), mock(ScopeMapping.class), null), is(false));
        assertThat(interceptor.isRecordAllowed(requestFor("7777:7777:7777:7777:7777:7777:7777:7766"), mock(ScopeMapping.class), null), is(true));
        assertThat(interceptor.isRecordAllowed(requestFor("7777:7777:7777:7777:7777:7777:7777:6666"), mock(ScopeMapping.class), null), is(false));
    }

    @Test
    public void testWithIncludesAndExcludes() throws Exception {
        final AllowedHostsServletHealthInterceptor interceptor = interceptor();
        interceptor.setExcludesPattern("7777:7777:7777:7777:7777:7777:7777:6666/16");
        interceptor.setIncludesPattern("7.7.7.7,7777:7777:7777:7777:7777:7777:7777:7777/24");
        assertThat(interceptor.isRecordAllowed(requestFor("7.7.7.7"), mock(ScopeMapping.class), null), is(true));
        assertThat(interceptor.isRecordAllowed(requestFor("6.6.6.6"), mock(ScopeMapping.class), null), is(false));
        assertThat(interceptor.isRecordAllowed(requestFor("7777:7777:7777:7777:7777:7777:7766:6666"), mock(ScopeMapping.class), null), is(true));
        assertThat(interceptor.isRecordAllowed(requestFor("7777:7777:7777:7777:7777:7777:7777:6666"), mock(ScopeMapping.class), null), is(false));
    }

    @Nonnull
    private ServletRequest requestFor(@Nonnull String address) {
        final ServletRequest request = mock(ServletRequest.class);
        doReturn(address).when(request).getRemoteAddr();
        return request;
    }

    @Test
    public void testToPattern() throws Exception {
        final List<InetAddressMatcher> matchers = asList(matcher("6.6.6.6", 0), matcher("6666:6666:6666:6666:6666:6666:6666:6666", 48), matcher("1.1.1.1", 16));
        assertThat(interceptor().toPattern(matchers), is("6.6.6.6,6666:6666:6666:6666:6666:6666:6666:6666/48,1.1.1.1/16"));
    }

    @Test
    public void testParseMatchersBy() throws Exception {
        final Collection<InetAddressMatcher> expected = asList(matcher("6.6.6.6", 0), matcher("6666:6666:6666:6666:6666:6666:6666:6666", 48), matcher("1.1.1.1", 16));
        assertThat(interceptor().parseMatchersBy("6.6.6.6,6666:6666:6666:6666:6666:6666:6666:6666/48,1.1.1.1/16"), is(expected));
    }

    @Nonnull
    protected static AllowedHostsServletHealthInterceptor interceptor() {
        return new AllowedHostsServletHealthInterceptor();
    }

}
