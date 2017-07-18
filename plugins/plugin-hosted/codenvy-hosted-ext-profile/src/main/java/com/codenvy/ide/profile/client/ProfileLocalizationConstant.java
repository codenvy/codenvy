/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.profile.client;

import com.google.gwt.i18n.client.Messages;

/**
 * Localization constants. Interface to represent the constants defined in resource bundle:
 * 'ProfileLocalizationConstant.properties'.
 *
 * @author Oleksii Orel
 */
public interface ProfileLocalizationConstant extends Messages {
    /* Group */

    @Key("profileActionGroup")
    String profileActionGroup();

    /* Actions */

    @Key("action.redirectToDashboardAccount.action")
    String redirectToDashboardAccountAction();

    @Key("action.redirectToDashboardAccount.url")
    String redirectToDashboardAccountUrl();

    @Key("action.redirectToDashboardAccount.title")
    String redirectToDashboardAccountTitle();

    @Key("action.redirectToDashboardAccount.description")
    String redirectToDashboardAccountDescription();

}
