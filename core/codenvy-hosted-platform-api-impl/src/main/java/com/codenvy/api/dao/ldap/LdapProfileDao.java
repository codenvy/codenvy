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

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.InitialLdapContext;


import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * LDAP based implementation of {@link ProfileDao}.
 *
 * @author Eugene Voevodin
 */
@Singleton
public class LdapProfileDao implements ProfileDao {

    private final InitialLdapContextFactory contextFactory;
    private final ProfileAttributesMapper   attributesMapper;

    @Inject
    public LdapProfileDao(InitialLdapContextFactory contextFactory, ProfileAttributesMapper attributesMapper) {
        this.contextFactory = contextFactory;
        this.attributesMapper = attributesMapper;
    }

    /**
     * Not supported for this implementation, nothing will be done
     */
    @Override
    public void create(ProfileImpl profile) throws ServerException {
        try {
            update(profile);
        } catch (NotFoundException e) {
            throw new ServerException("Unable to create profile for non-existent user");
        }
    }

    /**
     * Not supported for this implementation, nothing will be done
     */
    @Override
    public void remove(String id) throws ServerException {
    }

    @Override
    public void update(ProfileImpl profile) throws NotFoundException, ServerException {
        requireNonNull(profile, "Required non-null profile");
        final String id = profile.getUserId();
        final ProfileImpl existing = getById(id);
        try {
            final ModificationItem[] mods = attributesMapper.createModifications(existing.getAttributes(), profile.getAttributes());
            if (mods.length > 0) {
                try (CloseableSupplier<InitialLdapContext> contextSup = LdapCloser.wrapCloseable(contextFactory.createContext())) {
                    contextSup.get().modifyAttributes(attributesMapper.getProfileDn(id), mods);
                }
            }
        } catch (NamingException ex) {
            throw new ServerException(format("Unable to update profile '%s'", profile.getUserId()));
        }
    }

    @Override
    public ProfileImpl getById(String id) throws NotFoundException, ServerException {
        requireNonNull(id, "Required non-null id");
        try {
            final ProfileImpl profile = doGetById(id);
            if (profile == null) {
                throw new NotFoundException(format("Profile with id '%s' was not found", id));
            }
            return profile;
        } catch (NamingException namingEx) {
            throw new ServerException(format("Unable to get profile '%s'", id), namingEx);
        }
    }

    private ProfileImpl doGetById(String id) throws NamingException {
        try (CloseableSupplier<InitialLdapContext> contextSup = LdapCloser.wrapCloseable(contextFactory.createContext()))  {
            final Attributes attributes = getProfileAttributes(contextSup.get(), id);
            if (attributes != null) {
                return attributesMapper.asProfile(attributes);
            }
        }
        return null;
    }

    private Attributes getProfileAttributes(InitialLdapContext ctx, String id) throws NamingException {
        try {
            return ctx.getAttributes(attributesMapper.getProfileDn(id));
        } catch (NameNotFoundException nnfEx) {
            return null;
        }
    }
}
