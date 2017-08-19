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
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
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
