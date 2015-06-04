package com.dpiessens;

import jetbrains.buildServer.issueTracker.IssueData;
import jetbrains.buildServer.issueTracker.IssueFetcher;
import jetbrains.buildServer.issueTracker.IssueMention;
import jetbrains.buildServer.util.cache.EhCacheUtil;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.VcsManager;
import jetbrains.buildServer.vcs.VcsModification;
import jetbrains.buildServer.vcs.VcsRootInstance;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the TfsIssueProvider
 * Created by dan.piessens on 6/2/2015.
 */
public class TfsIssueProviderTest {

    @Test
    public void testUseIdPrefix() throws Exception {

        final IssueFetcher fetcher = mock(IssueFetcher.class);
        final TfsDataProvider dataProvider = mock(TfsDataProvider.class);
        final VcsManager vcsManager = mock(VcsManager.class);
        final EhCacheUtil cacheUtil = mock(EhCacheUtil.class);

        TfsIssueProvider provider = new TfsIssueProvider(fetcher, dataProvider, vcsManager, cacheUtil);

        Boolean usePrefix = provider.useIdPrefix();

        assertFalse(usePrefix);

        verifyZeroInteractions(fetcher);
        verifyZeroInteractions(dataProvider);
        verifyZeroInteractions(vcsManager);
    }

    @Test
    public void testGetProviderType() throws Exception {

        final IssueFetcher fetcher = mock(IssueFetcher.class);
        final TfsDataProvider dataProvider = mock(TfsDataProvider.class);
        final VcsManager vcsManager = mock(VcsManager.class);
        final EhCacheUtil cacheUtil = mock(EhCacheUtil.class);

        TfsIssueProvider provider = new TfsIssueProvider(fetcher, dataProvider, vcsManager, cacheUtil);

        String providerType = provider.getType();

        assertEquals("tfs-workitems", providerType);

        verifyZeroInteractions(fetcher);
        verifyZeroInteractions(dataProvider);
        verifyZeroInteractions(vcsManager);
    }

    @Test
    public void testIsBatchFetchSupported() throws Exception {

        final IssueFetcher fetcher = mock(IssueFetcher.class);
        final TfsDataProvider dataProvider = mock(TfsDataProvider.class);
        final VcsManager vcsManager = mock(VcsManager.class);
        final EhCacheUtil cacheUtil = mock(EhCacheUtil.class);

        TfsIssueProvider provider = new TfsIssueProvider(fetcher, dataProvider, vcsManager, cacheUtil);

        Boolean usePrefix = provider.isBatchFetchSupported();

        assertTrue(usePrefix);

        verifyZeroInteractions(fetcher);
        verifyZeroInteractions(dataProvider);
        verifyZeroInteractions(vcsManager);
    }

    @Test
    public void testIsHasRelatedIssues_whenRevisionIsPersonal_returnsFalse() throws Exception {

        final IssueFetcher fetcher = mock(IssueFetcher.class);
        final TfsDataProvider dataProvider = mock(TfsDataProvider.class);
        final VcsManager vcsManager = mock(VcsManager.class);
        final EhCacheUtil cacheUtil = mock(EhCacheUtil.class);

        TfsIssueProvider provider = new TfsIssueProvider(fetcher, dataProvider, vcsManager, cacheUtil);

        final VcsModification modification = mock(VcsModification.class);
        when(modification.isPersonal()).thenReturn(true);

        Boolean hasRelatedIssues = provider.isHasRelatedIssues(modification);

        assertFalse(hasRelatedIssues);

        verify(modification).isPersonal();
        verifyNoMoreInteractions(modification);

        verifyZeroInteractions(fetcher);
        verifyZeroInteractions(dataProvider);
        verifyZeroInteractions(vcsManager);
    }

    @Test
    public void testIsHasRelatedIssues_whenVersionIsNotANumber_returnsFalse() throws Exception {

        final IssueFetcher fetcher = mock(IssueFetcher.class);
        final TfsDataProvider dataProvider = mock(TfsDataProvider.class);
        final VcsManager vcsManager = mock(VcsManager.class);
        final EhCacheUtil cacheUtil = mock(EhCacheUtil.class);

        TfsIssueProvider provider = new TfsIssueProvider(fetcher, dataProvider, vcsManager, cacheUtil);

        final VcsModification modification = mock(VcsModification.class);
        when(modification.isPersonal()).thenReturn(false);
        when(modification.getVersion()).thenReturn("e05dce6d-7e0d-49ad-ac14-62e5441c5ac7");

        Boolean hasRelatedIssues = provider.isHasRelatedIssues(modification);

        assertFalse(hasRelatedIssues);

        verify(modification).isPersonal();
        verify(modification).getVersion();
        verifyNoMoreInteractions(modification);

        verifyZeroInteractions(fetcher);
        verifyZeroInteractions(dataProvider);
        verifyZeroInteractions(vcsManager);
    }

    @Test
    public void testIsHasRelatedIssues_whenVersionIsANumber_returnsTrue() throws Exception {

        final IssueFetcher fetcher = mock(IssueFetcher.class);
        final TfsDataProvider dataProvider = mock(TfsDataProvider.class);
        final VcsManager vcsManager = mock(VcsManager.class);
        final EhCacheUtil cacheUtil = mock(EhCacheUtil.class);

        TfsIssueProvider provider = new TfsIssueProvider(fetcher, dataProvider, vcsManager, cacheUtil);

        final VcsModification modification = mock(VcsModification.class);
        when(modification.isPersonal()).thenReturn(false);
        when(modification.getVersion()).thenReturn("12345");

        Boolean hasRelatedIssues = provider.isHasRelatedIssues(modification);

        assertTrue(hasRelatedIssues);

        verify(modification).isPersonal();
        verify(modification).getVersion();
        verifyNoMoreInteractions(modification);

        verifyZeroInteractions(fetcher);
        verifyZeroInteractions(dataProvider);
        verifyZeroInteractions(vcsManager);
    }

    @Test
    public void testGetRelatedIssues_whenModificationIsPersonal_returnsEmptyIssueCollection() throws Exception {

        final IssueFetcher fetcher = mock(IssueFetcher.class);
        final TfsDataProvider dataProvider = mock(TfsDataProvider.class);
        final VcsManager vcsManager = mock(VcsManager.class);
        final EhCacheUtil cacheUtil = mock(EhCacheUtil.class);

        TfsIssueProvider provider = new TfsIssueProvider(fetcher, dataProvider, vcsManager, cacheUtil);

        final VcsModification modification = mock(VcsModification.class);
        when(modification.isPersonal()).thenReturn(true);

        Collection<IssueMention> relatedIssues = provider.getRelatedIssues(modification);

        assertNotNull(relatedIssues);
        assertEquals(0, relatedIssues.size());

        verify(modification).isPersonal();
        verifyNoMoreInteractions(modification);

        verifyZeroInteractions(fetcher);
        verifyZeroInteractions(dataProvider);
        verifyZeroInteractions(vcsManager);

    }

    @Test
    public void testGetRelatedIssues_whenServerRevisionIsNotFound_returnsEmptyIssueCollection() throws Exception {

        final IssueFetcher fetcher = mock(IssueFetcher.class);
        final TfsDataProvider dataProvider = mock(TfsDataProvider.class);
        final VcsManager vcsManager = mock(VcsManager.class);
        final EhCacheUtil cacheUtil = mock(EhCacheUtil.class);

        TfsIssueProvider provider = new TfsIssueProvider(fetcher, dataProvider, vcsManager, cacheUtil);

        final VcsModification modification = mock(VcsModification.class);
        when(modification.isPersonal()).thenReturn(false);
        when(modification.getId()).thenReturn(11234L);

        when(vcsManager.findModificationById(11234L, false)).thenReturn(null);

        Collection<IssueMention> relatedIssues = provider.getRelatedIssues(modification);

        assertNotNull(relatedIssues);
        assertEquals(0, relatedIssues.size());

        verify(modification).isPersonal();
        verify(modification).getId();
        verifyNoMoreInteractions(modification);

        verifyZeroInteractions(fetcher);
        verifyZeroInteractions(dataProvider);

        verify(vcsManager).findModificationById(11234L, false);
        verifyNoMoreInteractions(vcsManager);
    }

    @Test
    public void testGetRelatedIssues_whenSourceControlIsNotTfs_returnsEmptyIssueCollection() throws Exception {

        final IssueFetcher fetcher = mock(IssueFetcher.class);
        final TfsDataProvider dataProvider = mock(TfsDataProvider.class);
        final VcsManager vcsManager = mock(VcsManager.class);
        final EhCacheUtil cacheUtil = mock(EhCacheUtil.class);

        TfsIssueProvider provider = new TfsIssueProvider(fetcher, dataProvider, vcsManager, cacheUtil);

        final VcsModification modification = mock(VcsModification.class);
        when(modification.isPersonal()).thenReturn(false);
        when(modification.getId()).thenReturn(11234L);

        final VcsRootInstance vcsRootInstance = mock(VcsRootInstance.class);
        when(vcsRootInstance.getVcsName()).thenReturn("svn");

        final SVcsModification serverModification = mock(SVcsModification.class);
        when(serverModification.getVcsRoot()).thenReturn(vcsRootInstance);

        when(vcsManager.findModificationById(11234L, false)).thenReturn(serverModification);

        Collection<IssueMention> relatedIssues = provider.getRelatedIssues(modification);

        assertNotNull(relatedIssues);
        assertEquals(0, relatedIssues.size());

        verify(modification).isPersonal();
        verify(modification).getId();

        verify(vcsRootInstance).getVcsName();

        verify(serverModification).getVcsRoot();

        verify(vcsManager).findModificationById(11234L, false);

        verifyNoMoreInteractions(vcsManager, modification, vcsRootInstance, serverModification);
        verifyZeroInteractions(dataProvider, fetcher);
    }

    @Test
    public void testGetRelatedIssues_whenVcsRevisionIsNotANumber_returnsEmptyIssueCollection() throws Exception {

        final IssueFetcher fetcher = mock(IssueFetcher.class);
        final TfsDataProvider dataProvider = mock(TfsDataProvider.class);
        final VcsManager vcsManager = mock(VcsManager.class);
        final EhCacheUtil cacheUtil = mock(EhCacheUtil.class);

        TfsIssueProvider provider = new TfsIssueProvider(fetcher, dataProvider, vcsManager, cacheUtil);

        final VcsModification modification = mock(VcsModification.class);
        when(modification.isPersonal()).thenReturn(false);
        when(modification.getId()).thenReturn(11234L);
        when(modification.getVersion()).thenReturn("c2e2b90e-954e-4489-8a38-8e0022314d4d");

        final VcsRootInstance vcsRootInstance = mock(VcsRootInstance.class);
        when(vcsRootInstance.getVcsName()).thenReturn("tfs");

        final SVcsModification serverModification = mock(SVcsModification.class);
        when(serverModification.getVcsRoot()).thenReturn(vcsRootInstance);

        when(vcsManager.findModificationById(11234L, false)).thenReturn(serverModification);

        Collection<IssueMention> relatedIssues = provider.getRelatedIssues(modification);

        assertNotNull(relatedIssues);
        assertEquals(0, relatedIssues.size());

        verify(modification).isPersonal();
        verify(modification).getId();
        verify(modification, times(2)).getVersion();

        verify(vcsRootInstance).getVcsName();

        verify(serverModification).getVcsRoot();

        verify(vcsManager).findModificationById(11234L, false);

        verifyNoMoreInteractions(vcsManager, modification, vcsRootInstance, serverModification);
        verifyZeroInteractions(dataProvider, fetcher);
    }

    @Test
    public void testGetRelatedIssues_whenVcsRevisionHasIssuesAndIsNotCached_returnsIssueCollection() throws Exception {

        final IssueFetcher fetcher = mock(IssueFetcher.class);
        final TfsDataProvider dataProvider = mock(TfsDataProvider.class);
        final VcsManager vcsManager = mock(VcsManager.class);
        final EhCacheUtil cacheUtil = mock(EhCacheUtil.class);

        TfsIssueProvider provider = new TfsIssueProvider(fetcher, dataProvider, vcsManager, cacheUtil);

        final VcsModification modification = mock(VcsModification.class);
        when(modification.isPersonal()).thenReturn(false);
        when(modification.getId()).thenReturn(11234L);
        when(modification.getVersion()).thenReturn("123");

        final VcsRootInstance vcsRootInstance = mock(VcsRootInstance.class);
        when(vcsRootInstance.getVcsName()).thenReturn("tfs");

        final SVcsModification serverModification = mock(SVcsModification.class);
        when(serverModification.getVcsRoot()).thenReturn(vcsRootInstance);

        when(vcsManager.findModificationById(11234L, false)).thenReturn(serverModification);

        when(dataProvider.getIssuesForVersion(eq(123), anyString(), any(org.apache.commons.httpclient.Credentials.class)))
                .thenReturn(new ArrayList<IssueData>() {{
                    add(new IssueData("1", "My Issue", "Done", "http://myapp.com/1", true));
                }});

        Collection<IssueMention> relatedIssues = provider.getRelatedIssues(modification);

        assertNotNull(relatedIssues);
        assertEquals(1, relatedIssues.size());

        verify(modification).isPersonal();
        verify(modification).getId();
        verify(modification).getVersion();

        verify(vcsRootInstance).getVcsName();

        verify(serverModification).getVcsRoot();

        verify(vcsManager).findModificationById(11234L, false);

        verify(dataProvider).getIssuesForVersion(eq(123), anyString(), any(org.apache.commons.httpclient.Credentials.class));

        verifyNoMoreInteractions(vcsManager, modification, vcsRootInstance, serverModification, dataProvider);
        verifyZeroInteractions(fetcher);
    }

    @Test
    public void testGetRelatedIssues_whenVcsRevisionHasIssuesAndCredsAndIsNotCached_returnsIssueCollection() throws Exception {

        final IssueFetcher fetcher = mock(IssueFetcher.class);
        final TfsDataProvider dataProvider = mock(TfsDataProvider.class);
        final VcsManager vcsManager = mock(VcsManager.class);
        final EhCacheUtil cacheUtil = mock(EhCacheUtil.class);

        TfsIssueProvider provider = new TfsIssueProvider(fetcher, dataProvider, vcsManager, cacheUtil);

        final VcsModification modification = mock(VcsModification.class);
        when(modification.isPersonal()).thenReturn(false);
        when(modification.getId()).thenReturn(11234L);
        when(modification.getVersion()).thenReturn("123");

        final VcsRootInstance vcsRootInstance = mock(VcsRootInstance.class);
        when(vcsRootInstance.getVcsName()).thenReturn("tfs");

        when(vcsRootInstance.getProperty("tfs-username")).thenReturn("bob");
        when(vcsRootInstance.getProperty("secure:tfs-password")).thenReturn("abc1234");

        final SVcsModification serverModification = mock(SVcsModification.class);
        when(serverModification.getVcsRoot()).thenReturn(vcsRootInstance);

        when(vcsManager.findModificationById(11234L, false)).thenReturn(serverModification);

        when(dataProvider.getIssuesForVersion(eq(123), anyString(), any(org.apache.commons.httpclient.Credentials.class)))
                .thenReturn(new ArrayList<IssueData>() {{
                    add(new IssueData("1", "My Issue", "Done", "http://myapp.com/1", true));
                }});


        Map<String, String> providerProps = new HashMap<String, String>() {
            {
                put("useVcsCredentials", "true");
            }
        };

        provider.setProperties(providerProps);
        Collection<IssueMention> relatedIssues = provider.getRelatedIssues(modification);

        assertNotNull(relatedIssues);
        assertEquals(1, relatedIssues.size());

        verify(modification).isPersonal();
        verify(modification).getId();
        verify(modification).getVersion();

        verify(vcsRootInstance).getVcsName();
        verify(vcsRootInstance).getProperty("tfs-username");
        verify(vcsRootInstance).getProperty("secure:tfs-password");

        verify(serverModification).getVcsRoot();

        verify(vcsManager).findModificationById(11234L, false);

        verify(dataProvider).getIssuesForVersion(eq(123), anyString(), any(org.apache.commons.httpclient.Credentials.class));

        verifyNoMoreInteractions(vcsManager, modification, vcsRootInstance, serverModification, dataProvider);
        verifyZeroInteractions(fetcher);
    }
}