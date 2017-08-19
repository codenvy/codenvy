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
package com.codenvy.onpremises;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.ext.RuntimeDelegate;
import org.everrest.core.impl.RuntimeDelegateImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Initialize components of cloud ide site. */
public class OnPremisesIdeServletContextListener implements ServletContextListener {
  /** Class logger. */
  private static final Logger LOG =
      LoggerFactory.getLogger(OnPremisesIdeServletContextListener.class);

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {}
}
