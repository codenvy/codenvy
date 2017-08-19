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
