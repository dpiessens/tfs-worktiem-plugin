package com.dpiessens;

import jetbrains.buildServer.issueTracker.AbstractIssueProvider;
import jetbrains.buildServer.issueTracker.IssueFetcher;
import jetbrains.buildServer.issueTracker.IssueMention;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class TfsIssueProvider extends AbstractIssueProvider {

    public TfsIssueProvider(TfsIssueFetcher fetcher) {
        super("tfs-workitems", fetcher);
    }

    @Override
    protected boolean useIdPrefix() {
        return false;
    }
}
