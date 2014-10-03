package com.dpiessens;

import jetbrains.buildServer.issueTracker.AbstractIssueProviderFactory;
import jetbrains.buildServer.issueTracker.IssueFetcher;
import jetbrains.buildServer.issueTracker.IssueProvider;

public class TfsIssueProviderFactory extends AbstractIssueProviderFactory {

    private TfsIssueFetcher myFetcher;

    public TfsIssueProviderFactory(TfsIssueFetcher fetcher) {
        super(fetcher, "tfs-workitems");
        this.myFetcher = fetcher;
    }

    public IssueProvider createProvider() {
        return new TfsIssueProvider(myFetcher);
    }
}
