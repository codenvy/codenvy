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
package com.codenvy.onpremises.factory.filter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static org.eclipse.che.api.factory.server.FactoryConstants.INVALID_PARAMETER_MESSAGE;
import static org.eclipse.che.api.factory.server.FactoryConstants.PARAMETRIZED_ILLEGAL_PARAMETER_VALUE_MESSAGE;

/**
 * Validates request query params to contain only allowed ones.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class FactoryParamsFilter implements Filter {

    @Inject
    @Named("page.invalid.factory")
    protected String INVALID_FACTORY_URL_PAGE;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain)
            throws IOException, ServletException {

        final Map<String, String[]> params = req.getParameterMap();
        if (params.get("id") != null) {
            if (params.size() != 1) {
                dispatchToErrorPage(req, resp, INVALID_FACTORY_URL_PAGE, INVALID_PARAMETER_MESSAGE);
                return;
            }
            if (params.get("id").length != 1) {
                dispatchToErrorPage(req, resp, INVALID_FACTORY_URL_PAGE,
                                    String.format(PARAMETRIZED_ILLEGAL_PARAMETER_VALUE_MESSAGE, "id", params.get("id")[1]));
                return;
            }
        } else if (params.get("name") != null || params.get("user") != null) {
            if (params.size() != 2) {
                dispatchToErrorPage(req, resp, INVALID_FACTORY_URL_PAGE, INVALID_PARAMETER_MESSAGE);
                return;
            }

            if (params.get("name").length != 1) {
                dispatchToErrorPage(req, resp, INVALID_FACTORY_URL_PAGE,
                                    String.format(PARAMETRIZED_ILLEGAL_PARAMETER_VALUE_MESSAGE, "name", params.get("name")[1]));
                return;
            }
            if (params.get("user").length != 1) {
                dispatchToErrorPage(req, resp, INVALID_FACTORY_URL_PAGE,
                                    String.format(PARAMETRIZED_ILLEGAL_PARAMETER_VALUE_MESSAGE, "user", params.get("user")[1]));
                return;
            }

        } else {

            // check that we have unique value for each parameters
            // custom parameters will be checked at the next step when trying to retrieve it
            Iterator<Map.Entry<String, String[]>> iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String[]> element = iterator.next();
                String key = element.getKey();
                String[] array = element.getValue();
                // check it's only one item length
                if (array.length != 1) {
                    dispatchToErrorPage(req, resp, INVALID_FACTORY_URL_PAGE,
                                        String.format(PARAMETRIZED_ILLEGAL_PARAMETER_VALUE_MESSAGE, key, params.get(key)[1]));
                }
            }

        }
        filterChain.doFilter(req, resp);
    }

    protected void dispatchToErrorPage(ServletRequest req, ServletResponse resp, String dispatchPath, String message)
            throws ServletException, IOException {
        if (message != null) {
            req.setAttribute(RequestDispatcher.ERROR_MESSAGE, message);
        }
        req.getRequestDispatcher(dispatchPath).forward(req, resp);
    }
}
