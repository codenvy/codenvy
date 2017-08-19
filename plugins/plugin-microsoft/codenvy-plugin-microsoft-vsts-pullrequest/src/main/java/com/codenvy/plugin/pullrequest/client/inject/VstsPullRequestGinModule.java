/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.plugin.pullrequest.client.inject;

import com.codenvy.plugin.pullrequest.client.MicrosoftHostingService;
import com.codenvy.plugin.pullrequest.client.VstsContributionWorkflow;
import com.codenvy.plugin.pullrequest.client.VstsStagesProvider;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.plugin.pullrequest.client.parts.contribute.StagesProvider;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.VcsHostingService;
import org.eclipse.che.plugin.pullrequest.client.workflow.ContributionWorkflow;

/**
 * Gin module definition for VSTS pull request plugin.
 *
 * @author Mihail Kuznyetsov
 */
@ExtensionGinModule
public class VstsPullRequestGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    final GinMapBinder<String, ContributionWorkflow> workflowBinder =
        GinMapBinder.newMapBinder(binder(), String.class, ContributionWorkflow.class);
    workflowBinder
        .addBinding(MicrosoftHostingService.SERVICE_NAME)
        .to(VstsContributionWorkflow.class);

    final GinMapBinder<String, StagesProvider> stagesProvider =
        GinMapBinder.newMapBinder(binder(), String.class, StagesProvider.class);
    stagesProvider.addBinding(MicrosoftHostingService.SERVICE_NAME).to(VstsStagesProvider.class);

    final GinMultibinder<VcsHostingService> vcsHostingService =
        GinMultibinder.newSetBinder(binder(), VcsHostingService.class);
    vcsHostingService.addBinding().to(MicrosoftHostingService.class);
  }
}
