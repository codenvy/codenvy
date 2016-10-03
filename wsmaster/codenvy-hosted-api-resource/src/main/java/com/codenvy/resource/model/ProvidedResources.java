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

import org.eclipse.che.commons.annotation.Nullable;

import java.util.List;

/**
 * Resources that are provided for using by account by some resource providing mechanism
 *
 * @author Sergii Leschenko
 */
public interface ProvidedResources {

    /**
     * Returns id of resource provider
     */
    String getProviderId();

    /**
     * Returns id of granted resource entity
     *
     * Can be null when provider provides static single entry
     */
    @Nullable
    String getId();

    /**
     * Returns owner of license
     */
    String getOwner();

    /**
     * Returns time when license became active
     */
    Long getStartTime();

    /**
     * Returns time when license will be/became inactive
     */
    Long getEndTime();

    /**
     * Returns list of resources which can be used by owner
     */
    List<? extends Resource> getResources();
}
