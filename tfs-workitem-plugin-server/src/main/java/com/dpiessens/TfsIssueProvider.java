package com.dpiessens;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.issueTracker.AbstractIssueProvider;
import jetbrains.buildServer.issueTracker.IssueData;
import jetbrains.buildServer.issueTracker.IssueFetcher;
import jetbrains.buildServer.issueTracker.IssueMention;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.VcsManager;
import jetbrains.buildServer.vcs.VcsModification;
import jetbrains.buildServer.vcs.VcsRootInstance;
import org.jetbrains.annotations.NotNull;
import org.jfree.util.Log;

import java.util.ArrayList;
import java.util.Collection;

public class TfsIssueProvider extends AbstractIssueProvider {

    private static Logger LOG = Logger.getInstance(TfsIssueProvider.class.getName());

    private final TfsDataProvider dataProvider;
    private final VcsManager vcsManager;

    public TfsIssueProvider(IssueFetcher fetcher, TfsDataProvider dataProvider, VcsManager vcsManager) {
        super("tfs-workitems", fetcher);
        this.dataProvider = dataProvider;
        this.vcsManager = vcsManager;
    }

    @Override
    protected boolean useIdPrefix() {
        return false;
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

        VcsRootInstance vcsRoot = vcsModification.getVcsRoot();

        LOG.debug("Issue Tracker VCS Root Type: " + vcsRoot.getVcsName());

        if (!vcsRoot.getVcsName().equalsIgnoreCase("tfs")) {
            return result;
        }

        Integer revision;
        try {
            revision = Integer.parseInt(modification.getVersion());
        }
        catch (NumberFormatException ex) {
            LOG.debug("Revision number could not be parsed: " + modification.getVersion());
            return result;
        }

        //TODO: Get host and optionally credentials from VCS root as a priority
        LOG.debug(String.format("Getting issues for revision: %d Host: %s Credentials: %s", revision, this.myHost, this.myCredentials));

        try {

            Collection<IssueData> issueData = this.dataProvider.getIssuesForVersion(revision, this.myHost, this.myCredentials);
            for (IssueData issue : issueData) {
                result.add(new IssueMention(issue.getId(), issue.getUrl()));
            }

        } catch (Exception e) {
            LOG.error(e);
        }

        return result;
    }


}
