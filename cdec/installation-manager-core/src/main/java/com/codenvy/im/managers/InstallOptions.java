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
package com.codenvy.im.managers;

import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

/**
 * @author Anatoliy Bazko
 */
public class InstallOptions {
    private int                 step;

    @ApiModelProperty(required = true)
    private InstallType         installType;

    @ApiModelProperty(required = true)
    private Map<String, String> configProperties;

    /** Getter for {@link #installType} */
    public InstallType getInstallType() {
        return installType;
    }

    /** Setter for {@link #installType} */
    public InstallOptions setInstallType(InstallType installType) {
        this.installType = installType;
        return this;
    }

    /** Getter for {@link #step} */
    public int getStep() {
        return step;
    }

    /** Setter for {@link #step} */
    public InstallOptions setStep(int step) {
        this.step = step;
        return this;
    }

    /** Getter for {@link #configProperties} */
    public Map<String, String> getConfigProperties() {
        return configProperties;
    }

    /** Setter for {@link #configProperties} */
    public InstallOptions setConfigProperties(Map<String, String> configProperties) {
        this.configProperties = configProperties;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InstallOptions that = (InstallOptions)o;

        if (configProperties != null ? !configProperties.equals(that.configProperties) : that.configProperties != null) {
            return false;
        }

        if (step != that.step) {
            return false;
        }

        if (installType != that.installType) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = step;
        result = 31 * result + (installType != null ? installType.hashCode() : 0);
        result = 31 * result + (configProperties != null ? configProperties.hashCode() : 0);

        return result;
    }
}
