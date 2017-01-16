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

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet for getting scheduled maintenance status.
 *
 * @author Mihail Kuznyetsov
 */
@Singleton
public class MaintenanceStatusServlet extends HttpServlet {
    private final StatusPageContentProvider contentProvider;

    @Inject
    public MaintenanceStatusServlet(StatusPageContentProvider contentProvider) {
        this.contentProvider = contentProvider;
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        try {
            String result = contentProvider.getContent();
            resp.setContentType("application/json");
            resp.getWriter().write(result);
        } catch (IOException e) {
            resp.setStatus(500);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
