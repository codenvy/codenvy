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
