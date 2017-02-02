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

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test of {@link MaintenanceStatusServlet} functionality.
 *
 * @author Mihail Kuznyetsov
 */
@Listeners(value = MockitoTestNGListener.class)
public class MaintenanceStatusServletTest {
    @Mock
    private HttpServletRequest req;

    @Mock
    private HttpServletResponse resp;

    @Mock
    private StatusPageContentProvider contentProvider;

    @Mock
    private PrintWriter responseWriter;

    private MaintenanceStatusServlet servlet;

    @BeforeMethod
    public void setup() {
        servlet = new MaintenanceStatusServlet(contentProvider);
    }

    @Test
    public void shouldWriteContentToResponse() throws ServletException, IOException {
        when(contentProvider.getContent()).thenReturn("JSON");
        when(resp.getWriter()).thenReturn(responseWriter);

        servlet.doGet(req, resp);

        verify(resp).getWriter();
        verify(responseWriter).write(eq("JSON"));
    }

    @Test
    public void shouldWriteErrorToResponceWrappedInJson() throws ServletException, IOException {
        doThrow(new IOException("error message")).when(contentProvider).getContent();
        when(resp.getWriter()).thenReturn(responseWriter);

        servlet.doGet(req, resp);

        verify(resp).setStatus(eq(500));
        verify(resp).getWriter();
        verify(responseWriter).write(eq("{\"error\":\"error message\"}"));
    }
}
