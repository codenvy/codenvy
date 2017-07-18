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
package com.codenvy.machine;

import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerPropertiesImpl;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Alexander Garagatyi
 */
public class UriTemplateServerProxyTransformerTest {
    private UriTemplateServerProxyTransformer serverModifier;

    @Test
    public void shouldBeAbleToNotChangeServer() throws Exception {
        serverModifier = new UriTemplateServerProxyTransformer("http://%2$s:%3$s/%4$s", null, null) {};
        ServerImpl originServer = new ServerImpl("myRef",
                                                 "http",
                                                 "my-server.com:32589",
                                                 "http://my-server.com:32589/some/path",
                                                 new ServerPropertiesImpl("some/path",
                                                                          "my-server.com:32589",
                                                                          "http://my-server.com:32589/some/path"));
        ServerImpl expectedServer = new ServerImpl(originServer.getRef(),
                                                   "http",
                                                   originServer.getAddress(),
                                                   "http://my-server.com:32589/some/path",
                                                   new ServerPropertiesImpl(
                                                           '/' + originServer.getProperties().getPath(),
                                                           originServer.getAddress(),
                                                           "http://my-server.com:32589/some/path"));

        ServerImpl modifiedServer = serverModifier.transform(originServer);

        assertEquals(modifiedServer, expectedServer);
    }

    @Test
    public void shouldRemoveLeadingSlashFromArgumentServerPath() throws Exception {
        serverModifier = new UriTemplateServerProxyTransformer("http://%2$s:%3$s/%4$s", null, null) {};
        ServerImpl originServer = new ServerImpl("myRef",
                                                 "http",
                                                 "my-server.com:32589",
                                                 "http://my-server.com:32589/some/path",
                                                 new ServerPropertiesImpl("/some/path",
                                                                          "my-server.com:32589",
                                                                          "http://my-server.com:32589/some/path"));

        ServerImpl modifiedServer = serverModifier.transform(originServer);

        assertEquals(modifiedServer.getUrl(), originServer.getUrl());
    }

    @Test
    public void shouldReturnUnchangedServerIfCreatedUriIsInvalid() throws Exception {
        serverModifier = new UriTemplateServerProxyTransformer(":::://:%3$s`%4$s",null, null) {};
        ServerImpl originServer = new ServerImpl("myRef",
                                                 "http",
                                                 "my-server.com:32589",
                                                 "http://my-server.com:32589/some/path",
                                                 new ServerPropertiesImpl("/some/path",
                                                                          "my-server.com:32589",
                                                                          "http://my-server.com:32589/some/path"));

        ServerImpl modifiedServer = serverModifier.transform(originServer);

        assertEquals(modifiedServer, originServer);
    }

    @Test
    public void shouldNotAdd80PortToUrl() throws Exception {
        serverModifier = new UriTemplateServerProxyTransformer("http://transform-host/%4$s",null, null) {};
        ServerImpl originServer = new ServerImpl("myRef",
                                                 "http",
                                                 "my-server.com:32589",
                                                 "http://my-server.com:32589/some/path",
                                                 new ServerPropertiesImpl("/some/path",
                                                                          "my-server.com:32589",
                                                                          "http://my-server.com:32589/some/path"));

        ServerImpl modifiedServer = serverModifier.transform(originServer);

        assertEquals(modifiedServer.getUrl(), "http://transform-host/some/path");
    }

    @Test
    public void shouldBeAbleToChangeServerAttributes() throws Exception {
        serverModifier = new UriTemplateServerProxyTransformer("https://%3$s-%2$s.transform-host:444/%1$s/%4$s",null, null) {};
        ServerImpl originServer = new ServerImpl("myRef",
                                                 "http",
                                                 "my-server.com:32589",
                                                 "http://my-server.com:32589/some/path",
                                                 new ServerPropertiesImpl("/some/path",
                                                                          "my-server.com:32589",
                                                                          "http://my-server.com:32589/some/path"));
        ServerImpl expectedServer = new ServerImpl("myRef",
                                                   "https",
                                                   "32589-my-server.com.transform-host:444",
                                                   "https://32589-my-server.com.transform-host:444/myRef/some/path",
                                                   new ServerPropertiesImpl("/myRef/some/path",
                                                                            "32589-my-server.com.transform-host:444",
                                                                            "https://32589-my-server.com.transform-host:444/myRef/some/path"));

        ServerImpl modifiedServer = serverModifier.transform(originServer);

        assertEquals(modifiedServer, expectedServer);
    }

    @Test
    public void shouldBeAbleToChangeServerAttributesWithCustomAttributes() throws Exception {
        serverModifier = new UriTemplateServerProxyTransformer("https://%5$s/%3$s_%2$s/%4$s","public-host.com", "localhost") {};
        ServerImpl originServer = new ServerImpl("myRef",
                                                 "http",
                                                 "my-server.com:32589",
                                                 "http://my-server.com:32589/some/path",
                                                 new ServerPropertiesImpl("/some/path",
                                                                          "my-server.com:32589",
                                                                          "http://my-server.com:32589/some/path"));
        ServerImpl expectedServer = new ServerImpl("myRef",
                                                   "https",
                                                   "localhost",
                                                   "https://localhost/32589_my-server.com/some/path",
                                                   new ServerPropertiesImpl("/32589_my-server.com/some/path",
                                                                            "public-host.com",
                                                                            "https://public-host.com/32589_my-server.com/some/path"));

        ServerImpl modifiedServer = serverModifier.transform(originServer);

        assertEquals(modifiedServer, expectedServer);
    }

    @Test
    public void shouldWorkProperlyIfPathIsNull() throws Exception {
        serverModifier = new UriTemplateServerProxyTransformer("https://%3$s-%2$s.transform-host:444/%4$s", null, null) {};
        ServerImpl originServer = new ServerImpl("myRef",
                                                 "http",
                                                 "my-server.com:32589",
                                                 "http://my-server.com:32589/",
                                                 new ServerPropertiesImpl(null,
                                                                          "my-server.com:32589",
                                                                          "http://my-server.com:32589/"));
        ServerImpl expectedServer = new ServerImpl("myRef",
                                                   "https",
                                                   "32589-my-server.com.transform-host:444",
                                                   "https://32589-my-server.com.transform-host:444/",
                                                   new ServerPropertiesImpl("/",
                                                                            "32589-my-server.com.transform-host:444",
                                                                            "https://32589-my-server.com.transform-host:444/"));

        ServerImpl modifiedServer = serverModifier.transform(originServer);

        assertEquals(modifiedServer, expectedServer);
    }
}
