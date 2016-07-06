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

import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import javax.inject.Inject;
import javax.naming.NamingException;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.InitialLdapContext;
import java.util.Collection;
import java.util.Collections;

import static com.codenvy.api.dao.ldap.LdapCloser.wrapCloseable;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;

/**
 * @author Yevhenii Voevodin
 */
public class ProfileTckRepository extends AbstractLdapTckRepository<ProfileImpl> {

    private final ProfileAttributesMapper mapper;
    private final UserTckRepository       userTckRepository;

    @Inject
    protected ProfileTckRepository(InitialLdapContextFactory contextFactory,
                                   ProfileAttributesMapper mapper,
                                   UserAttributesMapper userAttributesMapper,
                                   UserTckRepository userTckRepository) {
        super(mapper.profileDn,
              mapper.profileContainerDn,
              userAttributesMapper.userObjectClasses,
              contextFactory);
        this.mapper = mapper;
        this.userTckRepository = userTckRepository;
    }

    @Override
    public void createAll(Collection<? extends ProfileImpl> profiles) throws TckRepositoryException {
        // Profile dao modifies user records so it is needed to create users before creating profiles
        userTckRepository.createAll(profiles.stream()
                                            .map(profile -> new UserImpl(profile.getUserId(),
                                                                         profile.getUserId() + "@codenvy.com",
                                                                         profile.getUserId(),
                                                                         "password",
                                                                         Collections.emptyList()))
                                            .collect(toList()));
        for (ProfileImpl profile : profiles) {
            try (CloseableSupplier<InitialLdapContext> contextSup = wrapCloseable(contextFactory.createContext())) {
                final InitialLdapContext context = contextSup.get();
                final ModificationItem[] modifications = mapper.createModifications(singletonMap("lastName", "<none>"),
                                                                                    profile.getAttributes());
                context.modifyAttributes(normalizeDn(profile.getUserId()), modifications);
            } catch (NamingException x) {
                throw new TckRepositoryException(x.getMessage(), x);
            }
        }
    }
}

