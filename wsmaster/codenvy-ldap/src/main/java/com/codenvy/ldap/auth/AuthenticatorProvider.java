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
package com.codenvy.ldap.auth;

import com.google.common.base.Strings;

import org.eclipse.che.commons.annotation.Nullable;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.EntryResolver;
import org.ldaptive.auth.FormatDnResolver;
import org.ldaptive.auth.PooledBindAuthenticationHandler;
import org.ldaptive.auth.PooledCompareAuthenticationHandler;
import org.ldaptive.auth.PooledSearchDnResolver;
import org.ldaptive.control.PasswordPolicyControl;
import org.ldaptive.pool.PooledConnectionFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Create Authenticator based on container configuration.
 */
@Singleton
public class AuthenticatorProvider implements Provider<Authenticator> {

    private final Authenticator authenticator;

    private       String             baseDn;
    /**
     *  Type authentication to use.
     *  AD - Active Directory. Users authenticate with sAMAccountName.
     *  AUTHENTICATED - Authenticated Search.  Manager bind/search followed by user simple bind.
     *  ANONYMOUS -  Anonymous search followed by user simple bind.
     *  DIRECT -  Direct Bind. Compute user DN from format string and perform simple bind.
     *  SASL - SASL bind search.
     *
     * Configured over ldap.authenticationtype
     */
    private final AuthenticationType type;

    /**
     * Resolves an entry DN by using String#format. This resolver is typically used when an entry DN
     * can be formatted directly from the user identifier. For instance, entry DNs of the form
     * uid=dfisher,ou=people,dc=ldaptive,dc=org could be formatted
     * from uid=%s,ou=people,dc=ldaptive,dc=org.
     *
     * Configured over ldap.dnformat
     */
    private final String dnFormat;
    /**
     * Configuration of PooledCompareAuthenticationHandler.
     * Authenticates an entry DN by performing an LDAP compare operation on the userPassword attribute.
     * This authentication handler should be used in cases where you do not have authorization to perform
     * binds, but do have authorization to read the userPassword attribute.
     *
     * Configured over ldap.userpasswordattribute
     */
    private final String userPasswordAttribute;


    /**
     * search filter to execute; e.g. (mail={user}) Configured over ldap.userfilter.
     */
    private final String  userFilter;
    /**
     * whether to throw an exception if multiple entries are found with the search filter. Configured over ldap.allowmultipledns
     */
    private final boolean allowMultipleDns;
    /**
     * whether a subtree search should be performed.
     * Configured over ldap.subtreesearch
     */
    private final boolean subtreeSearch;

    @Inject
    public AuthenticatorProvider(PooledConnectionFactory connFactory,
                                 EntryResolver entryResolver,
                                 @NotNull @Named("ldap.base_dn") String baseDn,
                                 @NotNull @Named("ldap.auth.authentication_type") String type,
                                 @Nullable @Named("ldap.auth.dn_format") String dnFormat,
                                 @Nullable @Named("ldap.auth.user_password_attribute") String userPasswordAttribute,
                                 @Nullable @Named("ldap.auth.user.filter") String userFilter,
                                 @Nullable @Named("ldap.auth.allow_multiple_dns") String allowMultipleDns,
                                 @Nullable @Named("ldap.auth.subtree_search") String subtreeSearch) {
        this.baseDn = baseDn;
        this.type = AuthenticationType.valueOf(type);
        this.dnFormat = dnFormat;
        this.userPasswordAttribute = userPasswordAttribute;
        this.userFilter = userFilter;
        this.allowMultipleDns = Strings.isNullOrEmpty(allowMultipleDns) ? false : Boolean.valueOf(allowMultipleDns);
        this.subtreeSearch = Strings.isNullOrEmpty(subtreeSearch) ? false : Boolean.valueOf(subtreeSearch);
        this.authenticator = getAuthenticator(connFactory, entryResolver);
    }

    @Override
    public Authenticator get() {
        return authenticator;
    }

    private Authenticator getAuthenticator(PooledConnectionFactory connFactory,
                                           EntryResolver entryResolver) {
        switch (type) {
            case AD:
                return getActiveDirectoryAuthenticator(connFactory, entryResolver);
            case DIRECT:
                return getDirectBindAuthenticator(connFactory);
            case SASL:
                return getSaslAuthenticator(connFactory);
            case ANONYMOUS:
            case AUTHENTICATED:
            default:
                return getAuthenticatedOrAnonSearchAuthenticator(connFactory, entryResolver);
        }
    }

    private Authenticator getSaslAuthenticator(PooledConnectionFactory connFactory) {
        final PooledSearchDnResolver resolver = new PooledSearchDnResolver();
        resolver.setBaseDn(baseDn);
        resolver.setSubtreeSearch(subtreeSearch);
        resolver.setAllowMultipleDns(allowMultipleDns);
        resolver.setConnectionFactory(connFactory);
        resolver.setUserFilter(userFilter);
        return new Authenticator(resolver, getPooledBindAuthenticationHandler(connFactory));
    }

    private Authenticator getAuthenticatedOrAnonSearchAuthenticator(PooledConnectionFactory connFactory,
                                                                    EntryResolver entryResolver) {
        final PooledSearchDnResolver resolver = new PooledSearchDnResolver();
        resolver.setBaseDn(baseDn);
        resolver.setSubtreeSearch(subtreeSearch);
        resolver.setAllowMultipleDns(allowMultipleDns);
        resolver.setConnectionFactory(connFactory);
        resolver.setUserFilter(userFilter);

        final Authenticator auth;
        if (Strings.isNullOrEmpty(userPasswordAttribute)) {
            auth = new Authenticator(resolver, getPooledBindAuthenticationHandler(connFactory));
        } else {
            auth = new Authenticator(resolver, getPooledCompareAuthenticationHandler(connFactory));
        }
        auth.setEntryResolver(entryResolver);

        return auth;
    }

    private Authenticator getDirectBindAuthenticator(PooledConnectionFactory connFactory) {
        final FormatDnResolver resolver = new FormatDnResolver(dnFormat);
        return new Authenticator(resolver, getPooledBindAuthenticationHandler(connFactory));
    }

    private Authenticator getActiveDirectoryAuthenticator(PooledConnectionFactory connFactory,
                                                          EntryResolver entryResolver) {
        final FormatDnResolver resolver = new FormatDnResolver(dnFormat);
        final Authenticator authn = new Authenticator(resolver, getPooledBindAuthenticationHandler(connFactory));
        authn.setEntryResolver(entryResolver);
        return authn;
    }

    private PooledBindAuthenticationHandler getPooledBindAuthenticationHandler(PooledConnectionFactory connFactory) {
        final PooledBindAuthenticationHandler handler = new PooledBindAuthenticationHandler(connFactory);
        handler.setAuthenticationControls(new PasswordPolicyControl());
        return handler;
    }

    private PooledCompareAuthenticationHandler getPooledCompareAuthenticationHandler(PooledConnectionFactory connFactory) {
        final PooledCompareAuthenticationHandler handler = new PooledCompareAuthenticationHandler(
                connFactory);
        handler.setPasswordAttribute(userPasswordAttribute);
        return handler;
    }
}
