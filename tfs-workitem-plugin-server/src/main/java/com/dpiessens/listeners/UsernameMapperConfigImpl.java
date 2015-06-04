package com.dpiessens.listeners;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.AuthModule;
import jetbrains.buildServer.serverSide.auth.AuthModuleType;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import org.jfree.util.Log;

import java.util.Map;

/**
 * A class that implements the user configuration
 * Created by dan.piessens on 5/28/2015.
 */
public class UsernameMapperConfigImpl implements UsernameMapperConfig {

    private static final Logger LOG = Logger.getInstance(UsernameMapperConfigImpl.class.getName());

    private final SBuildServer server;
    private String domainId;

    /**
     * Creates a new TfsUsernameMapperListener instance.
     *
     * @param server The server interface for the system.
     */
    public UsernameMapperConfigImpl(SBuildServer server) {
        this.server = server;
        this.domainId = "";
    }

    /**
     * Gets the domain for mapping users
     *
     * @return The domain for mapping users
     */
    public String getDomainName() {

        if (this.domainId != null && !this.domainId.isEmpty()) {
            LOG.debug(String.format("Domain is set in setting as: %s", this.domainId));
            return this.domainId;
        }

        LoginConfiguration loginConfiguration = this.server.getLoginConfiguration();
        AuthModuleType authModuleType = loginConfiguration.findAuthModuleTypeByName("NT-Domain");
        if (authModuleType == null) {
            LOG.debug("Cannot locate NT-Domain auth module type");
            return null;
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

        return null;
    }

    /**
     * Sets the domain name in the configuration
     *
     * @param domainName The set domain name.
     */
    public void setDomainName(String domainName) {
        Log.debug(String.format("Domain name set externally to: '%s'", domainName));
        this.domainId = domainName;
    }
}
