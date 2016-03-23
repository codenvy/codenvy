/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.plugin.pullrequest.client.inject;

import com.codenvy.plugin.pullrequest.client.GitHubContributionWorkflow;
import com.codenvy.plugin.pullrequest.client.GitHubHostingService;
import com.codenvy.plugin.pullrequest.client.GithubStagesProvider;
import com.codenvy.plugin.pullrequest.client.parts.contribute.StagesProvider;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.VcsHostingService;
import com.codenvy.plugin.pullrequest.client.workflow.ContributionWorkflow;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;

/**
 * Gin module definition for GitHub pull request plugin.
 *
 * @author Mihail Kuznyetsov
 */
@ExtensionGinModule
public class GithubPullRequestGinModule extends AbstractGinModule{

    @Override
    protected void configure() {
        final GinMapBinder<String, ContributionWorkflow> workflowBinder
                = GinMapBinder.newMapBinder(binder(),
                                            String.class,
                                            ContributionWorkflow.class);
        workflowBinder.addBinding(GitHubHostingService.SERVICE_NAME).to(GitHubContributionWorkflow.class);

        final GinMapBinder<String, StagesProvider> stagesProvider
                = GinMapBinder.newMapBinder(binder(),
                                            String.class,
                                            StagesProvider.class);
        stagesProvider.addBinding(GitHubHostingService.SERVICE_NAME).to(GithubStagesProvider.class);

        final GinMultibinder<VcsHostingService> vcsHostingService
                = GinMultibinder.newSetBinder(binder(), VcsHostingService.class);
        vcsHostingService.addBinding().to(GitHubHostingService.class);
    }
}
