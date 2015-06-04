package com.dpiessens.listeners;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.User;
import jetbrains.buildServer.users.UserModelListener;
import jetbrains.buildServer.users.VcsUsernamePropertyKey;
import jetbrains.buildServer.vcs.VcsSupportConfig;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A factory class that registers the user model
 * Created by dan.piessens on 4/3/2015.
 */
public class TfsUsernameMapperListener implements UserModelListener {

    private static final Logger LOG = Logger.getInstance(TfsUsernameMapperListener.class.getName());
    private static final String TFS_ROOT_NAME = "tfs";

    private final SBuildServer server;
    private final UsernameMapperConfig usernameMapperConfig;

    /**
     * Creates a new TfsUsernameMapperListener instance.
     * @param server The server interface for the system.
     * @param usernameMapperConfig  The interface for mapping users
     */
    public TfsUsernameMapperListener(SBuildServer server, UsernameMapperConfig usernameMapperConfig) {
        this.server = server;
        this.usernameMapperConfig = usernameMapperConfig;
    }

    /**
     * Called when the user account is created.
     * @param user The user that was created.
     */
    public void userAccountCreated(@NotNull User user) {
        LOG.debug("Calling TFS Username Mapper listener for a new account");
        this.checkForTfsMapping(user);
    }

    /**
     * Called when a user account is modified.
     * @param user The user account that was modified.
     */
    public void userAccountChanged(User user) {
        LOG.debug("Calling TFS Username Mapper listener for modifying an account");
        this.checkForTfsMapping(user);
    }

    /**
     * Called when a user account is removed.
     * @param user The user account being removed.
     */
    public void userAccountRemoved(User user) {
        // Not used
    }

    /**
     * Registers the plugin with the system
     */
    public void register() {
        LOG.info("Registering TFS Username Mapper extension with user model listener");
        this.server.getUserModel().addListener(this);
    }

    /**
     * Checks for the TFS domain mapping for the user account
     * @param user The user account.
     */
    private void checkForTfsMapping(User user) {

        String localDomain = this.usernameMapperConfig.getDomainName();
        if (localDomain == null || localDomain.isEmpty()) {
            LOG.debug("Domain was not set by a setting, please set it in the main config.");
            return;
        }

        SUser serverUser = this.server.getUserModel().findUserById(user.getId());

        if (serverUser == null) {
            LOG.warn(String.format("Could not find server user with ID: %d", user.getId()));
            return;
        }

        List<VcsUsernamePropertyKey> vcsUsernameProperties = serverUser.getVcsUsernameProperties();
        String userName = user.getUsername();
        String domainUser = String.format("%s\\%s", localDomain, userName);

        for (VcsUsernamePropertyKey vcsUsernameProperty : vcsUsernameProperties) {
            if (vcsUsernameProperty.getVcsName().equals(TFS_ROOT_NAME)) {

                String currentMapping = user.getPropertyValue(vcsUsernameProperty);

                if (currentMapping == null) {
                    // Set the username and update
                    LOG.info(String.format("Updating TFS source mapping for user '%s' to domain mapping: %s", userName, domainUser));
                    serverUser.setUserProperty(vcsUsernameProperty, domainUser);
                }

                return;
            }
        }

        // No mapping was found add it to the system
        LOG.info(String.format("Adding TFS source mapping for user '%s' to domain mapping: %s", userName, domainUser));

        VcsSupportConfig tfsRoot = this.server.getVcsManager().findVcsByName(TFS_ROOT_NAME);
        if (tfsRoot != null) {
            LOG.debug("Found TFS root config, setting property");
            VcsUsernamePropertyKey key = new VcsUsernamePropertyKey(tfsRoot);
            serverUser.setUserProperty(key, domainUser);
        }
    }
}
