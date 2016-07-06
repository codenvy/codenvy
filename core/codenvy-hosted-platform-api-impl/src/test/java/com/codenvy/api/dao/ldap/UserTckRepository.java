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
package com.codenvy.api.dao.ldap;

import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.InitialLdapContext;
import java.util.Collection;

import static com.codenvy.api.dao.ldap.LdapCloser.close;

/**
 * @author Yevhenii Voevodin
 */
public class UserTckRepository extends AbstractLdapTckRepository<UserImpl> {

    private final UserAttributesMapper mapper;

    @Inject
    protected UserTckRepository(@Named("user.ldap.user_container_dn") String containerDn,
                                InitialLdapContextFactory contextFactory,
                                UserAttributesMapper mapper) {
        super(mapper.userIdAttr, containerDn, mapper.userObjectClasses, contextFactory);
        this.mapper = mapper;
    }

    @Override
    public void createAll(Collection<? extends UserImpl> entities) throws TckRepositoryException {
        try (LdapCloser.CloseableSupplier<InitialLdapContext> contextSup = LdapCloser.wrapCloseable(contextFactory.createContext())) {
            for (UserImpl user : entities) {
                try {
                    final Attributes attributes = mapper.toAttributes(user);
                    close(contextSup.get().createSubcontext(normalizeDn(user.getId()), attributes));
                } catch (NamingException x) {
                    throw new TckRepositoryException(x.getMessage(), x);
                }
            }
        } catch (NamingException x) {
            throw new TckRepositoryException(x.getMessage(), x);
        }
    }
}
