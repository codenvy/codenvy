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
package com.codenvy.ide.hosted.client;

import com.google.gwt.i18n.client.Messages;

/**
 * Localization constants. Interface to represent the constants defined in resource bundle:
 * 'HostedLocalizationConstant.properties'.
 *
 * @author Vitaly Parfonov
 */
public interface HostedLocalizationConstant extends Messages {

    @Key("codenvy.tab.title")
    String codenvyTabTitle();

    @Key("codenvy.tab.title.with.workspace.name")
    String codenvyTabTitle(String workspaceName);

    /* Connection closed dialog*/
    @Key("connection.closed.dialog.title")
    String connectionClosedDialogTitle();

    @Key("connection.closed.dialog.message")
    String connectionClosedDialogMessage();

    /* Session expired dialog */
    @Key("session.expired.dialog.title")
    String sessionExpiredDialogTitle();

    @Key("session.expired.dialog.message")
    String sessionExpiredDialogMessage();

    /* Temporary */
    @Key("temporary.toolbar-label.text")
    String temporaryToolbarLabelText();

    @Key("temporary.toolbar-label.title")
    String temporaryToolbarLabelTitle();

    @Key("temporary.toolbar-label.title.message")
    String temporaryToolbarLabelTitleMessage();

    @Key("login.button")
    String loginButtonTitle();

    @Key("ok.button")
    String okButtonTitle();

    @Key("create.account.button")
    String createAccountButtonTitle();

    @Key("get.product.name")
    String getProductName();

    @Key("get.support.link")
    String getSupportLink();

    @Key("support.title")
    String supportTitle();

    @Key("open.dashboard.button")
    String openDashboardTitle();

    @Key("restart.ws.button")
    String restartWsButton();

    @Key("workspace.not.running.message")
    String workspaceNotRunningMessage();

    @Key("workspace.not.running.title")
    String workspaceNotRunningTitle();

    @Key("action.openDocs.title")
    String actionOpenDocsTitle();

    @Key("action.openDocs.description")
    String actionOpenDocsDescription();

    @Key("action.openDocs.url")
    String actionOpenDocsUrl();
}
