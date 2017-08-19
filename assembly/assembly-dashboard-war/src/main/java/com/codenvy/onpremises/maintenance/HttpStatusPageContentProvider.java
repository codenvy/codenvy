/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.onpremises.maintenance;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.IoUtil;

/**
 * Provides scheduled maintenance info via sending HTTP request and returning JSON from response.
 *
 * @author Mihail Kuznyetsov
 */
public class HttpStatusPageContentProvider implements StatusPageContentProvider {
  private final URL url;
  private final String apiKey;

  @Inject
  public HttpStatusPageContentProvider(
      @Named("maintenance.statuspage.id") @Nullable String id,
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
