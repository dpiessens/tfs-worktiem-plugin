package com.dpiessens;

import jetbrains.buildServer.issueTracker.IssueFetcher;
import jetbrains.buildServer.issueTracker.IssueProvider;
import jetbrains.buildServer.util.cache.EhCacheUtil;
import jetbrains.buildServer.vcs.VcsManager;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Unit tests for the TfsIssueProviderFactory class
 * Created by dan.piessens on 6/2/2015.
 */
public class TfsIssueProviderFactoryTest {

    @Test
    public void testCreateProvider() throws Exception {

        final IssueFetcher fetcher = mock(IssueFetcher.class);
        final TfsDataProvider dataProvider = mock(TfsDataProvider.class);
        final VcsManager vcsManager = mock(VcsManager.class);
        final EhCacheUtil cacheUtil = mock(EhCacheUtil.class);

        TfsIssueProviderFactory factory = new TfsIssueProviderFactory(fetcher, dataProvider, vcsManager, cacheUtil);

        IssueProvider provider = factory.createProvider();

        assertNotNull(provider);

        verifyZeroInteractions(fetcher);
        verifyZeroInteractions(dataProvider);
        verifyZeroInteractions(vcsManager);
    }
}