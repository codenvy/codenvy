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
package com.codenvy.ide.hosted.client;

import com.codenvy.ide.hosted.client.action.OpenDocsAction;
import com.codenvy.ide.hosted.client.informers.HttpSessionDestroyedInformer;
import com.codenvy.ide.hosted.client.informers.TemporaryWorkspaceInformer;
import com.codenvy.ide.hosted.client.informers.UnstagedChangesInformer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.constraints.Anchor;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;

/**
 * Extension which adds menu items for help menu, depending on the SubscriptionDescriptor
 *
 * @author Vitaly Parfonov
 */
@Singleton
@Extension(title = "HostedExtension", version = "1.0.0")
public class HostedExtension {
  /*not use here need for initialize*/
  private final HostedLocalizationConstant localizationConstant;

  /** Create extension. */
  @Inject
  public HostedExtension(
      ActionManager actionManager,
      HostedResources resources,
      HostedLocalizationConstant localizationConstant,
      HttpSessionDestroyedInformer httpSessionDestroyedInformer,
      UnstagedChangesInformer unstagedChangesInformer,
      OpenDocsAction openDocsAction,
      TemporaryWorkspaceInformer temporaryWorkspaceInformer) {
    this.localizationConstant = localizationConstant;
    httpSessionDestroyedInformer.process();
    temporaryWorkspaceInformer.process();

    resources.hostedCSS().ensureInjected();

    actionManager.registerAction("warnOnClose", unstagedChangesInformer);

    DefaultActionGroup helpGroup =
        (DefaultActionGroup) actionManager.getAction(IdeActions.GROUP_HELP);

    actionManager.registerAction(localizationConstant.actionOpenDocsTitle(), openDocsAction);
    Constraints constraint = new Constraints(Anchor.BEFORE, "showAbout");
    helpGroup.add(openDocsAction, constraint);
  }
}
