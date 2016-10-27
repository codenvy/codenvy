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

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * Helps to make db requests.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class DBHelper {

    @Inject
    private Provider<EntityManager> emProvider;

    /**
     * Executes native query and returns execution result.
     *
     * @param nativeQuery
     *         query to execute
     * @return execution result
     */
    @Transactional
    public List executeNativeQuery(String nativeQuery) {
        return emProvider.get()
                         .createNativeQuery(nativeQuery)
                         .getResultList();
    }
}
