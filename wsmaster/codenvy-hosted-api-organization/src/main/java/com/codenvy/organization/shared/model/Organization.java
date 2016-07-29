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

import org.eclipse.che.commons.annotation.Nullable;

/**
 * Describes group of users that can use common resources
 *
 * @author gazarenkov
 * @author Sergii Leschenko
 */
public interface Organization {

    /**
     * Returns the identifier of the organization (e.g. "organization0x1234567890").
     * The identifier value is unique and mandatory.
     */
    String getId();

    /**
     * Returns name of organization.
     * The name is unique, mandatory and updatable.
     * //TODO Add description of unique scope
     */
    String getName();

    /**
     * Returns id of parent organization.
     * The returned value can be nullable in case when organization is root
     */
    @Nullable
    String getParent();
}
