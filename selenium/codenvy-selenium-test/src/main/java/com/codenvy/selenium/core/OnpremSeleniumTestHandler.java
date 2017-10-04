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
package com.codenvy.selenium.core;

import com.codenvy.selenium.OnpremSeleniumWebDriverRelatedModule;
import com.google.inject.Module;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.selenium.core.inject.SeleniumTestHandler;

/** @author Anatolii Bazko */
public class OnpremSeleniumTestHandler extends SeleniumTestHandler {

  @Override
  public List<Module> getParentModules() {
    List<Module> modules = new ArrayList<>();
    modules.add(new OnpremSeleniumSuiteModule());
    return modules;
  }

  @Override
  public List<Module> getChildModules() {
    List<Module> modules = new ArrayList<>();
    modules.add(new OnpremSeleniumWebDriverRelatedModule());
    return modules;
  }
}
