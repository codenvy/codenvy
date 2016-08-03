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
package com.codenvy.resource.api.ram;

import com.codenvy.resource.spi.impl.AbstractResource;

import javax.persistence.Entity;

/**
 * Ram resources indicates how many RAM can be used by active workspaces of one account at the same time
 *
 * @author Sergii Leschenko
 */
@Entity(name = "RamResource")
public class RamResource extends AbstractResource {
    public RamResource() {
    }

    public RamResource(long amount) {
        super(amount);
    }

    @Override
    public String getType() {
        return RamResourceType.ID;
    }

    @Override
    public String getUnit() {
        return RamResourceType.UNIT;
    }
}
