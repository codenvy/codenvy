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
package com.codenvy.plugin.webhooks.bitbucketserver.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Represents reference change.
 *
 * @author Igor Vinokur
 */
@DTO
public interface RefChange {
    /**
     * Returns reference Id.
     */
    String getRefId();

    void setRefId(String refId);

    RefChange withRefId(String refId);

    /**
     * Returns hash of the head commit before the change was performed.
     */
    String getFromHash();

    void setFromHash(String fromHash);

    /**
     * Returns hash of the head commit after the change was performed.
     */
    String getToHash();

    void setToHash(String toHash);

    RefChange withToHash(String toHash);

    /**
     * Returns reference change type.
     */
    String getType();

    void setType(String type);

    RefChange withType(String type);
}
