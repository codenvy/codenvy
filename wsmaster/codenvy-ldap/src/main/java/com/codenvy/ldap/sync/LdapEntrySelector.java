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

import org.ldaptive.Connection;
import org.ldaptive.LdapEntry;

/**
 * Define a strategy for selecting ldap entries.
 *
 * <p>The interface allows to return such kind of {@link Iterable}
 * which requests data on demand and allows to process it
 * as a stream instead of keeping all of it in memory.
 *
 * @author Yevhenii Voevodin
 */
public interface LdapEntrySelector {

    /**
     * Selects ldap entries in implementation specific way.
     *
     * @param connection
     *         the connection which should be used for selection,
     *         it is already opened and shouldn't be closed
     * @return an iterable describing the result iterator
     * @throws SyncException
     *         when any error occurs during selection, or during iteration
     */
    Iterable<LdapEntry> select(Connection connection) throws SyncException;
}
