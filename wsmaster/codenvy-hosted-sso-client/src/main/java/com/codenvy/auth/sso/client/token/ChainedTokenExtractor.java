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
package com.codenvy.auth.sso.client.token;

import javax.servlet.http.HttpServletRequest;

/**
 * Try to extract token from request in 3 steps.
 * 1. From query parameter.
 * 2. From header.
 * 3. From cookie.
 *
 * @author Sergii Kabashniuk
 */
public class ChainedTokenExtractor implements RequestTokenExtractor {
    private final CookieRequestTokenExtractor cookieRequestTokenExtractor;

    private final HeaderRequestTokenExtractor headerRequestTokenExtractor;

    private final QueryRequestTokenExtractor queryRequestTokenExtractor;

    public ChainedTokenExtractor() {
        cookieRequestTokenExtractor = new CookieRequestTokenExtractor();
        headerRequestTokenExtractor = new HeaderRequestTokenExtractor();
        queryRequestTokenExtractor = new QueryRequestTokenExtractor();
    }

    @Override
    public String getToken(HttpServletRequest req) {
        String token;
        if ((token = queryRequestTokenExtractor.getToken(req)) == null) {
            if ((token = headerRequestTokenExtractor.getToken(req)) == null) {
                token = cookieRequestTokenExtractor.getToken(req);
            }
        }
        return token;
    }
}
