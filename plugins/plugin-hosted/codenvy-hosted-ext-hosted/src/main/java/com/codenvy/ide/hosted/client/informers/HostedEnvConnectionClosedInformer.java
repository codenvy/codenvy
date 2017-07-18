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
package com.codenvy.ide.hosted.client.informers;

import com.codenvy.ide.hosted.client.HostedLocalizationConstant;
import com.codenvy.ide.hosted.client.notifier.BadConnectionNotifierView;
import com.google.inject.Inject;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.client.ConnectionClosedInformerImpl;
import org.eclipse.che.ide.websocket.events.WebSocketClosedEvent;

import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_ABNORMAL;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_FAILURE_TLS_HANDSHAKE;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_GOING_AWAY;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_INCONSISTENT_DATA;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_NEGOTIATE_EXTENSION;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_NORMAL;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_NO_STATUS;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_PROTOCOL_ERROR;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_TOO_LARGE;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_UNEXPECTED_CONDITION;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_UNSUPPORTED;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_VIOLATE_POLICY;

/**
 * Notify that WebSocket connection was closed.
 *
 * @author Roman Nikitenko
 * @author Anton Korneta
 */
public class HostedEnvConnectionClosedInformer extends ConnectionClosedInformerImpl {

    private BadConnectionNotifierView  badConnectionInfoView;
    private HostedLocalizationConstant localizationConstant;

    @Inject
    HostedEnvConnectionClosedInformer(final DialogFactory dialogFactory,
                                      final CoreLocalizationConstant corelocalizationConstant,
                                      final BadConnectionNotifierView badConnectionInfoView,
                                      final HostedLocalizationConstant localizationConstant) {
        super(dialogFactory, corelocalizationConstant);
        this.badConnectionInfoView = badConnectionInfoView;
        this.localizationConstant = localizationConstant;

        badConnectionInfoView.setDelegate(() -> badConnectionInfoView.close());
    }

    @Override
    public void onConnectionClosed(WebSocketClosedEvent event) {
        switch (event.getCode()) {
            case CLOSE_NORMAL:
            case CLOSE_GOING_AWAY:
            case CLOSE_PROTOCOL_ERROR:
            case CLOSE_UNSUPPORTED:
            case CLOSE_NO_STATUS:
            case CLOSE_ABNORMAL:
            case CLOSE_INCONSISTENT_DATA:
            case CLOSE_VIOLATE_POLICY:
            case CLOSE_TOO_LARGE:
            case CLOSE_NEGOTIATE_EXTENSION:
            case CLOSE_UNEXPECTED_CONDITION:
            case CLOSE_FAILURE_TLS_HANDSHAKE:
                showPromptToLogin(localizationConstant.connectionClosedDialogTitle(), localizationConstant.connectionClosedDialogMessage());
        }
    }

    /**
     * Displays dialog using title and message.
     */
    private void showPromptToLogin(String title, String message) {
        badConnectionInfoView.showDialog(title, message);
    }
}
