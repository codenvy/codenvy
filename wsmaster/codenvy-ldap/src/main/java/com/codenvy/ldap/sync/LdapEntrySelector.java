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
