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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Provide extension point to change functionality
 * depends on token state.
 *
 * @author Sergii Kabashniuk
 */
public interface TokenHandler {


    /**
     * Handle situation with valid token.
     *
     * @param request
     *         - http request.
     * @param response
     *         - http response.
     * @param chain
     *         - filter chain.
     * @param session
     *         - http session associated with given token.
     * @param principal
     *         - user associated with given token.
     * @throws IOException
     * @throws ServletException
     */
    void handleValidToken(HttpServletRequest request,
                          HttpServletResponse response,
                          FilterChain chain,
                          HttpSession session,
                          SsoClientPrincipal principal)
            throws IOException, ServletException;

    /**
     * Handle situation when token existed but it's not valid.
     *
     * @param request
     *         - http request.
     * @param response
     *         - http response.
     * @param chain
     *         - filter chain.
     * @param token
     *         - invalid token.
     * @throws IOException
     * @throws ServletException
     */
    void handleBadToken(HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain chain,
                        String token)
            throws IOException, ServletException;

    /**
     * Handle situation when token is not exist.
     *
     * @param request
     *         - http request.
     * @param response
     *         - http response.
     * @param chain
     *         - filter chain.
     * @throws IOException
     * @throws ServletException
     */
    void handleMissingToken(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain)
            throws IOException, ServletException;

}
