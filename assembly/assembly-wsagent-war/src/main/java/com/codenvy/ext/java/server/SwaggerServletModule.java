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

import com.google.inject.servlet.ServletModule;
import org.eclipse.che.inject.DynaModule;

/** @author Sergii Kabashniuk */
@DynaModule
public class SwaggerServletModule extends ServletModule {
  @Override
  protected void configureServlets() {
    install(new org.eclipse.che.swagger.deploy.DocsModule());
    serve("/swaggerinit").with(AgentSwaggerConfig.class);
  }
}
