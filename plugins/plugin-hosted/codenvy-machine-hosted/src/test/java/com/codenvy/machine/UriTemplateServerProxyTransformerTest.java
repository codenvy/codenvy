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
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Alexander Garagatyi
 */
public class UriTemplateServerProxyTransformerTest {
    private UriTemplateServerProxyTransformer serverModifier;

    @Test
    public void shouldBeAbleToNotChangeServer() throws Exception {
        serverModifier = new UriTemplateServerProxyTransformer("http://%2$s:%3$s/%4$s",
                                                               "https://%2$s.ua:%3$s/%4$s/1") {};
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
                                                           "my-server.com.ua:32589",
                                                           "https://my-server.com.ua:32589/some/path/1"));

        ServerImpl modifiedServer = serverModifier.transform(originServer);

        assertEquals(modifiedServer, expectedServer);
    }

    @Test
    public void shouldRemoveLeadingSlashFromArgumentServerPath() throws Exception {
        serverModifier = new UriTemplateServerProxyTransformer("http://%2$s:%3$s/%4$s",
                                                               "https://%2$s.ca:%3$s/%4$s") {};
        ServerImpl originServer = new ServerImpl("myRef",
                                                 "http",
                                                 "my-server.com:32589",
                                                 "http://my-server.com:32589/some/path",
                                                 new ServerPropertiesImpl("/some/path",
                                                                          "my-server.com:32589",
                                                                          "http://my-server.com:32589/some/path"));

        ServerImpl expectedServer = new ServerImpl("myRef",
                                                   "http",
                                                   "my-server.com:32589",
                                                   "http://my-server.com:32589/some/path",
                                                   new ServerPropertiesImpl("/some/path",
                                                                            "my-server.com.ca:32589",
                                                                            "https://my-server.com.ca:32589/some/path"));

        ServerImpl modifiedServer = serverModifier.transform(originServer);

        assertEquals(modifiedServer, expectedServer);
    }

    @Test
    public void shouldReturnUnchangedServerIfCreatedExternalUriIsInvalid() throws Exception {
        serverModifier = new UriTemplateServerProxyTransformer(":::://:%3$s`%4$s",
                                                               "https://%2$s.fr:%3$s/%4$s") {};
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
    public void shouldReturnUnchangedServerIfCreatedInternalUriIsInvalid() throws Exception {
        serverModifier = new UriTemplateServerProxyTransformer("https://%2$s.fr:%3$s/%4$s",
                                                               ":::://:%3$s`%4$s") {};
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
    public void shouldNotAdd80PortToExternalUrl() throws Exception {
        serverModifier = new UriTemplateServerProxyTransformer("http://transform-host/%4$s",
                                                               "http://%2$s:%3$s/%4$s") {};
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
    public void shouldNotAdd80PortToInternalUrl() throws Exception {
        serverModifier = new UriTemplateServerProxyTransformer("http://%2$s:%3$s/%4$s",
                                                               "http://transform-host/%4$s") {};
        ServerImpl originServer = new ServerImpl("myRef",
                                                 "http",
                                                 "my-server.com:32589",
                                                 "http://my-server.com:32589/some/path",
                                                 new ServerPropertiesImpl("/some/path",
                                                                          "my-server.com:32589",
                                                                          "http://my-server.com:32589/some/path"));

        ServerImpl modifiedServer = serverModifier.transform(originServer);

        assertEquals(modifiedServer.getProperties().getInternalUrl(), "http://transform-host/some/path");
    }

    @Test
    public void shouldBeAbleToChangeExternalUrl() throws Exception {
        serverModifier = new UriTemplateServerProxyTransformer("https://%3$s-%2$s.transform-host:444/%1$s/%4$s",
                                                               "http://%2$s:%3$s/%4$s") {};
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
                                                                            "my-server.com:32589",
                                                                            "http://my-server.com:32589/some/path"));

        ServerImpl modifiedServer = serverModifier.transform(originServer);

        assertEquals(modifiedServer, expectedServer);
    }

    @Test
    public void shouldBeAbleToChangeInternalUrl() throws Exception {
        serverModifier = new UriTemplateServerProxyTransformer("http://%2$s:%3$s/%4$s",
                                                               "https://%3$s-%2$s.transform-host:444/%1$s/%4$s") {};
        ServerImpl originServer = new ServerImpl("myRef",
                                                 "http",
                                                 "my-server.com:32589",
                                                 "http://my-server.com:32589/some/path",
                                                 new ServerPropertiesImpl("/some/path",
                                                                          "my-server.com:32589",
                                                                          "http://my-server.com:32589/some/path"));
        ServerImpl expectedServer = new ServerImpl("myRef",
                                                   "http",
                                                   "my-server.com:32589",
                                                   "http://my-server.com:32589/some/path",
                                                   new ServerPropertiesImpl("/some/path",
                                                                            "32589-my-server.com.transform-host:444",
                                                                            "https://32589-my-server.com.transform-host:444/myRef/some/path"));

        ServerImpl modifiedServer = serverModifier.transform(originServer);

        assertEquals(modifiedServer, expectedServer);
    }

    @Test
    public void shouldWorkProperlyIfPathIsNull() throws Exception {
        serverModifier = new UriTemplateServerProxyTransformer("https://%3$s-%2$s.transform-host:444/%4$s",
                                                               "http://%2$s:%3$s/%4$s") {};
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
                                                                            "my-server.com:32589",
                                                                            "http://my-server.com:32589/"));

        ServerImpl modifiedServer = serverModifier.transform(originServer);

        assertEquals(modifiedServer, expectedServer);
    }
}
