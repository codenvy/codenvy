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
package com.codenvy.im.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Alexander Reshetnyak
 */
@JsonPropertyOrder({"stable", "unstable"})
public class AvailableVersionInfo implements Info {
    private String stable;
    private String unstable;

    public AvailableVersionInfo(){
    }

    public String getStable() {
        return stable;
    }

    public void setStable(String stable) {
        this.stable = stable;
    }

    public String getUnstable() {
        return unstable;
    }

    public void setUnstable(String unstable) {
        this.unstable = unstable;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AvailableVersionInfo)) {
            return false;
        }

        AvailableVersionInfo that = (AvailableVersionInfo)o;

        if (stable != null ? !stable.equals(that.stable) : that.stable != null) {
            return false;
        }
        if (unstable != null ? !unstable.equals(that.unstable) : that.unstable != null) {
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = stable != null ? stable.hashCode() : 0;
        result = 31 * result + (unstable != null ? unstable.hashCode() : 0);
        return result;
    }
}
