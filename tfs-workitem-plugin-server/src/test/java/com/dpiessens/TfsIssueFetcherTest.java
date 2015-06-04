package com.dpiessens;

import com.microsoft.tfs.core.httpclient.auth.InvalidCredentialsException;
import jetbrains.buildServer.issueTracker.IssueData;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.cache.EhCacheUtil;
import jetbrains.buildServer.util.cache.ResetCacheRegister;
import org.apache.commons.httpclient.Credentials;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the TfsIssueFetcher class.
 * Created by dan.piessens on 6/2/2015.
 */
public class TfsIssueFetcherTest {

    static final String HOST = "http://tfs.com";

    @Test
    public void testGetIssue_whenIssueExists_returnsData() throws Exception {

        EhCacheUtil cacheUtil = this.createCacheUtil();
        Credentials credentials = mock(Credentials.class);

        IssueData issueData = new IssueData("1", "MyIssue", "Done", HOST, true);

        TfsDataProvider tfsDataProvider = mock(TfsDataProvider.class);
        when(tfsDataProvider.getIssueById(1, HOST, credentials)).thenReturn(issueData);

        TfsIssueFetcher fetcher = new TfsIssueFetcher(cacheUtil, tfsDataProvider);

        IssueData result = fetcher.getIssue(HOST, "1", credentials);

        assertNotNull(result);
        assertSame(issueData, result);

        verifyZeroInteractions(credentials);
    }

    @Test(expected = NumberFormatException.class)
    public void testGetIssue_whenIssueIsNotANumber_throwsException() throws Exception {

        EhCacheUtil cacheUtil = this.createCacheUtil();
        Credentials credentials = mock(Credentials.class);

        TfsDataProvider tfsDataProvider = mock(TfsDataProvider.class);
        TfsIssueFetcher fetcher = new TfsIssueFetcher(cacheUtil, tfsDataProvider);

        try {
            fetcher.getIssue(HOST, "foo", credentials);
        } catch (NumberFormatException e) {

            verifyZeroInteractions(tfsDataProvider, credentials);
            throw e;
        }
    }

    @Test(expected = Exception.class)
    public void testGetIssue_whenIssueIsNotFound_throwsException() throws Exception {

        EhCacheUtil cacheUtil = this.createCacheUtil();
        Credentials credentials = mock(Credentials.class);

        TfsDataProvider tfsDataProvider = mock(TfsDataProvider.class);
        when(tfsDataProvider.getIssueById(1, HOST, credentials)).thenReturn(null);

        TfsIssueFetcher fetcher = new TfsIssueFetcher(cacheUtil, tfsDataProvider);

        try {
            fetcher.getIssue(HOST, "2", credentials);
        } catch (NumberFormatException e) {

            assertEquals("Could not find issue in TFS Id: 2", e.getMessage());

            verify(tfsDataProvider).getIssueById(1, HOST, credentials);

            verifyNoMoreInteractions(tfsDataProvider);
            verifyZeroInteractions(tfsDataProvider, credentials);
            throw e;
        }
    }

    @Test
    public void testGetIssuesInBatch_whenIssueExists_returnsData() throws Exception {
        EhCacheUtil cacheUtil = this.createCacheUtil();
        Credentials credentials = mock(Credentials.class);

        IssueData issueData3 = new IssueData("3", "MyIssue 3", "Done", HOST, true);
        IssueData issueData4 = new IssueData("4", "MyIssue 4", "Done", HOST, true);
        Collection<IssueData> issueList = Arrays.asList(issueData3, issueData4);

        TfsDataProvider tfsDataProvider = mock(TfsDataProvider.class);
        when(tfsDataProvider.getIssues(argThat(new IsListOf(Arrays.asList(4, 5))), eq(HOST), eq(credentials))).thenReturn(issueList);

        TfsIssueFetcher fetcher = new TfsIssueFetcher(cacheUtil, tfsDataProvider);

        Collection<IssueData> result = fetcher.getIssuesInBatch(HOST, Arrays.asList("4", "5"), credentials);

        assertNotNull(result);
        assertArrayEquals(issueList.toArray(), result.toArray());

        verifyZeroInteractions(credentials);
    }

    @Test
    public void testGetIssuesInBatch_whenIssueIsNotNumber_returnsDataExcludingInvalid() throws Exception {
        EhCacheUtil cacheUtil = this.createCacheUtil();
        Credentials credentials = mock(Credentials.class);

        IssueData issueData6 = new IssueData("6", "MyIssue 6", "Done", HOST, true);
        Collection<IssueData> issueList = Arrays.asList(issueData6);

        TfsDataProvider tfsDataProvider = mock(TfsDataProvider.class);
        when(tfsDataProvider.getIssues(argThat(new IsListOf(Arrays.asList(6))), eq(HOST), eq(credentials))).thenReturn(issueList);

        TfsIssueFetcher fetcher = new TfsIssueFetcher(cacheUtil, tfsDataProvider);

        Collection<IssueData> result = fetcher.getIssuesInBatch(HOST, Arrays.asList("6", "foo"), credentials);

        assertNotNull(result);
        assertArrayEquals(issueList.toArray(), result.toArray());

        verifyZeroInteractions(credentials);
    }

    @Test
    public void testGetIssuesInBatch_whenretrieveErrors_returnsList() throws Exception {
        EhCacheUtil cacheUtil = this.createCacheUtil();
        Credentials credentials = mock(Credentials.class);

        TfsDataProvider tfsDataProvider = mock(TfsDataProvider.class);
        when(tfsDataProvider.getIssues(argThat(new IsListOf(Arrays.asList(8, 9))), eq(HOST), eq(credentials)))
                .thenThrow(new InvalidCredentialsException("Boo!"));

        TfsIssueFetcher fetcher = new TfsIssueFetcher(cacheUtil, tfsDataProvider);

        Collection<IssueData> result = fetcher.getIssuesInBatch(HOST, Arrays.asList("8", "9"), credentials);

        assertNotNull(result);

        verifyZeroInteractions(credentials);
    }

    @Test
    public void testGetUrl_whenHostContainsTrailingSlash_returnsFormattedValue() throws Exception {

        EhCacheUtil cacheUtil = this.createCacheUtil();
        TfsDataProvider tfsDataProvider = mock(TfsDataProvider.class);

        TfsIssueFetcher fetcher = new TfsIssueFetcher(cacheUtil, tfsDataProvider);

        String result = fetcher.getUrl("https://tfs.myhost.com/", "1");

        assertEquals("https://tfs.myhost.com/1", result);

        verifyZeroInteractions(tfsDataProvider);
    }

    @Test
    public void testGetUrl_whenHostDoesNotContainTrailingSlash_returnsFormattedValue() throws Exception {

        EhCacheUtil cacheUtil = this.createCacheUtil();
        TfsDataProvider tfsDataProvider = mock(TfsDataProvider.class);

        TfsIssueFetcher fetcher = new TfsIssueFetcher(cacheUtil, tfsDataProvider);

        String result = fetcher.getUrl("https://tfs.myhost.com", "1");

        assertEquals("https://tfs.myhost.com/1", result);

        verifyZeroInteractions(tfsDataProvider);
    }

    @SuppressWarnings("unchecked")
    private EhCacheUtil createCacheUtil() {
        EventDispatcher<BuildServerListener> listener = mock(EventDispatcher.class);
        ResetCacheRegister register = mock(ResetCacheRegister.class);

        return new EhCacheUtil(new ServerPaths("/root"), listener, register);
    }

    class IsListOf extends ArgumentMatcher<List> {

        private final List matchItems;

        public IsListOf(List matchItems) {
            this.matchItems = matchItems;
        }

        public boolean matches(Object list) {

            List matchList = (List) list;

            if (matchList.size() != this.matchItems.size()) {
                return false;
            }

            for (Object item : this.matchItems) {
                if (!matchList.contains(item)) {
                    return false;
                }
            }

            return true;
        }
    }
}