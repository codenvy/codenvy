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
package org.eclipse.che.ide.ext.microsoft.server.inject;

import com.google.inject.AbstractModule;
import org.eclipse.che.ide.ext.microsoft.server.MicrosoftVstsRestClient;
import org.eclipse.che.ide.ext.microsoft.server.rest.MicrosoftVstsService;

/** @author Mihail Kuznyetsov */
public class MicrosoftModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(MicrosoftVstsRestClient.class);
    bind(MicrosoftVstsService.class);
  }
}
