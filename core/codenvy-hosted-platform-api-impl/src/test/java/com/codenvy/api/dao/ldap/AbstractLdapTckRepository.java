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

import com.codenvy.api.dao.ldap.LdapCloser.CloseableSupplier;

import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import java.util.LinkedList;
import java.util.List;

import static com.codenvy.api.dao.ldap.LdapCloser.wrapCloseable;

public abstract class AbstractLdapTckRepository<T> implements TckRepository<T> {

    protected final String                    dn;
    protected final String[]                  objectClass;
    protected final String                    containerDn;
    protected final InitialLdapContextFactory contextFactory;

    protected AbstractLdapTckRepository(String dn,
                                        String containerDn,
                                        String[] objectClass,
                                        InitialLdapContextFactory contextFactory) {
        this.dn = dn;
        this.objectClass = objectClass;
        this.containerDn = containerDn;
        this.contextFactory = contextFactory;
    }

    @Override
    public void removeAll() throws TckRepositoryException {
        for (String id : getAllIds()) {
            try (CloseableSupplier<InitialLdapContext> contextSup = wrapCloseable(contextFactory.createContext())) {
                contextSup.get().destroySubcontext(normalizeDn(id));
            } catch (NamingException x) {
                throw new TckRepositoryException(x.getMessage(), x);
            }
        }
    }

    protected String normalizeDn(String id) {
        return dn + '=' + id + ',' + containerDn;
    }

    private List<String> getAllIds() throws TckRepositoryException {
        final List<String> result = new LinkedList<>();
        try (CloseableSupplier<InitialLdapContext> contextSup = wrapCloseable(contextFactory.createContext())) {
            // Configure search controls
            final SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(new String[] {dn});

            // Search the entities
            try (CloseableSupplier<NamingEnumeration<SearchResult>> enumSup = wrapCloseable(
                    contextSup.get().search(containerDn, "(objectClass=" + objectClass[0] + ')', controls))) {
                final NamingEnumeration<SearchResult> searchRes = enumSup.get();
                while (searchRes.hasMore()) {
                    final SearchResult next = searchRes.next();
                    final Attribute uid = next.getAttributes().get(dn);
                    result.add(uid.getAll().next().toString());
                }
            }
        } catch (NamingException x) {
            throw new TckRepositoryException(x.getLocalizedMessage(), x);
        }
        return result;
    }
}
