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
package com.codenvy.service.system;

import org.eclipse.che.api.core.ServerException;

/**
 * Provides system RAM information. The specific of system RAM is depends on the implementation.
 *
 * @author Igor Vinokur
 */
public interface SystemRamInfoProvider {

  /**
   * Returns {@link SystemRamInfo} object, that describes system RAM values and properties.
   *
   * @throws ServerException if failed to retrieve system RAM values
   */
  SystemRamInfo getSystemRamInfo() throws ServerException;
}
