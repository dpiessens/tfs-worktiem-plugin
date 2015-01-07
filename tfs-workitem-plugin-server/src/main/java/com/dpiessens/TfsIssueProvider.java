package com.dpiessens;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.issueTracker.AbstractIssueProvider;
import jetbrains.buildServer.issueTracker.IssueData;
import jetbrains.buildServer.issueTracker.IssueFetcher;
import jetbrains.buildServer.issueTracker.IssueMention;
import jetbrains.buildServer.util.cache.EhCacheUtil;
import jetbrains.buildServer.vcs.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.Credentials;
import org.jetbrains.annotations.NotNull;
import org.jfree.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import net.sf.ehcache.Cache;

public class TfsIssueProvider extends AbstractIssueProvider {

    private static final String USE_CREDS_PROPERTY = "useVcsCredentials";

    private static Logger LOG = Logger.getInstance(TfsIssueProvider.class.getName());

    private final TfsDataProvider dataProvider;
    private final VcsManager vcsManager;
    private final Cache myCache;

    public TfsIssueProvider(IssueFetcher fetcher, TfsDataProvider dataProvider, VcsManager vcsManager, EhCacheUtil cacheUtil) {
        super("tfs-workitems", fetcher);
        this.dataProvider = dataProvider;
        this.vcsManager = vcsManager;
        this.myCache = cacheUtil.createCache("tfsIssueMentions");
    }

    @Override
    protected boolean useIdPrefix() {
        return false;
    }

    @Override
    public boolean isBatchFetchSupported() {
        return true;
    }

    @Override
    public boolean isHasRelatedIssues(@NotNull VcsModification modification) {
        try {
            return !modification.isPersonal() && Integer.parseInt(modification.getVersion()) > 0;
        }
        catch (NumberFormatException ex){
            return false;
        }
    }

    @NotNull
    @Override
    public Collection<IssueMention> getRelatedIssues(@NotNull final VcsModification modification) {

        Collection<IssueMention> result = new ArrayList<IssueMention>();

        if(modification.isPersonal()) {
            return result;
        }

        // Filter by getting the VCS Root and figuring out if that is a TFS root
        final SVcsModification vcsModification = this.vcsManager.findModificationById(modification.getId(), false);
        if (vcsModification == null) {
            Log.warn(String.format("Could not find VCS Modification %d in build system", modification.getId()));
            return result;
        }

        final VcsRootInstance vcsRoot = vcsModification.getVcsRoot();

        LOG.debug("Issue Tracker VCS Root Type: " + vcsRoot.getVcsName());

        if (!vcsRoot.getVcsName().equalsIgnoreCase("tfs")) {
            return result;
        }

        final Integer revision;
        try {
            revision = Integer.parseInt(modification.getVersion());
        }
        catch (NumberFormatException ex) {
            LOG.debug("Revision number could not be parsed: " + modification.getVersion());
            return result;
        }


        LOG.debug(String.format("VCS Properties: %s", vcsRoot.getProperties()));

        //TODO: Get host and optionally credentials from VCS root as a priority
        LOG.debug(String.format("Getting issues for revision: %d from host: %s", revision, this.myHost));

        try {

            Collection<SerializableIssueMention> issueMentions = getFromCacheOrFetch(revision, new FetchFunction() {

                @NotNull
                public Collection<SerializableIssueMention> fetch() throws Exception {

                    Collection<SerializableIssueMention> issueMentions = new ArrayList<SerializableIssueMention>();

                    Credentials credentials = checkForVcsCredentials(vcsRoot);

                    LOG.debug("Issue list was not cached, getting issues from TFS");
                    Collection<IssueData> issueData = dataProvider.getIssuesForVersion(revision, myHost, credentials);

                    for (IssueData issue : issueData) {
                        issueMentions.add(new SerializableIssueMention(issue.getId(), issue.getUrl()));
                    }

                    return issueMentions;
                }
            });

            for(SerializableIssueMention mention: issueMentions) {
                result.add(new IssueMention(mention.getId(), mention.getUrl()));
            }

        } catch (Exception e) {
            LOG.error(e);
        }

        return result;
    }

    /**
     * Checks to see if the credentials should be shared from the project VCS root.
     * @param vcsRoot The VCS root for the revision
     * @return The appropriate credentials.
     */
    @NotNull
    private org.apache.commons.httpclient.Credentials checkForVcsCredentials(final VcsRoot vcsRoot) {

        // If flag is checked use TFS credentials instead
        final Map<String, String> properties = this.getProperties();

        if (properties.containsKey(USE_CREDS_PROPERTY) && Boolean.parseBoolean(properties.get(USE_CREDS_PROPERTY))) {
            String username = vcsRoot.getProperty("tfs-username");
            String password = vcsRoot.getProperty("secure:tfs-password");
            return new UsernamePasswordCredentials(username, password);
        }

        return myCredentials;
    }

    @NotNull
    private Collection<SerializableIssueMention> getFromCacheOrFetch(@NotNull Object key, @NotNull FetchFunction function)
            throws Exception {

        // NOTE: UGLY HACK!
        // EhCache uses 'Thread.currentThread().getContextClassLoader()' to load classes of serialized
        // instances, which is not the same as 'getClass().getClassLoader()'. This causes a problem
        // for plugin classes, such as IssueDataImpl.
        // If you can make this work without the hack, you're welcome to fix it.
        ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        try {
            net.sf.ehcache.Element element = myCache.get(key);
            if (element != null) {
                Serializable value = element.getValue();
                if (value == null) {
                    throw new RuntimeException(String.format("Cached issue mention for %s is not found", key));
                }

                LOG.debug(String.format("Returning issue list for revision %s from issue cache", key));
                return (Collection<SerializableIssueMention>) value;
            }

            try {
                LOG.debug(String.format("Adding revision %s to revision cache", key));
                Collection<SerializableIssueMention> result = function.fetch();
                myCache.put(new net.sf.ehcache.Element(key, result));
                return result;
            }
            catch (Exception e) {
                myCache.put(new net.sf.ehcache.Element(key, null));
                throw e;
            }
        }
        finally {
            Thread.currentThread().setContextClassLoader(currentLoader);
        }
    }

    /**
     * An interface represents an action of actual issue fetching.
     * This action takes place when a suitable issue isn't found in cache, or expired.
     */
    private static interface FetchFunction {
        /**
         * Fetches the issue. Throws an exception in case of a problem.
         * @return a non-null issue in case of success
         * @throws Exception in case of an error
         */
        @NotNull
        Collection<SerializableIssueMention> fetch() throws Exception;
    }
}
