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
package com.codenvy.api.permission.server.spi.tck;

import com.codenvy.api.permission.server.model.impl.PermissionsImpl;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.List;

/**
 * @author Max Shaposhnik
 */

@Entity(name = "TestPermissionsImpl1")
@DiscriminatorValue("domain1")
public class TestPermissionsImpl1 extends PermissionsImpl {

    public TestPermissionsImpl1() {

    }

    public TestPermissionsImpl1(String userId, String domainId, String instanceId, List<String> actions) {
        super(userId,domainId, instanceId, actions);
    }

}
