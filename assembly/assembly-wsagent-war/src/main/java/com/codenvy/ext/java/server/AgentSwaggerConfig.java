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
package com.codenvy.ext.java.server;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.SwaggerContextService;
import java.net.MalformedURLException;
import java.net.URL;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes Swagger config with correct base path.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Singleton
public class AgentSwaggerConfig extends HttpServlet {

  private static final Logger LOG = LoggerFactory.getLogger(AgentSwaggerConfig.class);

  @Inject
  @Named("wsagent.endpoint")
  private String agentEndpoint;

  @Override
  public void init(ServletConfig config) throws ServletException {
    try {
      BeanConfig beanConfig = new BeanConfig();
      beanConfig.setVersion("1.0");
      beanConfig.setTitle("Codenvy");
      beanConfig.setBasePath(new URL(agentEndpoint).getPath());
      beanConfig.scanAndRead();
      new SwaggerContextService().withSwaggerConfig(beanConfig).initConfig().initScanner();
    } catch (MalformedURLException e) {
      LOG.warn("Unable to initialize swagger config due to malformed agent URL.", e);
    }
  }
}
