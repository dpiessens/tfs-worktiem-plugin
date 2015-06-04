package com.dpiessens;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for SerializableIssueMention class
 * Created by dan.piessens on 6/2/2015.
 */
public class SerializableIssueMentionTest {

    @Test
    public void testGetId() throws Exception {

        SerializableIssueMention issueMention = new SerializableIssueMention("1", "http://myurl.com/1");

        assertEquals("1", issueMention.getId());
    }

    @Test
    public void testGetUrl() throws Exception {

        SerializableIssueMention issueMention = new SerializableIssueMention("1", "http://myurl.com/1");

        assertEquals("http://myurl.com/1", issueMention.getUrl());
    }

    @Test
    public void testEquals_whenObjectsEqual_returnsTrue() throws Exception {

        SerializableIssueMention issueMention1 = new SerializableIssueMention("1", "http://myurl.com/1");
        SerializableIssueMention issueMention2 = new SerializableIssueMention("1", "http://myurl.com/1");

        assertTrue(issueMention1.equals(issueMention2));
    }

    @Test
    public void testEquals_whenObjectsNotEqualWithData_returnsFalse() throws Exception {

        SerializableIssueMention issueMention1 = new SerializableIssueMention("1", "http://myurl.com/1");
        SerializableIssueMention issueMention2 = new SerializableIssueMention("2", "http://myurl.com/2");

        assertFalse(issueMention1.equals(issueMention2));
    }

    @Test
    public void testEquals_whenObjectsNotSameType_returnsFalse() throws Exception {

        SerializableIssueMention issueMention1 = new SerializableIssueMention("1", "http://myurl.com/1");
        String item2 = "Hello";

        assertFalse(issueMention1.equals(item2));
    }

    @Test
    public void testHashCode() throws Exception {

        SerializableIssueMention issueMention1 = new SerializableIssueMention("1", "http://myurl.com/1");
        SerializableIssueMention issueMention2 = new SerializableIssueMention("1", "http://myurl.com/1");

        int hashCode1 = issueMention1.hashCode();
        int hashCode2 = issueMention2.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    public void testToString() throws Exception {

        SerializableIssueMention issueMention = new SerializableIssueMention("1", "http://myurl.com/1");

        assertEquals("[1: http://myurl.com/1]", issueMention.toString());
    }
}