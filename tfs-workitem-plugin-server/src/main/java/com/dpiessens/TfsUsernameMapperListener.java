package com.dpiessens;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.MainConfigProcessor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.AuthModule;
import jetbrains.buildServer.serverSide.auth.AuthModuleType;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import jetbrains.buildServer.users.*;
import jetbrains.buildServer.vcs.VcsSupportConfig;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * A factory class that registers the user model
 * Created by dan.piessens on 4/3/2015.
 */
    public class TfsUsernameMapperListener implements UserModelListener, MainConfigProcessor {

    private static final Logger LOG = Logger.getInstance(TfsUsernameMapperListener.class.getName());
    private static final String TFS_ROOT_NAME = "tfs";

    // Config setting names
    private static final String LISTENER_CONFIG_ELEMENT = "tfsUserMapper";
    private static final String DOMAIN_ATTRIBUTE = "domain";

    private final SBuildServer server;
    private String domainId;

    /**
     * Creates a new TfsUsernameMapperListener instance.
     * @param server The server interface for the system.
     */
    public TfsUsernameMapperListener(SBuildServer server) {
        this.server = server;
        this.domainId = "";
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
     * Reads the configuration from the main configuration file.
     * @param element The root element of the configuration.
     */
    public void readFrom(Element element) {
        LOG.debug("Reading configuration from the main configuration file.");

        Element listenerElement = element.getChild(LISTENER_CONFIG_ELEMENT);
        if (listenerElement != null && listenerElement.getAttribute(DOMAIN_ATTRIBUTE) != null) {

            String domainName = listenerElement.getAttributeValue(DOMAIN_ATTRIBUTE);
            LOG.debug(String.format("Setting domain name from config value: %s", domainName));
            this.domainId = domainName;
        }
        else {
            LOG.debug("Main configuration was not not found");
        }
    }

    /**
     * Writes the configuration back to the main configuration file.
     * @param element The root element of the configuration.
     */
    public void writeTo(Element element) {
        LOG.debug("Writing configuration to the main configuration file.");
        Element el = new Element(LISTENER_CONFIG_ELEMENT);
        String domainName = getDomainName();

        el.setAttribute(DOMAIN_ATTRIBUTE, domainName);
        element.addContent(el);
    }

    /**
     * Checks for the TFS domain mapping for the user account
     * @param user The user account.
     */
    private void checkForTfsMapping(User user) {

        String localDomain = getDomainName();
        if (localDomain.isEmpty()) {
            LOG.debug("Domain was not set by a setting, please set it in the main config.");
            return;
        }

        SUser serverUser = this.server.getUserModel().findUserById(user.getId());

        if (serverUser == null) {
            LOG.warn(String.format("Could not find server user with ID: %d", user.getId()));
            return;
        }

        List<VcsUsernamePropertyKey> vcsUsernameProperties = serverUser.getVcsUsernameProperties();
        String domainUser = String.format("%s\\%s", localDomain, user.getUsername());

        for (VcsUsernamePropertyKey vcsUsernameProperty : vcsUsernameProperties) {
            if (vcsUsernameProperty.getVcsName().equals(TFS_ROOT_NAME)) {

                String currentMapping = user.getPropertyValue(vcsUsernameProperty);

                if (currentMapping == null) {
                    // Set the username and update
                    LOG.info(String.format("Updating TFS source mapping for user '%s' to domain mapping: %s", user.getUsername(), domainUser));
                    serverUser.setUserProperty(vcsUsernameProperty, domainUser);
                }

                return;
            }
        }

        // No mapping was found add it to the system
        LOG.info(String.format("Adding TFS source mapping for user '%s' to domain mapping: %s", user.getUsername(), domainUser));

        VcsSupportConfig tfsRoot = this.server.getVcsManager().findVcsByName(TFS_ROOT_NAME);
        if (tfsRoot != null) {
            LOG.debug("Found TFS root config, setting property");
            VcsUsernamePropertyKey key = new VcsUsernamePropertyKey(tfsRoot);
            serverUser.setUserProperty(key, domainUser);
        }
    }

    /**
     * Attempts to get the domain name either from the setting or the authentication module if configured.
     * @return The domain name if located.
     */
    private String getDomainName() {

        if (this.domainId != null && !this.domainId.isEmpty()) {
            LOG.debug(String.format("Domain is set in setting as: %s", this.domainId));
            return this.domainId;
        }

        LoginConfiguration loginConfiguration = this.server.getLoginConfiguration();
        AuthModuleType authModuleType = loginConfiguration.findAuthModuleTypeByName("NT-Domain");
        if (authModuleType == null) {
            LOG.debug("Cannot locate NT-Domain auth module type");
            return "";
        }

        for (AuthModule<AuthModuleType> module : loginConfiguration.getConfiguredAuthModules(null)) {
            if (authModuleType.equals(module.getType())) {

                LOG.debug("Attempting to get setting from NT DOMAIN auth module");
                Map<String, String> properties = module.getProperties();
                final String domainKey = "defaultDomain";

                if (properties.containsKey(domainKey)) {
                    String domainValue = properties.get(domainKey);

                    LOG.debug(String.format("Setting domain ID to: %s", domainValue));
                    this.domainId = domainValue;
                    return domainValue;
                }

                break;
            }
        }

        return "";
    }
}
