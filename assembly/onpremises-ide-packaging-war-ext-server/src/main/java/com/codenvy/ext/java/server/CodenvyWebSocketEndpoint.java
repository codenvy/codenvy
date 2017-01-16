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
package com.codenvy.ext.java.server;

import org.eclipse.che.api.core.websocket.WebSocketMessageReceiver;
import org.eclipse.che.api.core.websocket.impl.BasicWebSocketEndpoint;
import org.eclipse.che.api.core.websocket.impl.GuiceInjectorEndpointConfigurator;
import org.eclipse.che.api.core.websocket.impl.MessagesReSender;
import org.eclipse.che.api.core.websocket.impl.WebSocketSessionRegistry;

import javax.inject.Inject;
import javax.websocket.server.ServerEndpoint;

/**
 * Duplex WEB SOCKET endpoint, handles messages, errors, session open/close events.
 * This is needed for Hosted version (Codenvy package).
 * In multi node installation we have some additional network configuration.
 * It produces additional path param in URL to wsagent which describe node domain and port where wsagent is launched.
 * Mapping for WebSocket Endpoint changed has changed according this rule.
 * For resolving URL correctly added {node.port_host} parameter we don't use it iny logic construction.
 *
 * @author Vitalii Parfonov
 */

@ServerEndpoint(value = "/{node.port_host}/websocket/{endpoint-id}", configurator = GuiceInjectorEndpointConfigurator.class)
public class CodenvyWebSocketEndpoint extends BasicWebSocketEndpoint {

    @Inject
    public CodenvyWebSocketEndpoint(WebSocketSessionRegistry registry,
                                    MessagesReSender reSender,
                                    WebSocketMessageReceiver receiver) {
        super(registry, reSender, receiver);
    }
}
