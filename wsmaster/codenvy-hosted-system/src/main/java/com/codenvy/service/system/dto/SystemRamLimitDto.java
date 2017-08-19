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
package com.codenvy.service.system.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Dto to describe system RAM limit status.
 *
 * @author Igor Vinokur
 */
@DTO
public interface SystemRamLimitDto {

  /** Returns {@code true} if system RAM limit is exceeded, otherwise returns {@code false}. */
  boolean isSystemRamLimitExceeded();

  /** Define if the system RAM limit is exceeded or not. */
  void setSystemRamLimitExceeded(boolean systemRamLimitExceeded);

  /** Returns {@link SystemRamLimitDto} with defined system RAM limit state. */
  SystemRamLimitDto withSystemRamLimitExceeded(boolean systemRamLimitExceeded);
}
