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
package com.codenvy.machine;

import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerPropertiesImpl;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Modifies machine server attributes according to provided template of URI of the server.
 *
 * @author Alexander Garagatyi
 */
public abstract class UriTemplateServerProxyTransformer implements MachineServerProxyTransformer {
    private static final Logger LOG = getLogger(RemoteDockerNode.class);

    private final String serverExternalUrlTemplate;
    private final String serverInternalUrlTemplate;

    /**
     * Accepts templates for external and internal server URLs.
     *
     * </p>Template URIs are used in {@link String#format(String, Object...)} with such arguments:
     * <ul>
     * <li>Template URI</li>
     * <li>Server reference</li>
     * <li>Server location hostname</li>
     * <li>Server location external port</li>
     * <li>Server path (without leading slash if present)</li>
     * </ul>
     * Template should satisfy that invocation. Not all arguments have to be used.<br>
     * Modified server components will be retrieved from URI created by this operation.<br>
     * To avoid changing of server use template:http://%2$s:%3$s/%4$s
     */
    public UriTemplateServerProxyTransformer(String serverExternalUrlTemplate,
                                             String serverInternalUrlTemplate) {
        this.serverExternalUrlTemplate = serverExternalUrlTemplate;
        this.serverInternalUrlTemplate = serverInternalUrlTemplate;
    }

    @Override
    public ServerImpl transform(ServerImpl server) {
        final String serverAddress = server.getAddress();
        final int colonIndex = serverAddress.indexOf(':');
        final String serverHost = serverAddress.substring(0, colonIndex);
        final String serverPort = serverAddress.substring(colonIndex + 1);
        String serverPath = "";
        if (server.getProperties() != null && server.getProperties().getPath() != null) {
            serverPath = server.getProperties().getPath();
        }
        if (serverPath.startsWith("/")) {
            serverPath = serverPath.substring(1);
        }

        URI serverExternalUri;
        URI serverInternalUri;
        try {
            serverExternalUri = new URI(format(serverExternalUrlTemplate,
                                               server.getRef(),
                                               serverHost,
                                               serverPort,
                                               serverPath));

        } catch (URISyntaxException e) {
            LOG.error(format("Server uri created from template taken from configuration is invalid. Template:%s. Origin server:%s",
                             serverExternalUrlTemplate,
                             server),
                      e);
            return server;
        }
        try {
            serverInternalUri = new URI(format(serverInternalUrlTemplate,
                                               server.getRef(),
                                               serverHost,
                                               serverPort,
                                               serverPath));
        } catch (URISyntaxException e) {
            LOG.error(
                    format("Server uri created from template taken from configuration is invalid. Template:%s. Origin server:%s",
                           serverInternalUrlTemplate,
                           server),
                    e);
            return server;
        }
        String newServerExternalAddress = serverExternalUri.getHost() +
                                          (serverExternalUri.getPort() == -1 ? ""
                                                                             : ":" + serverExternalUri.getPort());
        String newServerInternalAddress = serverInternalUri.getHost() +
                                          (serverInternalUri.getPort() == -1 ? ""
                                                                             : ":" + serverInternalUri.getPort());
        return new ServerImpl(server.getRef(),
                              serverExternalUri.getScheme(),
                              newServerExternalAddress,
                              serverExternalUri.toString(),
                              new ServerPropertiesImpl(serverExternalUri.getPath(), newServerInternalAddress,
                                                       serverInternalUri.toString()));
    }
}
