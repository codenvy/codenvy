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
package com.codenvy.onpremises.factory;

import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URLDecoder;

import static com.google.common.base.Strings.nullToEmpty;
import static javax.ws.rs.core.UriBuilder.fromUri;

/**
 * Servlet to handle factory URL's.
 */
@Singleton
public class FactoryServlet extends HttpServlet {
    private static final long serialVersionUID = 2761405300745713306L;

    private static final Logger LOG = LoggerFactory.getLogger(FactoryServlet.class);


    private final String creationFailedPage;

    @Inject
    public FactoryServlet(@Named("page.creation.error") String creationFailedPage) {
        super();
        this.creationFailedPage = creationFailedPage;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        FactoryDto requestedFactory = (FactoryDto)req.getAttribute("factory");
        if (requestedFactory == null) {
            dispatchToErrorPage(req, resp, creationFailedPage, "Unable to get factory from request.");
        }

        // DO NOT REMOVE! This log will be used in statistic analyzing
        LOG.info("EVENT#factory-accepted# WS#{}# REFERRER#{}# FACTORY#{}# # AFFILIATE-ID#{}# WS-LOCATION#{}# WS-TYPE#{}#",
                 requestedFactory.getWorkspace().getName(),
                 nullToEmpty(req.getHeader("Referer")),
                 URLDecoder.decode(fromUri(req.getRequestURL().toString()).replaceQuery(req.getQueryString()).build().toString(), "UTF-8"),
                 "", //AFFILIATE-ID
                 "", //TODO: use from policy
                 "" //TODO: use from policy
                );

        String redirectUrl = UriBuilder.fromPath("/dashboard/").fragment("load-factory/").build().toString();

        // factory has been resolved. If resolved factory has id, only send the ID. Else send the whole parameters
        if (requestedFactory.getId() != null) {
            redirectUrl += "?id=" + requestedFactory.getId();
        } else {
            redirectUrl += "?" + req.getQueryString();
        }

        resp.sendRedirect(redirectUrl);
    }


    private void dispatchToErrorPage(HttpServletRequest req, HttpServletResponse resp, String dispatchPath, String message)
            throws ServletException, IOException {
        if (message != null) {
            LOG.debug(message);
            req.setAttribute(RequestDispatcher.ERROR_MESSAGE, message);
        }
        req.getRequestDispatcher(dispatchPath).forward(req, resp);
    }

}
