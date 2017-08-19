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
package org.eclipse.che.ide.ext.microsoft.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.ext.microsoft.client.MicrosoftServiceClient;
import org.eclipse.che.ide.ext.microsoft.client.MicrosoftServiceClientImpl;

/** @author Mihail Kuznyetsov */
@ExtensionGinModule
public class MicrosoftGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(MicrosoftServiceClient.class).to(MicrosoftServiceClientImpl.class).in(Singleton.class);
  }
}
