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
package com.codenvy.organization.shared.model;

import com.codenvy.resource.model.Resource;

import java.util.List;

/**
 * Defines resources which are distributed for suborganization by parent organization
 *
 * @author Sergii Leschenko
 */
public interface OrganizationDistributedResources {
    /**
     * Id of organization that owns these distributed resources
     */
    String getOrganizationId();

    /**
     * Returns resources that can be used.
     */
    List<? extends Resource> getResources();
}
