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
package com.codenvy.api.permission.shared.model;

import java.util.List;

/**
 * Describes permissions domain
 *
 * @author Sergii Leschenko
 * @author gazarenkov
 */
public interface PermissionsDomain {
    /**
     * @return id of permissions domain
     */
    String getId();

    /**
     * @return true if domain requires non nullable value for instance field or false otherwise
     */
    Boolean isInstanceRequired();

    /**
     * @return list actions which are allowed for domain
     */
    List<String> getAllowedActions();
}
