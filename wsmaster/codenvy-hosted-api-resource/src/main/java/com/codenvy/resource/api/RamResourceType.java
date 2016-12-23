/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.resource.api;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Describes resource type that control number of RAM
 * which can be used by running workspaces at the same time.
 *
 * @author Sergii Leschenko
 */
public class RamResourceType extends AbstractExhaustibleResource {
    public static final String ID   = "RAM";
    public static final String UNIT = "mb";

    private static final Set<String> SUPPORTED_UNITS = ImmutableSet.of(UNIT);

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDescription() {
        return "Number of RAM which can be used by running workspaces at the same time";
    }

    @Override
    public Set<String> getSupportedUnits() {
        return SUPPORTED_UNITS;
    }

    @Override
    public String getDefaultUnit() {
        return UNIT;
    }
}
