package com.dpiessens;

import com.intellij.openapi.diagnostic.Logger;
import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Changeset;
import com.microsoft.tfs.core.clients.workitem.CoreFieldReferenceNames;
import com.microsoft.tfs.core.clients.workitem.WorkItem;
import com.microsoft.tfs.core.clients.workitem.WorkItemClient;
import com.microsoft.tfs.core.clients.workitem.fields.FieldCollection;
import com.microsoft.tfs.core.httpclient.auth.InvalidCredentialsException;
import com.microsoft.tfs.core.util.CredentialsUtils;
import com.microsoft.tfs.core.util.TSWAHyperlinkBuilder;
import com.microsoft.tfs.core.util.URIUtils;
import jetbrains.buildServer.issueTracker.IssueData;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.util.Log;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A provider that uses the TFS native API to get work item information
 * Created by dan.piessens on 10/14/2014.
 */
public class TfsJavaDataProvider implements TfsDataProvider {

    private static final Logger LOG = Logger.getInstance(TfsJavaDataProvider.class.getName());

    private static volatile boolean runtimeSet = false;

    public TfsJavaDataProvider(PluginDescriptor pluginDescriptor) {
        validateRuntimeConfiguration(pluginDescriptor);
    }

    /**
     * Gets all issues related to a specific source control revision.
     * @param revision The source control revision number
     * @param host The TFS host URL
     * @param credentials The credentials needed to access TFS.
     * @throws com.microsoft.tfs.core.httpclient.auth.InvalidCredentialsException Thrown if we cannot connect to the server.
     * @return A collection of the related issues.
     */
    @NotNull
    public Collection<IssueData> getIssuesForVersion(@NotNull Integer revision, @NotNull String host, @Nullable Credentials credentials)
            throws InvalidCredentialsException {
        Collection<IssueData> issues = new ArrayList<IssueData>();

        TFSTeamProjectCollection collection = null;
        VersionControlClient versionControlClient = null;
        WorkItemClient workItemClient = null;

        try {
            collection = getProjectCollection(host, credentials);

            versionControlClient = collection.getVersionControlClient();
            workItemClient = collection.getWorkItemClient();

            final Changeset changeset = versionControlClient.getChangeset(revision);

            if (changeset != null) {
                final WorkItem[] workItems = changeset.getWorkItems(workItemClient);
                if (workItems != null && workItems.length > 0) {
                    LOG.debug(String.format("Changeset %d was linked to %d work items", revision, workItems.length));

                    TSWAHyperlinkBuilder linkingClient = new TSWAHyperlinkBuilder(collection);

                    for (WorkItem workItem : workItems) {
                        LOG.debug(String.format("Adding work item: %d", workItem.getID()));
                        issues.add(convertWorkItemToIssueData(workItem, linkingClient));
                    }
                } else {
                    LOG.debug(String.format("Changeset %d was not linked to any work items", revision));
                }
            } else {
                LOG.debug(String.format("Changeset %d could not be found in system.", revision));
            }
        }
        finally {
            if (workItemClient != null) {
                workItemClient.close();
            }

            if (versionControlClient != null) {
                versionControlClient.close();
            }

            if (collection != null) {
                collection.close();
            }
        }

        return issues;
    }

    /**
     * Gets the data related to a specific work item.
     * @param ids The ID collection of the issue to get.
     * @param host The TFS host URL
     * @param credentials The credentials needed to access TFS.
     * @throws com.microsoft.tfs.core.httpclient.auth.InvalidCredentialsException Thrown if we cannot connect to the server.
     * @return The work item data of the items that could be located.
     */
    @NotNull
    public Collection<IssueData> getIssues(@NotNull Collection<Integer> ids, @NotNull String host, @Nullable Credentials credentials)
            throws InvalidCredentialsException {

        TFSTeamProjectCollection collection = null;
        WorkItemClient client = null;

        try {
            collection = getProjectCollection(host, credentials);
            client = collection.getWorkItemClient();

            Collection<IssueData> issues = new ArrayList<IssueData>();
            for (Integer id: ids) {

                Log.debug(String.format("Getting work item %d from TFS in batch", id));
                issues.add(getIssueById(id, collection, client));
            }

            return issues;
        }
        finally {

            if (client != null) {
                client.close();
            }

            if (collection != null) {
                collection.close();
            }
        }
    }

    /**
     * Gets the data related to a specific work item.
     * @param id The ID of the issue to get.
     * @param host The TFS host URL
     * @param credentials The credentials needed to access TFS.
     * @throws com.microsoft.tfs.core.httpclient.auth.InvalidCredentialsException Thrown if we cannot connect to the server.
     * @return The work item data if located; otherwise null.
     */
    @Nullable
    public IssueData getIssueById(@NotNull Integer id, @NotNull String host, @Nullable Credentials credentials)
            throws InvalidCredentialsException {

        TFSTeamProjectCollection collection = null;
        WorkItemClient client = null;

        try {
            collection = getProjectCollection(host, credentials);
            client = collection.getWorkItemClient();

            Log.debug(String.format("Getting work item %d from TFS", id));

            return getIssueById(id, collection, client);
        }
        finally {

            if (client != null) {
                client.close();
            }

            if (collection != null) {
                collection.close();
            }
        }
    }

    /**
     * Gets the team project collection connection to TFS.
     * @param host The TFS host URL
     * @param credentials The credentials needed to access TFS.
     * @return The team project collection
     */
    @NotNull
    private static TFSTeamProjectCollection getProjectCollection(@NotNull String host, @Nullable Credentials credentials)
            throws InvalidCredentialsException {

        URI tfsHost = URIUtils.newURI(host);

        UsernamePasswordCredentials userPass = (UsernamePasswordCredentials)credentials;

        com.microsoft.tfs.core.httpclient.Credentials tfsCredentials;
        if (userPass == null || userPass.getUserName() == null || userPass.getUserName().isEmpty()) {
            if ( CredentialsUtils.supportsDefaultCredentials()) {
                LOG.debug(String.format("Connecting to host %s with default NT Credentials", tfsHost));
                tfsCredentials = new com.microsoft.tfs.core.httpclient.DefaultNTCredentials();
            }
            else {
                throw new InvalidCredentialsException("Native credentials are not supported; please enter a username and password.");
            }
        }
        else {
            LOG.debug(String.format("Connecting to host %s with user: %s", tfsHost, userPass.getUserName()));
            tfsCredentials = new com.microsoft.tfs.core.httpclient.UsernamePasswordCredentials(userPass.getUserName(), userPass.getPassword());
        }

        return new TFSTeamProjectCollection(tfsHost, tfsCredentials);
    }

    /**
     * Gets an issue from the work item manager by ID.
     * @param id The work item ID.
     * @param collection The team project collection.
     * @param client The work item client
     * @return The issue data if located; otherwise null
     */
    @Nullable
    private static IssueData getIssueById(int id, @NotNull TFSTeamProjectCollection collection, @NotNull WorkItemClient client) {

        WorkItem workItem = client.getWorkItemByID(id);

        if (workItem != null) {
            TSWAHyperlinkBuilder linkingClient = new TSWAHyperlinkBuilder(collection);
            return convertWorkItemToIssueData(workItem, linkingClient);
        }

        return null;
    }

    /**
     * Converts a TFS work item into JetBrains standard IssueData object.
     * @param workItem The work item to convert.
     * @param hyperlinkBuilder The hyperlink builder needed to create links.
     * @return The converted IssueData item.
     */
    @NotNull
    private static IssueData convertWorkItemToIssueData(@NotNull WorkItem workItem, @NotNull TSWAHyperlinkBuilder hyperlinkBuilder) {
        Map<String, String> data = new HashMap<String, String>();
        data.put(IssueData.SUMMARY_FIELD, workItem.getTitle());

        final FieldCollection fields = workItem.getFields();

        // Determine state to see if done
        String value = fields.getField(CoreFieldReferenceNames.STATE).getValue().toString();

        boolean resolved = !workItem.isOpen();
        data.put(IssueData.STATE_FIELD, value);

        // Get feature request state
        String issueType = fields.getField(CoreFieldReferenceNames.WORK_ITEM_TYPE).getValue().toString();
        boolean featureRequest = !issueType.equalsIgnoreCase("task");
        data.put(IssueData.TYPE_FIELD, issueType);

        return new IssueData(
                Integer.toString(workItem.getID()),
                data,
                resolved,
                featureRequest,
                hyperlinkBuilder.getWorkItemEditorURL(workItem.getID()).toString());
    }

    /**
     * Validates the runtime configuration to ensure that the native TFS binaries have been loaded
     * @param pluginDescriptor The plugin information needed to set the path.
     */
    private static synchronized void validateRuntimeConfiguration(@NotNull PluginDescriptor pluginDescriptor) {
        if (runtimeSet) {
            LOG.debug("TFS binaries have already been loaded, skipping load");
            return;
        }

        runtimeSet = true;

        File pluginRoot = pluginDescriptor.getPluginRoot();
        if (!pluginRoot.exists()) {
            LOG.error(String.format("Plugin root could not be found or does not exist!"));
            return;
        }

        // Locate the file
        File libDir = new File(pluginRoot, "server/lib/tfs");
        if (libDir.exists()){
            LOG.debug("Setting native TFS library path: " + libDir.toString());
            System.setProperty("com.microsoft.tfs.jni.native.base-directory", libDir.getAbsolutePath());
        }
        else {
            LOG.warn("Cannot locate native TFS library path: " + libDir);
        }
    }
}
