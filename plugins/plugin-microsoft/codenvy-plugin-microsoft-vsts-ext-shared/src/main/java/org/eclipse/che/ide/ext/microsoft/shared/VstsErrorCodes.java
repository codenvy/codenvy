/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
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
