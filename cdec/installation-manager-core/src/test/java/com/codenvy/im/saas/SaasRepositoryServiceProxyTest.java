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
package com.codenvy.im.saas;

import com.codenvy.im.BaseTest;
import com.codenvy.im.event.Event;
import com.codenvy.im.utils.HttpTransport;
import com.google.common.collect.ImmutableMap;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Anatoliy Bazko
 */
public class SaasRepositoryServiceProxyTest extends BaseTest {

    @Mock
    private HttpTransport transport;

    private SaasRepositoryServiceProxy saasRepositoryServiceProxy;

    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);
        saasRepositoryServiceProxy = new SaasRepositoryServiceProxy(UPDATE_API_ENDPOINT, transport);
    }

    @Test
    public void shouldLogAnalyticsEventWithToken() throws Exception {
        ImmutableMap<String, String> params = ImmutableMap.of("a", "b");
        Event event = new Event(Event.Type.CDEC_FIRST_LOGIN, params);

        saasRepositoryServiceProxy.logAnalyticsEvent(event, null);

        verify(transport).doPost(UPDATE_API_ENDPOINT + "/repository/event", event);
    }

    @Test
    public void shouldLogAnalyticsEventWithoutToken() throws Exception {
        ImmutableMap<String, String> params = ImmutableMap.of("a", "b");
        Event event = new Event(Event.Type.CDEC_FIRST_LOGIN, params);

        String token = "token";

        saasRepositoryServiceProxy.logAnalyticsEvent(event, token);

        verify(transport).doPost(UPDATE_API_ENDPOINT + "/repository/event", event, token);
    }

}
