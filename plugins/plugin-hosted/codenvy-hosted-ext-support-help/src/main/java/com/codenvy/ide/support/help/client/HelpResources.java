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

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * @author Oleksii Orel
 */
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
