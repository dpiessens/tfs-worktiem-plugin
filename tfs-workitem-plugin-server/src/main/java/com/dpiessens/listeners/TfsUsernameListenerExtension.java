package com.dpiessens.listeners;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.MainConfigProcessor;
import jetbrains.buildServer.serverSide.ServerExtension;
import org.jdom.Element;

/**
 * The main listener entry point for the extension
 * Created by dan.piessens on 6/2/2015.
 */
public class TfsUsernameListenerExtension implements ServerExtension, MainConfigProcessor {

    private static final Logger LOG = Logger.getInstance(TfsUsernameListenerExtension.class.getName());

    // Config setting names
    private static final String LISTENER_CONFIG_ELEMENT = "tfsUserMapper";
    private static final String DOMAIN_ATTRIBUTE = "domain";

    private final UsernameMapperConfig usernameMapperConfig;

    /**
     * Creates a new instance of the TfsUsernameListenerExtension class
     *
     * @param usernameMapperConfig The user mapper configuration interface
     */
    public TfsUsernameListenerExtension(UsernameMapperConfig usernameMapperConfig) {
        this.usernameMapperConfig = usernameMapperConfig;
    }

    /**
     * Reads the configuration from the main configuration file.
     *
     * @param element The root element of the configuration.
     */
    public void readFrom(Element element) {
        LOG.debug("Reading configuration from the main configuration file.");

        Element listenerElement = element.getChild(LISTENER_CONFIG_ELEMENT);
        if (listenerElement != null && listenerElement.getAttribute(DOMAIN_ATTRIBUTE) != null) {

            String domainName = listenerElement.getAttributeValue(DOMAIN_ATTRIBUTE);
            LOG.debug(String.format("Setting domain name from config value: %s", domainName));
            this.usernameMapperConfig.setDomainName(domainName);
        } else {
            LOG.debug("Main configuration was not not found");
        }
    }

    /**
     * Writes the configuration back to the main configuration file.
     *
     * @param element The root element of the configuration.
     */
    public void writeTo(Element element) {
        LOG.debug("Writing configuration to the main configuration file.");
        Element el = new Element(LISTENER_CONFIG_ELEMENT);
        String domainName = this.usernameMapperConfig.getDomainName();

        el.setAttribute(DOMAIN_ATTRIBUTE, domainName);
        element.addContent(el);
    }

}
