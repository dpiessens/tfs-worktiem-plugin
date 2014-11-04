package com.dpiessens;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.issueTracker.AbstractIssueFetcher;
import jetbrains.buildServer.issueTracker.IssueData;
import jetbrains.buildServer.util.cache.EhCacheUtil;
import org.apache.commons.httpclient.Credentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.util.Log;

public class TfsIssueFetcher extends AbstractIssueFetcher {

    private static final Logger LOG = Logger.getInstance(TfsIssueFetcher.class.getName());

    @NotNull
    private final TfsDataProvider tfsDataProvider;

    /**
     * Creates a new instance of TfsIssueFetcher
     * @param cacheUtil The cache utility manager.
     * @param tfsDataProvider The TFS data provider to get data
     */
    public TfsIssueFetcher(@NotNull EhCacheUtil cacheUtil, @NotNull TfsDataProvider tfsDataProvider) {
        super(cacheUtil);
        this.tfsDataProvider = tfsDataProvider;
    }

    /**
     * Gets the issue from the TFS collection.
     * @param host The TFS collection URL
     * @param id The issue ID
     * @param credentials The credentials needed to connect to TFS.
     * @return The issue from TFS.
     * @throws Exception Thrown is the issue could not be retrieved.
     */
    @NotNull
    public IssueData getIssue(@NotNull final String host, @NotNull String id, @Nullable Credentials credentials) throws Exception {
        final String myId = id;
        final Credentials myCredentials = credentials;

        return getFromCacheOrFetch(getUrl(host, id), new FetchFunction(){
            @NotNull
            public IssueData fetch() throws Exception {
                return getTfsIssue(myId, host, myCredentials);
            }
        });
    }

    /**
     * This is a cache key for issues when they have been retrieved.
     * @param host The TFS collection URL
     * @param id The issue ID
     * @return A cache URL string
     */
    @NotNull
    public String getUrl(@NotNull String host, @NotNull String id) {
        // this is not actually the url of the issue, but nonetheless this method seems unused except for creating the cache key
        return appendTrailingSlash(host) + id;
    }

    /**
     * Appends a trailing slash to the host for caching.
     * @param host The host name
     * @return The host with a trailing slash.
     */
    private String appendTrailingSlash(String host) {
        if(host.endsWith("/"))
            return host;

        return host + "/";
    }

    /**
     * Gets the issue from TFS after parsing the ID.
     * @param issueId The issue ID as a string
     * @param host The TFS collection URL
     * @param credentials The credentials needed to connect to TFS.
     * @return The issue data if located.
     * @throws Exception Thrown if the issue could not be located.
     */
    @NotNull
    private IssueData getTfsIssue(@NotNull String issueId, @NotNull String host, @Nullable Credentials credentials)
            throws Exception {

        Log.debug(String.format("Getting issue from issue fetcher: %s", issueId));

        try {

            int issueNumber;
            try {
                issueNumber = Integer.parseInt(issueId);
            }
            catch (NumberFormatException e) {
                LOG.debug(String.format("Could not parse issue '%s' as a number", issueId));
                throw e;
            }

            IssueData issueData = this.tfsDataProvider.getIssueById(issueNumber, host, credentials);
            if (issueData == null) {
                throw new Exception("Could not find issue in TFS Id: " + issueId);
            }

            return issueData;
        }
        catch (Exception ex) {
            LOG.error("Cannot get issue: " + issueId);
            LOG.error(ex);

            throw ex;
        }
    }
}
