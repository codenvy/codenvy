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
 * Represents limit of resources which are available for free usage by some account.
 *
 * @author Sergii Leschenko
 */
public interface FreeResourcesLimit {
    /**
     * Returns id of account that can use free resources.
     */
    String getAccountId();

    /**
     * Returns resources which are available for free usage.
     */
    List<? extends Resource> getResources();
}
