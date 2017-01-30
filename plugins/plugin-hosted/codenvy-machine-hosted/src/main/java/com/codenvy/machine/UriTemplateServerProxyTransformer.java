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

import com.google.common.base.Strings;

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

    private final String serverUrlTemplate;

    /**
     * Value of CODENVY_HOST env variable
     */
    private final String codenvyHost;

    /**
     * Value of CHE_DOCKER_IP_EXTERNAL env variable. (optional then it can be null/empty)
     */
    private final String cheDockerIpExternal;

    /**
     * Template URI is used in {@link String#format(String, Object...)} with such arguments:
     * <ul>
     * <li>Template URI</li>
     * <li>Server reference</li>
     * <li>Server location hostname</li>
     * <li>Server location external port</li>
     * <li>Server path (without leading slash if present)</li>
     * <li>codenvy.host property (or "che.docker.ip.external" if set)</li>
     * </ul>
     * Template should satisfy that invocation. Not all arguments have to be used.<br>
     * Modified server components will be retrieved from URI created by this operation.<br>
     * To avoid changing of server use template:http://%2$s:%3$s/%4$s
     * @param codenvyHost that will be injected as %5 argument in template (unless cheDockerIpExternal is given)
     * @param cheDockerIpExternal that will be injected as %5 argument in external template by replacing codenvy.host
     */
    public UriTemplateServerProxyTransformer(String serverUrlTemplate, String codenvyHost, String cheDockerIpExternal) {
        this.serverUrlTemplate = serverUrlTemplate;
        this.codenvyHost = codenvyHost;
        this.cheDockerIpExternal = cheDockerIpExternal;
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

        // In case of external address property set, we should replace codenvy.host by this external address.
        // for example on macOS, codenvy.host is defaulted to 192.168.65.2 which is not reachable from host machine while
        // it needs to use localhost (external address).
        final String externalAddress;
        if (!Strings.isNullOrEmpty(cheDockerIpExternal)) {
            externalAddress = cheDockerIpExternal;
        } else {
            externalAddress = codenvyHost;
        }

        try {
            // external URI/address will be used by the browser clients (redirect calls through externalAddress or codenvyHost if not set)
            URI serverUriExternal = new URI(format(serverUrlTemplate,
                                                   server.getRef(),
                                                   serverHost,
                                                   serverPort,
                                                   serverPath,
                                                   externalAddress));
            String updatedExternalServerAddress = serverUriExternal.getHost() +
                                                  (serverUriExternal.getPort() == -1 ? "" : ":" + serverUriExternal.getPort());

            // internal URI/address will be used by workspace agents internals (we redirect calls to codenvyHost)
            URI serverUriInternal = new URI(format(serverUrlTemplate,
                                                   server.getRef(),
                                                   serverHost,
                                                   serverPort,
                                                   serverPath,
                                                   codenvyHost));
            String updatedInternalServerAddress = serverUriInternal.getHost() +
                                                  (serverUriInternal.getPort() == -1 ? "" : ":" + serverUriInternal.getPort());

            // return a new updated server object with correct external and internal addresses.
            return new ServerImpl(server.getRef(),
                                  serverUriExternal.getScheme(),
                                  updatedExternalServerAddress,
                                  serverUriExternal.toString(),
                                  new ServerPropertiesImpl(serverUriExternal.getPath(), updatedInternalServerAddress, serverUriInternal.toString()));
        } catch (URISyntaxException e) {
            LOG.error(format("Server uri created from template taken from configuration is invalid. Template:%s. Origin server:%s",
                             serverUrlTemplate,
                             server),
                      e);
            return server;
        }
    }
}
