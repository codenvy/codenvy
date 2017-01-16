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
package com.codenvy.ide.support.help.client;

import com.google.gwt.i18n.client.Messages;

/**
 * Localization constants. Interface to represent the constants defined in resource bundle:
 * 'HelpLocalizationConstant.properties'.
 *
 * @author Oleksii Orel
 */
public interface HelpLocalizationConstant extends Messages {
    /* Actions */

    @Key("redirectToEngineerChatChannel.action")
    String redirectToEngineerChatChannelAction();

    @Key("action.redirectToEngineerChatChannel.title")
    String actionRedirectToEngineerChatChannelTitle();

    @Key("action.redirectToEngineerChatChannel.description")
    String actionRedirectToEngineerChatChannelDescription();

    @Key("action.redirectToEngineerChatChannel.url")
    String actionRedirectToEngineerChatChannelUrl();

    @Key("createSupportTicket.action")
    String createSupportTicketAction();

    @Key("action.createSupportTicket.title")
    String actionCreateSupportTicketTitle();

    @Key("action.createSupportTicket.description")
    String actionCreateSupportTicketDescription();

}
