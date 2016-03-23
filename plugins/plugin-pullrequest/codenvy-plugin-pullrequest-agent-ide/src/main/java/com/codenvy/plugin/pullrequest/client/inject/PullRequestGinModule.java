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

import com.codenvy.plugin.pullrequest.client.steps.PushBranchStepFactory;
import com.codenvy.plugin.pullrequest.client.steps.AddForkRemoteStepFactory;
import com.codenvy.plugin.pullrequest.client.dialogs.commit.CommitView;
import com.codenvy.plugin.pullrequest.client.dialogs.commit.CommitViewImpl;
import com.codenvy.plugin.pullrequest.client.parts.contribute.ContributePartPresenter;
import com.codenvy.plugin.pullrequest.client.parts.contribute.ContributePartView;
import com.codenvy.plugin.pullrequest.client.parts.contribute.ContributePartViewImpl;
import com.codenvy.plugin.pullrequest.client.steps.PushBranchOnForkStep;
import com.codenvy.plugin.pullrequest.client.steps.WaitForkOnRemoteStepFactory;
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;

import javax.inject.Singleton;

/**
 * Gin module definition for the contributor extension.
 */
@ExtensionGinModule
public class PullRequestGinModule extends AbstractGinModule {

    @Override
    protected void configure() {

        // bind the commit dialog view
        bind(CommitView.class).to(CommitViewImpl.class);

        // bind the part view
        bind(ContributePartView.class).to(ContributePartViewImpl.class);
        bind(ContributePartPresenter.class);

        // the steps
        bind(WorkflowExecutor.class).in(Singleton.class);
        bind(PushBranchOnForkStep.class);
        install(new GinFactoryModuleBuilder().build(WaitForkOnRemoteStepFactory.class));
        install(new GinFactoryModuleBuilder().build(PushBranchStepFactory.class));
        install(new GinFactoryModuleBuilder().build(AddForkRemoteStepFactory.class));
    }
}
