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
package com.codenvy.auth.sso.client;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import java.net.MalformedURLException;

/**
 * Take path to the web application as sso client url path.
 *
 * @author Sergii Kabashniuk
 */
public class WebAppClientUrlExtractor implements ClientUrlExtractor {
    @Override
    public String getClientUrl(HttpServletRequest req) throws MalformedURLException {
        return UriBuilder.fromUri(req.getRequestURL().toString()).replacePath(req.getContextPath()).replaceQuery(null).build().toString();
    }
}
