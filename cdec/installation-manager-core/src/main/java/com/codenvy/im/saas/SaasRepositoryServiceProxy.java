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

import com.codenvy.im.event.Event;
import com.codenvy.im.event.EventFactory;
import com.codenvy.im.utils.HttpTransport;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import javax.inject.Named;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static com.codenvy.im.utils.Commons.combinePaths;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class SaasRepositoryServiceProxy {

    private final String        updateEndpoint;
    private final HttpTransport transport;

    @Inject
    public SaasRepositoryServiceProxy(@Named("installation-manager.update_server_endpoint") String updateEndpoint,
                                      HttpTransport transport) {
        this.updateEndpoint = updateEndpoint;
        this.transport = transport;
    }

    public void logAnalyticsEvent(Event event, @Nullable String authToken) throws IOException {
        String requestUrl = combinePaths(updateEndpoint, "/repository/event");

        if (authToken != null) {
            transport.doPost(requestUrl, event, authToken);
        } else {
            transport.doPost(requestUrl, event);
        }
    }
}
