package com.dpiessens.listeners;

/**
 * An interface for the username mapping configuration handler.
 * Created by dan.piessens on 5/28/2015.
 */
public interface UsernameMapperConfig {

    /**
     * Gets the domain for mapping users
     *
     * @return The domain for mapping users
     */
    String getDomainName();

    /**
     * Sets the domain name in the configuration
     *
     * @param domainName The set domain name.
     */
    void setDomainName(String domainName);
}
