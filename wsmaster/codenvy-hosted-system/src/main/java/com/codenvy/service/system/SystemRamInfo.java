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

import java.util.Objects;

/**
 * Describes system RAM values and properties.
 *
 * @author Igor Vinokur
 */
public class SystemRamInfo {

  private final long systemRamUsed;
  private final long systemRamTotal;
  private final boolean isSystemRamLimitExceeded;

  public SystemRamInfo(long systemRamUsed, long systemRamTotal) {
    this.systemRamUsed = systemRamUsed;
    this.systemRamTotal = systemRamTotal;
    this.isSystemRamLimitExceeded = systemRamTotal * 0.9 < systemRamUsed;
  }

  /** Total system RAM amount in Bytes. */
  public long getSystemRamTotal() {
    return systemRamTotal;
  }

  /** Used system RAM amount in Bytes. */
  public long getSystemRamUsed() {
    return systemRamUsed;
  }

  /**
   * Returns {@code true} if there is less then 10% of free RAM is present in the system, otherwise
   * returns {@code false}.
   */
  public boolean isSystemRamLimitExceeded() {
    return isSystemRamLimitExceeded;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SystemRamInfo)) return false;

    SystemRamInfo other = (SystemRamInfo) o;

    return systemRamUsed == other.systemRamUsed
        && systemRamTotal == other.systemRamTotal
        && isSystemRamLimitExceeded == other.isSystemRamLimitExceeded;
  }

  @Override
  public int hashCode() {
    return Objects.hash(systemRamUsed, systemRamTotal, isSystemRamLimitExceeded);
  }

  @Override
  public String toString() {
    return "SystemRamInfo{"
        + "systemRamUsed="
        + systemRamUsed
        + ", systemRamTotal="
        + systemRamTotal
        + ", isSystemRamLimitExceeded="
        + isSystemRamLimitExceeded
        + '}';
  }
}
