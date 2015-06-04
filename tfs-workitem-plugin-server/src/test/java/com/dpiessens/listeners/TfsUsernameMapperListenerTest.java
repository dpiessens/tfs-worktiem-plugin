package com.dpiessens.listeners;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.User;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.users.VcsUsernamePropertyKey;
import jetbrains.buildServer.vcs.VcsManager;
import jetbrains.buildServer.vcs.VcsSupportCore;
import jetbrains.buildServer.vcs.impl.VcsRootImpl;
import org.junit.Test;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

/**
 * A test fixture for the TfsUsernameMapperListener class
 * Created by dan.piessens on 5/25/2015.
 */
public class TfsUsernameMapperListenerTest {

    @Test
    public void testListenerRegister() throws Exception {

        final SBuildServer sBuildServer = mock(SBuildServer.class);
        final UsernameMapperConfig mapperConfig = mock(UsernameMapperConfig.class);

        final UserModel userModel = mock(UserModel.class);

        when(sBuildServer.getUserModel()).thenReturn(userModel);

        final TfsUsernameMapperListener listener = new TfsUsernameMapperListener(sBuildServer, mapperConfig);

        listener.register();

        verify(userModel).addListener(listener);
        verify(sBuildServer).getUserModel();
        verifyZeroInteractions(mapperConfig);
    }

    @Test
    public void testUserAccountRemoved_DoesNothing() throws Exception {

        final SBuildServer sBuildServer = mock(SBuildServer.class);
        final UsernameMapperConfig mapperConfig = mock(UsernameMapperConfig.class);

        final TfsUsernameMapperListener listener = new TfsUsernameMapperListener(sBuildServer, mapperConfig);

        User user = mock(User.class);

        listener.userAccountRemoved(user);

        verifyZeroInteractions(sBuildServer);
        verifyZeroInteractions(user);
        verifyZeroInteractions(mapperConfig);
        verifyZeroInteractions(user);
    }

    @Test
    public void testUserAccountChanged_whenNoDomainIsReturned_DoesNothing() throws Exception {

        final SBuildServer sBuildServer = mock(SBuildServer.class);

        final UsernameMapperConfig mapperConfig = mock(UsernameMapperConfig.class);
        when(mapperConfig.getDomainName()).thenReturn(null);

        final TfsUsernameMapperListener listener = new TfsUsernameMapperListener(sBuildServer, mapperConfig);

        User user = mock(User.class);

        listener.userAccountChanged(user);

        verifyZeroInteractions(sBuildServer);
        verifyZeroInteractions(user);
    }

    @Test
    public void testUserAccountCreated_whenNoDomainIsReturned_DoesNothing() throws Exception {

        final SBuildServer sBuildServer = mock(SBuildServer.class);

        final UsernameMapperConfig mapperConfig = mock(UsernameMapperConfig.class);
        when(mapperConfig.getDomainName()).thenReturn(null);

        final TfsUsernameMapperListener listener = new TfsUsernameMapperListener(sBuildServer, mapperConfig);

        User user = mock(User.class);

        listener.userAccountCreated(user);

        verifyZeroInteractions(sBuildServer);
        verifyZeroInteractions(user);
    }

    @Test
    public void testUserAccountCreated_whenNoServerUserIsFound_DoesNothing() throws Exception {

        long userId = 1;
        final SBuildServer sBuildServer = mock(SBuildServer.class);

        final UsernameMapperConfig mapperConfig = mock(UsernameMapperConfig.class);
        when(mapperConfig.getDomainName()).thenReturn("MYDOMAIN");

        final UserModel userModel = mock(UserModel.class);

        when(sBuildServer.getUserModel()).thenReturn(userModel);

        when(userModel.findUserById(userId)).thenReturn(null);

        final TfsUsernameMapperListener listener = new TfsUsernameMapperListener(sBuildServer, mapperConfig);

        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);

        listener.userAccountCreated(user);

        verify(sBuildServer).getUserModel();
        verify(userModel).findUserById(userId);
    }

    @Test
    public void testUserAccountCreated_whenUserMappingIsFound_DoesNothing() throws Exception {

        long userId = 1;
        final SBuildServer sBuildServer = mock(SBuildServer.class);

        final UsernameMapperConfig mapperConfig = mock(UsernameMapperConfig.class);
        when(mapperConfig.getDomainName()).thenReturn("MYDOMAIN");

        final UserModel userModel = mock(UserModel.class);

        when(sBuildServer.getUserModel()).thenReturn(userModel);

        final VcsUsernamePropertyKey propertyKey = new VcsUsernamePropertyKey(new VcsRootImpl(1, "tfs"));
        final SUser sUser = mock(SUser.class);
        when(sUser.getVcsUsernameProperties()).thenReturn(new ArrayList<VcsUsernamePropertyKey>() {{
            add(propertyKey);
        }});

        when(userModel.findUserById(userId)).thenReturn(sUser);

        final TfsUsernameMapperListener listener = new TfsUsernameMapperListener(sBuildServer, mapperConfig);

        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("bob");
        when(user.getPropertyValue(propertyKey)).thenReturn("MYDOMAIN\\bob");

        listener.userAccountCreated(user);

        verify(sBuildServer).getUserModel();
        verify(userModel).findUserById(userId);
        verify(sUser).getVcsUsernameProperties();
        verify(user).getUsername();

        verifyNoMoreInteractions(sBuildServer);
        verifyNoMoreInteractions(userModel);
        verifyNoMoreInteractions(sUser);
    }

    @Test
    public void testUserAccountCreated_whenUserMappingIsFoundButEmpty_UpdatesMapping() throws Exception {

        long userId = 1;
        final SBuildServer sBuildServer = mock(SBuildServer.class);

        final UsernameMapperConfig mapperConfig = mock(UsernameMapperConfig.class);
        when(mapperConfig.getDomainName()).thenReturn("MYDOMAIN");

        final UserModel userModel = mock(UserModel.class);

        when(sBuildServer.getUserModel()).thenReturn(userModel);

        final VcsUsernamePropertyKey propertyKey = new VcsUsernamePropertyKey(new VcsRootImpl(1, "tfs"));
        final SUser sUser = mock(SUser.class);
        when(sUser.getVcsUsernameProperties()).thenReturn(new ArrayList<VcsUsernamePropertyKey>() {{
            add(propertyKey);
        }});

        when(userModel.findUserById(userId)).thenReturn(sUser);

        final TfsUsernameMapperListener listener = new TfsUsernameMapperListener(sBuildServer, mapperConfig);

        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("bob");
        when(user.getPropertyValue(propertyKey)).thenReturn(null);

        listener.userAccountCreated(user);

        verify(sBuildServer).getUserModel();
        verify(userModel).findUserById(userId);
        verify(sUser).getVcsUsernameProperties();
        verify(user).getUsername();
        verify(sUser).setUserProperty(propertyKey, "MYDOMAIN\\bob");

        verifyNoMoreInteractions(sBuildServer);
        verifyNoMoreInteractions(userModel);
        verifyNoMoreInteractions(sUser);
    }

    @Test
    public void testUserAccountCreated_whenUserMappingIsNotFound_CreatesMapping() throws Exception {

        long userId = 1;
        final SBuildServer sBuildServer = mock(SBuildServer.class);

        final UsernameMapperConfig mapperConfig = mock(UsernameMapperConfig.class);
        when(mapperConfig.getDomainName()).thenReturn("MYDOMAIN");

        final UserModel userModel = mock(UserModel.class);

        final VcsSupportCore supportConfig = mock(VcsSupportCore.class);
        when(supportConfig.getName()).thenReturn("vcs");

        final VcsManager vcsManager = mock(VcsManager.class);
        when(vcsManager.findVcsByName("tfs")).thenReturn(supportConfig);

        when(sBuildServer.getUserModel()).thenReturn(userModel);
        when(sBuildServer.getVcsManager()).thenReturn(vcsManager);

        final SUser sUser = mock(SUser.class);
        when(sUser.getVcsUsernameProperties()).thenReturn(new ArrayList<VcsUsernamePropertyKey>());

        when(userModel.findUserById(userId)).thenReturn(sUser);

        final TfsUsernameMapperListener listener = new TfsUsernameMapperListener(sBuildServer, mapperConfig);

        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("bob");

        listener.userAccountCreated(user);

        verify(sBuildServer).getUserModel();
        verify(sBuildServer).getVcsManager();

        verify(userModel).findUserById(userId);
        verify(sUser).getVcsUsernameProperties();
        verify(user).getUsername();
        verify(sUser).setUserProperty(any(VcsUsernamePropertyKey.class), eq("MYDOMAIN\\bob"));

        verify(vcsManager).findVcsByName("tfs");

        verifyNoMoreInteractions(sBuildServer);
        verifyNoMoreInteractions(userModel);
        verifyNoMoreInteractions(sUser);
        verifyNoMoreInteractions(vcsManager);
    }

    @Test
    public void testUserAccountCreated_whenUserVcsMappingIsNotFound_CreatesMapping() throws Exception {

        long userId = 1;
        final SBuildServer sBuildServer = mock(SBuildServer.class);

        final UsernameMapperConfig mapperConfig = mock(UsernameMapperConfig.class);
        when(mapperConfig.getDomainName()).thenReturn("MYDOMAIN");

        final UserModel userModel = mock(UserModel.class);

        final VcsSupportCore supportConfig = mock(VcsSupportCore.class);
        when(supportConfig.getName()).thenReturn("foo");

        final VcsManager vcsManager = mock(VcsManager.class);
        when(vcsManager.findVcsByName("tfs")).thenReturn(supportConfig);

        when(sBuildServer.getUserModel()).thenReturn(userModel);
        when(sBuildServer.getVcsManager()).thenReturn(vcsManager);

        final SUser sUser = mock(SUser.class);
        when(sUser.getVcsUsernameProperties()).thenReturn(new ArrayList<VcsUsernamePropertyKey>());

        when(userModel.findUserById(userId)).thenReturn(sUser);

        final TfsUsernameMapperListener listener = new TfsUsernameMapperListener(sBuildServer, mapperConfig);

        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("bob");

        listener.userAccountCreated(user);

        verify(sBuildServer).getUserModel();
        verify(sBuildServer).getVcsManager();

        verify(userModel).findUserById(userId);
        verify(sUser).getVcsUsernameProperties();
        verify(user).getUsername();
        verify(sUser).setUserProperty(any(VcsUsernamePropertyKey.class), eq("MYDOMAIN\\bob"));

        verify(vcsManager).findVcsByName("tfs");

        verifyNoMoreInteractions(sBuildServer);
        verifyNoMoreInteractions(userModel);
        verifyNoMoreInteractions(sUser);
        verifyNoMoreInteractions(vcsManager);
    }
}