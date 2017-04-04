/*
 *  [2012] - [2017] Codenvy, S.A.
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

import com.codenvy.plugin.pullrequest.client.BitbucketHostingService;
import org.eclipse.che.plugin.pullrequest.client.parts.contribute.StagesProvider;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.VcsHostingService;
import org.eclipse.che.plugin.pullrequest.client.workflow.ContributionWorkflow;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;

/**
 * Gin module definition for Bitbucket pull request plugin.
 *
 * @author Mihail Kuznyetsov
 */
@ExtensionGinModule
public class BitbucketPullRequestGinModule extends AbstractGinModule{

    @Override
    protected void configure() {
        final GinMapBinder<String, ContributionWorkflow> workflowBinder
                = GinMapBinder.newMapBinder(binder(),
                                            String.class,
                                            ContributionWorkflow.class);
        workflowBinder.addBinding(BitbucketHostingService.SERVICE_NAME).to(
                com.codenvy.plugin.pullrequest.client.BitbucketContributionWorkflow.class);

        final GinMapBinder<String, StagesProvider> stagesProvider
                = GinMapBinder.newMapBinder(binder(),
                                            String.class,
                                            StagesProvider.class);
        stagesProvider.addBinding(BitbucketHostingService.SERVICE_NAME).to(
                com.codenvy.plugin.pullrequest.client.BitbucketStagesProvider.class);

        final GinMultibinder<VcsHostingService> vcsHostingService
                = GinMultibinder.newSetBinder(binder(), VcsHostingService.class);
        vcsHostingService.addBinding().to(BitbucketHostingService.class);
    }
}
