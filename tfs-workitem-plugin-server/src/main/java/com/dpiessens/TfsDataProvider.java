package com.dpiessens;

import com.microsoft.tfs.core.httpclient.auth.InvalidCredentialsException;
import jetbrains.buildServer.issueTracker.IssueData;
import org.apache.commons.httpclient.Credentials;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * An interface that represents the connection to TFS.
 * Created by dan.piessens on 10/14/2014.
 */
public interface TfsDataProvider {

    /**
     * Gets all issues related to a specific source control revision.
     * @param revision The source control revision number
     * @param host The TFS host URL
     * @param credentials The credentials needed to access TFS.
     * @throws com.microsoft.tfs.core.httpclient.auth.InvalidCredentialsException Thrown if we cannot connect to the server.
     * @return A collection of the related issues.
     */
    @NotNull
    Collection<IssueData> getIssuesForVersion(@NotNull Integer revision, @NotNull String host, @Nullable Credentials credentials) throws InvalidCredentialsException;

    /**
     * Gets the data related to a specific work item.
     * @param id The ID of the issue to get.
     * @param host The TFS host URL
     * @param credentials The credentials needed to access TFS.
     * @throws com.microsoft.tfs.core.httpclient.auth.InvalidCredentialsException Thrown if we cannot connect to the server.
     * @return The work item data if located; otherwise null.
     */
    @Nullable
    IssueData getIssueById(@NotNull Integer id, @NotNull String host, @Nullable Credentials credentials) throws InvalidCredentialsException;
}
