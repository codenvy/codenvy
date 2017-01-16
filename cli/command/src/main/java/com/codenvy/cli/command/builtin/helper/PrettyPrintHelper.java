/*******************************************************************************
 * Copyright (c) [2012] - [2017] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.cli.command.builtin.helper;

import com.codenvy.cli.command.builtin.model.UserBuilderStatus;
import com.codenvy.cli.command.builtin.model.UserRunnerStatus;

import java.util.List;

/**
 * Allows to convert permissions to pretty print permissions
 * @author Florent Benoit
 */
public class PrettyPrintHelper {

    /**
     * Utility class, no public constructor.
     */
    private PrettyPrintHelper() {

    }


    public static String prettyPrint(List<String> userPermissions) {
        if (userPermissions == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder("");

        if (userPermissions.contains("read")) {
            sb.append("R");
        }
        if (userPermissions.contains("write")) {
            sb.append("W");
        }

        // build permission if before the run
        if (userPermissions.contains("build")) {
            sb.append("B");
        }

        if (userPermissions.contains("run")) {
            sb.append("X");
        }

        // Update ACL should be the last permission
        if (userPermissions.contains("update_acl")) {
            sb.append("U");
        }

        return sb.toString();
    }

    public static String prettyPrintState(UserRunnerStatus runnerStatus) {
        StringBuilder sb = new StringBuilder(runnerStatus.shortId());
        sb.append("[");
        switch (runnerStatus.getInnerStatus().status()) {
            case RUNNING:
                sb.append("RUN");
                break;
            case CANCELLED:
                sb.append("CANCEL");
                break;
            case NEW:
                sb.append("WAIT");
                break;
            case FAILED:
                sb.append("FAIL");
                break;
            case STOPPED:
                sb.append("STOP");
                break;
        }
        sb.append("]");
        return sb.toString();
    }

    public static String prettyPrintState(UserBuilderStatus builderStatus) {
        StringBuilder sb = new StringBuilder(builderStatus.shortId());
        sb.append("[");
        switch (builderStatus.getInnerStatus().status()) {
            case IN_QUEUE:
                sb.append("WAIT");
                break;
            case IN_PROGRESS:
                sb.append("RUN");
                break;
            case FAILED:
                sb.append("FAIL");
                break;
            case CANCELLED:
                sb.append("CANCEL");
                break;
            case SUCCESSFUL:
                sb.append("OK");
                break;
        }
        sb.append("]");
        return sb.toString();
    }
}
