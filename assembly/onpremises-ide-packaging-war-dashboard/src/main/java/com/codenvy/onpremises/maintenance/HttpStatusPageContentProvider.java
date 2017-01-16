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
package com.codenvy.onpremises.maintenance;

import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Provides scheduled maintenance info via sending HTTP request and returning JSON from response.
 *
 * @author Mihail Kuznyetsov
 */
public class HttpStatusPageContentProvider implements StatusPageContentProvider {
    private final URL url;
    private final String apiKey;

    @Inject
    public HttpStatusPageContentProvider(@Named("maintenance.statuspage.id") @Nullable String id,
                                         @Named("maintenance.statuspage.apikey") @Nullable String key)
            throws MalformedURLException {
        if (id != null && key != null && !id.isEmpty() && !key.isEmpty()) {
            this.url = new URL("https://api.statuspage.io/v1/pages/" + id + "/incidents/scheduled.json");
            this.apiKey = key;
        } else {
            this.url = null;
            this.apiKey = null;
        }
    }

    @Override
    public String getContent() throws IOException {
        HttpURLConnection connection = null;
        String content;

        try {
            if (url == null) {
                throw new IOException("StatusPage ID and API key are not configured");
            }
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "OAuth " + apiKey);
            connection.setConnectTimeout(10 * 1000);
            connection.setReadTimeout(10 * 1000);

            if (connection.getResponseCode() / 100 == 2) {
                content = IoUtil.readAndCloseQuietly(connection.getInputStream());
            } else {
                throw new IOException(IoUtil.readAndCloseQuietly(connection.getErrorStream()));
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return content;
    }
}
