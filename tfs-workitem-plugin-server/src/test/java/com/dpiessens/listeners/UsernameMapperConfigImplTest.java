package com.dpiessens.listeners;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.AuthModule;
import jetbrains.buildServer.serverSide.auth.AuthModuleType;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the UsernameMapperConfigImpl class
 * Created by dan.piessens on 5/28/2015.
 */
public class UsernameMapperConfigImplTest {

    @Test
    public void testGetDomainName_whenModuleNotFound_returnsNull() throws Exception {

        final SBuildServer sBuildServer = mock(SBuildServer.class);

        final LoginConfiguration loginConfiguration = mock(LoginConfiguration.class);

        when(sBuildServer.getLoginConfiguration()).thenReturn(loginConfiguration);

        when(loginConfiguration.findAuthModuleTypeByName("NT-Domain")).thenReturn(null);

        final UsernameMapperConfigImpl config = new UsernameMapperConfigImpl(sBuildServer);

        String domainName = config.getDomainName();

        assertNull(domainName);

        verify(sBuildServer).getLoginConfiguration();
        verify(loginConfiguration).findAuthModuleTypeByName("NT-Domain");

        verifyNoMoreInteractions(sBuildServer);
        verifyNoMoreInteractions(loginConfiguration);
    }

    @Test
    public void testGetDomainName_whenModuleIsFoundButNotEnabled_returnsNull() throws Exception {

        final SBuildServer sBuildServer = mock(SBuildServer.class);

        final LoginConfiguration loginConfiguration = mock(LoginConfiguration.class);

        when(sBuildServer.getLoginConfiguration()).thenReturn(loginConfiguration);

        final AuthModuleType authModule = mock(AuthModuleType.class);

        when(loginConfiguration.findAuthModuleTypeByName("NT-Domain")).thenReturn(authModule);
        when(loginConfiguration.getConfiguredAuthModules(null)).thenReturn(new ArrayList<AuthModule<AuthModuleType>>());

        final UsernameMapperConfigImpl config = new UsernameMapperConfigImpl(sBuildServer);

        String domainName = config.getDomainName();

        assertNull(domainName);

        verify(sBuildServer).getLoginConfiguration();

        verify(loginConfiguration).findAuthModuleTypeByName("NT-Domain");
        verify(loginConfiguration).getConfiguredAuthModules(null);

        verifyNoMoreInteractions(sBuildServer);
        verifyNoMoreInteractions(loginConfiguration);
        verifyZeroInteractions(authModule);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetDomainName_whenModuleIsFoundButDoesNotContainDomainKey_returnsNull() throws Exception {

        final SBuildServer sBuildServer = mock(SBuildServer.class);

        final LoginConfiguration loginConfiguration = mock(LoginConfiguration.class);

        when(sBuildServer.getLoginConfiguration()).thenReturn(loginConfiguration);

        final AuthModuleType authModuleType = mock(AuthModuleType.class);
        final AuthModule<AuthModuleType> authModule = (AuthModule<AuthModuleType>) mock(AuthModule.class);

        when(authModule.getType()).thenReturn(authModuleType);
        when(authModule.getProperties()).thenReturn(new HashMap<String, String>());

        when(loginConfiguration.findAuthModuleTypeByName("NT-Domain")).thenReturn(authModuleType);
        when(loginConfiguration.getConfiguredAuthModules(null)).thenReturn(Arrays.asList(authModule));

        final UsernameMapperConfigImpl config = new UsernameMapperConfigImpl(sBuildServer);

        String domainName = config.getDomainName();

        assertNull(domainName);

        verify(sBuildServer).getLoginConfiguration();

        verify(loginConfiguration).findAuthModuleTypeByName("NT-Domain");
        verify(loginConfiguration).getConfiguredAuthModules(null);

        verify(authModule).getType();
        verify(authModule).getProperties();

        verifyNoMoreInteractions(sBuildServer);
        verifyNoMoreInteractions(loginConfiguration);
        verifyNoMoreInteractions(authModule);
        verifyZeroInteractions(authModuleType);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetDomainName_whenModuleIsFoundAndContainsProperty_returnsValueAndCachesIt() throws Exception {

        final SBuildServer sBuildServer = mock(SBuildServer.class);

        final LoginConfiguration loginConfiguration = mock(LoginConfiguration.class);

        when(sBuildServer.getLoginConfiguration()).thenReturn(loginConfiguration);

        final AuthModuleType authModuleType = mock(AuthModuleType.class);
        final AuthModule<AuthModuleType> authModule = (AuthModule<AuthModuleType>) mock(AuthModule.class);

        when(authModule.getType()).thenReturn(authModuleType);

        Map<String, String> properties = new HashMap<String, String>() {{
            put("defaultDomain", "TESTDOMAIN");
        }};
        when(authModule.getProperties()).thenReturn(properties);

        when(loginConfiguration.findAuthModuleTypeByName("NT-Domain")).thenReturn(authModuleType);
        when(loginConfiguration.getConfiguredAuthModules(null)).thenReturn(Arrays.asList(authModule));

        final UsernameMapperConfigImpl config = new UsernameMapperConfigImpl(sBuildServer);

        String domainName = config.getDomainName();

        assertEquals("TESTDOMAIN", domainName);

        // This tests the cached call
        String domainName2 = config.getDomainName();
        assertEquals("TESTDOMAIN", domainName2);

        verify(sBuildServer, times(1)).getLoginConfiguration();

        verify(loginConfiguration, times(1)).findAuthModuleTypeByName("NT-Domain");
        verify(loginConfiguration, times(1)).getConfiguredAuthModules(null);

        verify(authModule, times(1)).getType();
        verify(authModule, times(1)).getProperties();

        verifyNoMoreInteractions(sBuildServer);
        verifyNoMoreInteractions(loginConfiguration);
        verifyNoMoreInteractions(authModule);
        verifyZeroInteractions(authModuleType);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetDomainName_whenDomainIsSetManually_returnsValueAndCachesIt() throws Exception {

        final SBuildServer sBuildServer = mock(SBuildServer.class);

        final UsernameMapperConfigImpl config = new UsernameMapperConfigImpl(sBuildServer);

        config.setDomainName("TESTDOMAIN");
        String domainName = config.getDomainName();

        assertEquals("TESTDOMAIN", domainName);

        // This tests the cached call
        String domainName2 = config.getDomainName();
        assertEquals("TESTDOMAIN", domainName2);

        verifyZeroInteractions(sBuildServer);
    }
}