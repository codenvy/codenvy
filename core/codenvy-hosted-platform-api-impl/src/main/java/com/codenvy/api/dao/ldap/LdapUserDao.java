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
import com.codenvy.api.event.user.RemoveUserEvent;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.UserDao;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.naming.AuthenticationException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import java.util.HashSet;
import java.util.Set;

import static com.codenvy.api.dao.ldap.LdapCloser.close;
import static com.codenvy.api.dao.ldap.LdapCloser.wrapCloseable;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * LDAP based implementation of {@code UserDao}.
 *
 * @author andrew00x
 */
@Singleton
public class LdapUserDao implements UserDao {
    protected final String                    userObjectClassFilter;
    protected final String                    containerDn;
    protected final String                    userDn;
    protected final EventService              eventService;
    protected final UserAttributesMapper      mapper;
    protected final InitialLdapContextFactory contextFactory;


    /**
     * Creates new instance of {@code LdapUserDao}.
     *
     * @param userContainerDn
     *         full name of root object for user records, e.g. {@code ou=People,dc=codenvy,dc=com}
     * @param userAttributesMapper
     *         UserAttributesMapper
     */
    @Inject
    public LdapUserDao(InitialLdapContextFactory contextFactory,
                       @Named("user.ldap.user_container_dn") String userContainerDn,
                       @Named("user.ldap.user_dn") String userDn,
                       UserAttributesMapper userAttributesMapper,
                       EventService eventService) {
        this.contextFactory = contextFactory;
        this.containerDn = userContainerDn;
        this.userDn = userDn;
        this.mapper = userAttributesMapper;
        this.eventService = eventService;
        final StringBuilder sb = new StringBuilder();
        for (String objectClass : userAttributesMapper.userObjectClasses) {
            sb.append("(objectClass=");
            sb.append(objectClass);
            sb.append(')');
        }
        this.userObjectClassFilter = sb.toString();
    }

    @Override
    public String authenticate(String alias, String password) throws UnauthorizedException, ServerException {
        requireNonNull(alias, "Required non-null alias");
        requireNonNull(password, "Required non-null password");
        UserImpl user;
        try {
            user = doGetByAttribute(mapper.userEmailAttr, alias);
            if (user == null) {
                user = doGetByAttribute(mapper.userNameAttr, alias);
            }
            if (user == null) {
                user = doGetByAttribute(mapper.userAliasesAttr, alias);
            }
            if (user == null) {
                throw new UnauthorizedException(format("User '%s' doesn't exist", alias));
            }
            final String principal = formatDn(userDn, user.getId());
            try (CloseableSupplier<InitialLdapContext> ignored = wrapCloseable(contextFactory.createContext(principal, password))) {
                return user.getId();
            }
        } catch (AuthenticationException x) {
            throw new UnauthorizedException(format("User '%s' doesn't exist", alias));
        } catch (NamingException e) {
            throw new ServerException(format("Error during authentication of user '%s'", alias), e);
        }
    }

    @Override
    public void create(UserImpl user) throws ConflictException, ServerException {
        requireNonNull(user, "Required non-null user");
        try (CloseableSupplier<InitialLdapContext> contextSup = wrapCloseable(contextFactory.createContext())) {
            final String email = user.getEmail();
            if (email != null && doGetByAttribute(mapper.userEmailAttr, email) != null) {
                throw new ConflictException(format("User with email '%s' already exists", email));
            }
            for (String alias : user.getAliases()) {
                if (doGetByAttribute(mapper.userAliasesAttr, alias) != null) {
                    throw new ConflictException(format("User with alias '%s' already exists", alias));
                }
            }
            //TODO consider null value for user name
            if (user.getName() != null && doGetByAttribute(mapper.userNameAttr, user.getName()) != null) {
                throw new ConflictException(format("User with name '%s' already exists", user.getName()));
            }
            close(contextSup.get().createSubcontext(formatDn(userDn, user.getId()), mapper.toAttributes(user)));
        } catch (NameAlreadyBoundException e) {
            throw new ConflictException(format("Unable create new user '%s'. User already exists", user.getId()));
        } catch (NamingException e) {
            throw new ServerException(format("Unable create new user '%s'", user.getEmail()), e);
        }
    }

    @Override
    public void update(UserImpl update) throws NotFoundException, ServerException, ConflictException {
        requireNonNull(update, "Required non-null update");
        final String id = update.getId();
        try {
            final UserImpl target = doGetById(id);

            //check that user exists
            if (target == null) {
                throw new NotFoundException("User " + id + " was not found");
            }

            //check that new aliases are unique
            final Set<String> newAliases = new HashSet<>(update.getAliases());
            newAliases.removeAll(target.getAliases());
            for (String alias : newAliases) {
                if (doGetByAttribute(mapper.userAliasesAttr, alias) != null) {
                    throw new ConflictException(format("Unable update user '%s', alias '%s' is already in use", id, alias));
                }
            }

            //check that new name is unique
            if (!target.getName().equals(update.getName())
                && doGetByAttribute(mapper.userNameAttr, update.getName()) != null) {
                throw new ConflictException(format("Unable update user '%s', name '%s' already in use", id, update.getName()));
            }

            //check that new email is unique
            if (!target.getEmail().equals(update.getEmail())
                && doGetByAttribute(mapper.userEmailAttr, update.getEmail()) != null) {
                throw new ConflictException(format("Unable update user '%s', email '%s' already in use", id, update.getName()));
            }

            final ModificationItem[] mods = mapper.createModifications(target, update);
            if (mods.length > 0) {
                try (CloseableSupplier<InitialLdapContext> contextSup = wrapCloseable(contextFactory.createContext())) {
                    contextSup.get().modifyAttributes(formatDn(userDn, id), mods);
                } catch (NamingException e) {
                    throw new ServerException(format("Unable update (user) '%s'", update.getEmail()), e);
                }
            }
        } catch (NamingException e) {
            throw new ServerException(format("Unable update user '%s'", update.getEmail()), e);
        }
    }

    @Override
    public void remove(String id) throws ServerException, ConflictException {
        requireNonNull(id, "Required non-null id");
        try (CloseableSupplier<InitialLdapContext> contextSup = wrapCloseable(contextFactory.createContext())) {
            contextSup.get().destroySubcontext(formatDn(userDn, id));
            eventService.publish(new RemoveUserEvent(id));
        } catch (NameNotFoundException e) {
            // according to the interface contract it is okay if user doesn't exist
        } catch (NamingException e) {
            throw new ServerException(format("Unable remove user '%s'", id), e);
        }
    }

    @Override
    public UserImpl getByAlias(String alias) throws NotFoundException, ServerException {
        requireNonNull(alias, "Required non-null alias");
        try {
            final User user = doGetByAttribute(mapper.userAliasesAttr, alias);
            if (user == null) {
                throw new NotFoundException("User not found " + alias);
            }
            return new UserImpl(user);
        } catch (NamingException e) {
            throw new ServerException(format("Unable get user '%s'", alias), e);
        }
    }

    @Override
    public UserImpl getById(String id) throws NotFoundException, ServerException {
        requireNonNull(id, "Required non-null id");
        try {
            final User user = doGetById(id);
            if (user == null) {
                throw new NotFoundException("User " + id + " was not found ");
            }
            return new UserImpl(user);
        } catch (NamingException e) {
            throw new ServerException(format("Unable get user '%s'", id), e);
        }
    }

    @Override
    public UserImpl getByName(String name) throws NotFoundException, ServerException {
        requireNonNull(name, "Required non-null name");
        try {
            final User user = doGetByAttribute(mapper.userNameAttr, name);
            if (user == null) {
                throw new NotFoundException("User " + name + " was not found ");
            }
            return new UserImpl(user);
        } catch (NamingException e) {
            throw new ServerException(format("Unable get user '%s'", name), e);
        }
    }

    @Override
    public UserImpl getByEmail(String email) throws NotFoundException, ServerException {
        requireNonNull(email, "Required non-null email");
        try {
            final UserImpl user = doGetByAttribute(mapper.userEmailAttr, email);
            if (user == null) {
                throw new NotFoundException("User " + email + " was not found ");
            }
            return new UserImpl(user);
        } catch (NamingException e) {
            throw new ServerException(format("Unable get user '%s'", email), e);
        }
    }

    private UserImpl doGetByAttribute(String name, String value) throws NamingException {
        try (CloseableSupplier<InitialLdapContext> contextSup = wrapCloseable(contextFactory.createContext())) {
            final Attributes attributes = getUserAttributesByFilter(contextSup.get(), createFilter(name, value));
            if (attributes != null) {
                return mapper.fromAttributes(attributes);
            }
        }
        return null;
    }

    private UserImpl doGetById(String id) throws NamingException {
        try (CloseableSupplier<InitialLdapContext> contextSup = wrapCloseable(contextFactory.createContext())) {
            final Attributes attributes = getUserAttributesById(contextSup.get(), id);
            if (attributes != null) {
                return mapper.fromAttributes(attributes);
            }
        }
        return null;
    }

    private Attributes getUserAttributesById(InitialLdapContext context, String id) throws NamingException {
        try {
            //try to find user using dn
            return context.getAttributes(formatDn(userDn, id));
        } catch (NameNotFoundException nfEx) {
            return null;
        }
    }

    private String formatDn(String userDn, String id) {
        return userDn + '=' + id + ',' + containerDn;
    }

    private String createFilter(String attribute, String value) {
        return "(&(" + attribute + '=' + value + ")(" + userObjectClassFilter + "))";
    }

    private Attributes getUserAttributesByFilter(InitialLdapContext context, String filter) throws NamingException {
        final SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        try (CloseableSupplier<NamingEnumeration<SearchResult>> enumSup = wrapCloseable(context.search(containerDn, filter, controls))) {
            final NamingEnumeration<SearchResult> enumeration = enumSup.get();
            if (enumeration.hasMore()) {
                return enumeration.nextElement().getAttributes();
            }
            return null;
        }
    }
}
