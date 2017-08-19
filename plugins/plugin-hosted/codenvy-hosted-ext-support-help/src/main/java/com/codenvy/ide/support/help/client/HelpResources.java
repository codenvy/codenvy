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

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Oleksii Orel */
public interface HelpResources extends ClientBundle {

  @DataResource.MimeType("text/javascript")
  @Source("userVoice.js")
  DataResource userVoice();

  @Source("svg/irc-channel.svg")
  SVGResource ircChannel();

  @Source("svg/create-ticket.svg")
  SVGResource createTicket();

  @Source("svg/list-tickets.svg")
  SVGResource listTickets();
}
