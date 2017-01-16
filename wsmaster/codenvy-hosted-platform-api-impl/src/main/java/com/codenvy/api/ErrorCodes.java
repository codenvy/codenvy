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
package com.codenvy.api;

/**
 * Defines error codes, defined codes MUST NOT conflict with
 * existing {@link org.eclipse.che.api.core.ErrorCodes}, error codes must
 * be in range <b>10000-14999</b> inclusive.
 *
 * @author Yevhenii Voevodin
 */
public final class ErrorCodes {

    public static final int LIMIT_EXCEEDED = 10000;

    private ErrorCodes() {}
}
