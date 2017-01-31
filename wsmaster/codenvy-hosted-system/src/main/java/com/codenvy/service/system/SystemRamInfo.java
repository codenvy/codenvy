/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.service.system;

import java.util.Objects;

/**
 * Describes system RAM values and properties.
 *
 * @author Igor Vinokur
 */
public class SystemRamInfo {

    private final long    systemRamUsed;
    private final long    systemRamTotal;
    private final boolean isSystemRamLimitExceeded;

    public SystemRamInfo(long systemRamUsed, long systemRamTotal) {
        this.systemRamUsed = systemRamUsed;
        this.systemRamTotal = systemRamTotal;
        this.isSystemRamLimitExceeded = systemRamTotal * 0.9 < systemRamUsed;
    }

    /**
     * Total system RAM amount in Bytes.
     */
    public long getSystemRamTotal() {
        return systemRamTotal;
    }

    /**
     * Used system RAM amount in Bytes.
     */
    public long getSystemRamUsed() {
        return systemRamUsed;
    }

    /**
     * Returns {@code true} if there is less then 10% of free RAM is present in the system, otherwise returns {@code false}.
     */
    public boolean isSystemRamLimitExceeded() {
        return isSystemRamLimitExceeded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SystemRamInfo)) return false;

        SystemRamInfo other = (SystemRamInfo)o;
        
        return systemRamUsed == other.systemRamUsed &&
               systemRamTotal == other.systemRamTotal &&
               isSystemRamLimitExceeded == other.isSystemRamLimitExceeded;
    }

    @Override
    public int hashCode() {
        return Objects.hash(systemRamUsed, systemRamTotal, isSystemRamLimitExceeded);
    }

    @Override
    public String toString() {
        return "SystemRamInfo{" +
               "systemRamUsed=" + systemRamUsed +
               ", systemRamTotal=" + systemRamTotal +
               ", isSystemRamLimitExceeded=" + isSystemRamLimitExceeded +
               '}';
    }
}
