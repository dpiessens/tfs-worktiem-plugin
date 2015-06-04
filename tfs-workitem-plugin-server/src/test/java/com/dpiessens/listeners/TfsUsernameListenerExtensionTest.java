package com.dpiessens.listeners;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the username listener extension
 * Created by dan.piessens on 6/2/2015.
 */
public class TfsUsernameListenerExtensionTest {

    @Test
    public void testReadFrom_whenElementExists_setsDomainName() throws Exception {

        UsernameMapperConfig usernameMapperConfig = mock(UsernameMapperConfig.class);

        TfsUsernameListenerExtension extension = new TfsUsernameListenerExtension(usernameMapperConfig);

        Element element = createXmlElement("<config><tfsUserMapper domain=\"CONFIGDOMAIN\" /></config>");

        extension.readFrom(element);

        verify(usernameMapperConfig).setDomainName("CONFIGDOMAIN");
        verifyNoMoreInteractions(usernameMapperConfig);
    }

    @Test
    public void testReadFrom_whenElementDoesNotExist_doesNothing() throws Exception {

        UsernameMapperConfig usernameMapperConfig = mock(UsernameMapperConfig.class);

        TfsUsernameListenerExtension extension = new TfsUsernameListenerExtension(usernameMapperConfig);

        Element element = createXmlElement("<config></config>");

        extension.readFrom(element);

        verifyZeroInteractions(usernameMapperConfig);
    }

    @Test
    public void testWriteTo() throws Exception {

        UsernameMapperConfig usernameMapperConfig = mock(UsernameMapperConfig.class);
        when(usernameMapperConfig.getDomainName()).thenReturn("WRITEDOMAIN");

        TfsUsernameListenerExtension extension = new TfsUsernameListenerExtension(usernameMapperConfig);

        Element element = createXmlElement("<config></config>");

        extension.writeTo(element);

        Element child = element.getChild("tfsUserMapper");

        assertNotNull(child);
        assertEquals("WRITEDOMAIN", child.getAttributeValue("domain"));

        verify(usernameMapperConfig).getDomainName();
        verifyNoMoreInteractions(usernameMapperConfig);
    }

    private Element createXmlElement(String data) throws JDOMException, IOException {
        SAXBuilder saxBuild = new SAXBuilder();
        Document document = saxBuild.build(new StringReader(data));

        return document.getRootElement();
    }
}