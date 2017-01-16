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
package org.eclipse.che.ide.ext.microsoft.shared;

import com.google.common.annotations.Beta;

/**
 * Error codes for Microsoft VSTS REST API.
 *
 * @author Yevhenii Voevodin
 */
@Beta
public final class VstsErrorCodes {

    public static final int PULL_REQUEST_ALREADY_EXISTS = 100;
    public static final int SOURCE_BRANCH_DOES_NOT_EXIST = 101;

    public static int getCodeByTypeKey(String typeKey) {
        if (typeKey == null) {
            return -1;
        }
        switch (typeKey) {
            case "GitPullRequestExistsException":
                return PULL_REQUEST_ALREADY_EXISTS;
            case "GitPullRequestStaleException":
                return SOURCE_BRANCH_DOES_NOT_EXIST;
            default:
                return -1;
        }
    }

    private VstsErrorCodes() {}
}
