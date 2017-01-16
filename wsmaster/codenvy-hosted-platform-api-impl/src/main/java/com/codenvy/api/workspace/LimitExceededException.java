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
package com.codenvy.api.workspace;

import com.codenvy.api.ErrorCodes;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.shared.dto.ExtendedError;
import org.eclipse.che.dto.server.DtoFactory;

import java.util.Map;

/**
 * Should be thrown when limit of some resource is exceeded
 * e.g. the ram per workspace.
 *
 * @author Yevhenii Voevodin
 * @see LimitsCheckingWorkspaceManager
 */
public class LimitExceededException extends ServerException {

    public LimitExceededException(String message) {
        super(message);
    }

    public LimitExceededException(String message, Map<String, String> attributes) {
        super(DtoFactory.newDto(ExtendedError.class)
                        .withMessage(message)
                        .withAttributes(attributes)
                        .withErrorCode(ErrorCodes.LIMIT_EXCEEDED));
    }
}
