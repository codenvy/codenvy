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
package com.codenvy.resource.model;

import java.util.List;

/**
 * Permits account to use some resources.
 *
 * @author gazarenkov
 * @author Sergii Leschenko
 */
public interface AccountLicense {
    /**
     * Returns id of account that is owner of this license.
     */
    String getAccountId();

    /**
     * Returns detailed list of resources which can be used by owner.
     */
    List<? extends ProvidedResources> getResourcesDetails();

    /**
     * Returns list of resources which can be used by owner.
     */
    List<? extends Resource> getTotalResources();
}
