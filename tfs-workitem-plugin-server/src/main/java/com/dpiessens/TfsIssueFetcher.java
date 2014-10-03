package com.dpiessens;

import com.intellij.openapi.diagnostic.Logger;
import com.microsoft.tfs.core.util.CredentialsUtils;
import com.microsoft.tfs.core.util.URIUtils;
import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.workitem.CoreFieldReferenceNames;
import com.microsoft.tfs.core.clients.workitem.WorkItem;
import jetbrains.buildServer.issueTracker.AbstractIssueFetcher;
import jetbrains.buildServer.issueTracker.IssueData;
import jetbrains.buildServer.util.cache.EhCacheUtil;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.Integer;
import java.net.URI;

public class TfsIssueFetcher extends AbstractIssueFetcher {

    static {
        System.setProperty("com.microsoft.tfs.jni.native.base-directory", "C:\\Projects\\SDKs\\TFS-SDK\\TFS-SDK-12.0.2\\redist\\native");
    }

    private static final Logger LOG = Logger.getInstance(TfsIssueFetcher.class.getName());

    public TfsIssueFetcher(@NotNull EhCacheUtil cacheUtil) {
        super(cacheUtil);
    }

    @NotNull
    public IssueData getIssue(@NotNull final String host, @NotNull String id, @Nullable Credentials credentials) throws Exception {
        final String myHost = host;
        final String myId = id;
        final Credentials myCredentials = credentials; 

        return getFromCacheOrFetch(getUrl(host, id), new FetchFunction(){
            @NotNull
            public IssueData fetch() throws Exception {
                return getTfsIssue(myId, myHost, myCredentials);
            }
        });
    }

    @NotNull
    public String getUrl(@NotNull String host, @NotNull String id) {
        // this is not actually the url of the issue, but nonetheless this method seems unused except for creating the cache key
        return appendTrailingSlash(host) + id;
    }

    private String appendTrailingSlash(String host) {
        if(host.endsWith("/"))
            return host;

        return host + "/";
    }

    private static IssueData getTfsIssue(@NotNull String issueId, @NotNull String host, @Nullable Credentials credentials)
            throws Exception {

        try {

            int issueNumber;
            try {
                issueNumber = Integer.parseInt(issueId);
            } catch (NumberFormatException e) {
                return null;
            }

            TFSTeamProjectCollection collection = getProjectCollection(host, credentials);
            return getIssueById(issueNumber, collection);
        }
        catch (Exception ex) {
            LOG.error("Cannot get issue: " + issueId);
            LOG.error(ex);

            throw ex;
        }
    }

    @NotNull
    private static TFSTeamProjectCollection getProjectCollection(@NotNull String host, @Nullable Credentials credentials) {

        URI tfsHost = URIUtils.newURI(host);

        UsernamePasswordCredentials userPass = (UsernamePasswordCredentials)credentials;

        com.microsoft.tfs.core.httpclient.Credentials tfsCredentials;
        if ((userPass.getUserName() == null || userPass.getUserName().length() == 0) && CredentialsUtils.supportsDefaultCredentials())
        {
            tfsCredentials = new com.microsoft.tfs.core.httpclient.DefaultNTCredentials();
        }
        else
        {
            tfsCredentials = new com.microsoft.tfs.core.httpclient.UsernamePasswordCredentials(userPass.getUserName(), userPass.getPassword());
        }

        return new TFSTeamProjectCollection(tfsHost, tfsCredentials);
    }

    private static IssueData getIssueById(int id, TFSTeamProjectCollection collection) {
        WorkItem workItem = collection.getWorkItemClient().getWorkItemByID(id);

        if (workItem != null) {
            return new IssueData(
                    Integer.toString(workItem.getID()),
                    workItem.getTitle(),
                    workItem.getFields().getField(CoreFieldReferenceNames.STATE).getValue().toString(),
                    workItem.getURI(),
                    false);
        }

        return null;
    }
}
