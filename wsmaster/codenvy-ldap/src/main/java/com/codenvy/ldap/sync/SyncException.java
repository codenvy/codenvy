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
package com.codenvy.ldap.sync;

import org.ldaptive.LdapException;

import java.util.Iterator;

/**
 * Thrown when any synchronization error occurred.
 * Usually rethrows {@link LdapException} or another exceptions
 * occurred during operation executions, this exception extends
 * {@link RuntimeException} as it makes easier to create custom
 * {@link Iterator} implementations.
 * As the {@link LdapSynchronizer} is independent component this exception
 * is used for synchronizer only, its instances are not published.
 *
 * @author Yevhenii Voevodin
 */
public class SyncException extends RuntimeException {

    public SyncException(String message) {
        super(message);
    }

    public SyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
