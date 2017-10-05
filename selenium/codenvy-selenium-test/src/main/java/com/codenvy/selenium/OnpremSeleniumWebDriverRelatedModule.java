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
package com.codenvy.selenium;

import com.codenvy.selenium.core.entrance.OnpremEntranceImpl;
import com.codenvy.selenium.pageobject.site.LoginAndCreateOnpremAccountPage;
import com.google.inject.AbstractModule;
import org.eclipse.che.selenium.core.entrance.Entrance;
import org.eclipse.che.selenium.pageobject.site.LoginPage;

public class OnpremSeleniumWebDriverRelatedModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(LoginPage.class).to(LoginAndCreateOnpremAccountPage.class);
    bind(Entrance.class).to(OnpremEntranceImpl.class);
  }
}
