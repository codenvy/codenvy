/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.ldap.auth;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

import com.google.common.annotations.VisibleForTesting;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.inject.ConfigurationException;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.EntryResolver;
import org.ldaptive.auth.FormatDnResolver;
import org.ldaptive.auth.PooledBindAuthenticationHandler;
import org.ldaptive.auth.PooledCompareAuthenticationHandler;
import org.ldaptive.auth.PooledSearchDnResolver;
import org.ldaptive.control.PasswordPolicyControl;
import org.ldaptive.pool.PooledConnectionFactory;

/** Create Authenticator based on container configuration. */
@Singleton
public class AuthenticatorProvider implements Provider<Authenticator> {

  private final Authenticator authenticator;
  private static final String AUTH_TYPE_PROPERTY_NAME = "ldap.auth.authentication_type";
  private static final String DN_FORMAT_PROPERTY_NAME = "ldap.auth.dn_format";
  private static final String USER_FILTER_PROPERTY_NAME = "ldap.auth.user.filter";
  private static final String BASE_DN_PROPERTY_NAME = "ldap.base_dn";
  private static final String ALLOW_MULTIPLE_DNS_PROPERTY_NAME = "ldap.auth.allow_multiple_dns";
  private static final String USER_PASSWORD_ATTRIBUTE_PROPERTY_NAME =
      "ldap.auth.user_password_attribute";
  private static final String SUBTREE_SEARCH_PROPERTY_NAME = "ldap.auth.subtree_search";

  private String baseDn;
  /**
   * Type authentication to use. AD - Active Directory. Users authenticate with sAMAccountName.
   * AUTHENTICATED - Authenticated Search. Manager bind/search followed by user simple bind.
   * ANONYMOUS - Anonymous search followed by user simple bind. DIRECT - Direct Bind. Compute user
   * DN from format string and perform simple bind. SASL - SASL bind search.
   *
   * <p>Configured over ldap.authenticationtype
   */
  private final AuthenticationType type;

  /**
   * Resolves an entry DN by using String#format. This resolver is typically used when an entry DN
   * can be formatted directly from the user identifier. For instance, entry DNs of the form
   * uid=dfisher,ou=people,dc=ldaptive,dc=org could be formatted from
   * uid=%s,ou=people,dc=ldaptive,dc=org.
   *
   * <p>Configured over ldap.dnformat
   */
  private final String dnFormat;
  /**
   * Configuration of PooledCompareAuthenticationHandler. Authenticates an entry DN by performing an
   * LDAP compare operation on the userPassword attribute. This authentication handler should be
   * used in cases where you do not have authorization to perform binds, but do have authorization
   * to read the userPassword attribute.
   *
   * <p>Configured over ldap.userpasswordattribute
   */
  private final String userPasswordAttribute;

  /** search filter to execute; e.g. (mail={user}) Configured over ldap.userfilter. */
  private final String userFilter;
  /**
   * whether to throw an exception if multiple entries are found with the search filter. Configured
   * over ldap.allowmultipledns
   */
  private final boolean allowMultipleDns;
  /** whether a subtree search should be performed. Configured over ldap.subtreesearch */
  private final boolean subtreeSearch;

  @Inject
  public AuthenticatorProvider(
      PooledConnectionFactory connFactory,
      EntryResolver entryResolver,
      @NotNull @Named(BASE_DN_PROPERTY_NAME) String baseDn,
      @NotNull @Named(AUTH_TYPE_PROPERTY_NAME) String type,
      @Nullable @Named(DN_FORMAT_PROPERTY_NAME) String dnFormat,
      @Nullable @Named(USER_PASSWORD_ATTRIBUTE_PROPERTY_NAME) String userPasswordAttribute,
      @Nullable @Named(USER_FILTER_PROPERTY_NAME) String userFilter,
      @Nullable @Named(ALLOW_MULTIPLE_DNS_PROPERTY_NAME) String allowMultipleDns,
      @Nullable @Named(SUBTREE_SEARCH_PROPERTY_NAME) String subtreeSearch) {
    this.baseDn = baseDn;
    checkRequiredProperty(AUTH_TYPE_PROPERTY_NAME, type);
    this.type = AuthenticationType.valueOf(type);
    this.dnFormat = dnFormat;
    this.userPasswordAttribute = userPasswordAttribute;
    this.userFilter = userFilter;
    this.allowMultipleDns =
        isNullOrEmpty(allowMultipleDns) ? false : Boolean.valueOf(allowMultipleDns);
    this.subtreeSearch = isNullOrEmpty(subtreeSearch) ? false : Boolean.valueOf(subtreeSearch);
    this.authenticator = getAuthenticator(connFactory, entryResolver);
  }

  @Override
  public Authenticator get() {
    return authenticator;
  }

  private Authenticator getAuthenticator(
      PooledConnectionFactory connFactory, EntryResolver entryResolver) {
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
    checkRequiredProperty(
        Pair.of(USER_FILTER_PROPERTY_NAME, userFilter),
        Pair.of(BASE_DN_PROPERTY_NAME, baseDn),
        Pair.of(USER_FILTER_PROPERTY_NAME, userFilter));
    final PooledSearchDnResolver resolver = new PooledSearchDnResolver();
    resolver.setBaseDn(baseDn);
    resolver.setSubtreeSearch(subtreeSearch);
    resolver.setAllowMultipleDns(allowMultipleDns);
    resolver.setConnectionFactory(connFactory);
    resolver.setUserFilter(userFilter);
    return new Authenticator(resolver, getPooledBindAuthenticationHandler(connFactory));
  }

  private Authenticator getAuthenticatedOrAnonSearchAuthenticator(
      PooledConnectionFactory connFactory, EntryResolver entryResolver) {
    checkRequiredProperty(
        Pair.of(USER_FILTER_PROPERTY_NAME, userFilter),
        Pair.of(BASE_DN_PROPERTY_NAME, baseDn),
        Pair.of(USER_FILTER_PROPERTY_NAME, userFilter));
    final PooledSearchDnResolver resolver = new PooledSearchDnResolver();
    resolver.setBaseDn(baseDn);
    resolver.setSubtreeSearch(subtreeSearch);
    resolver.setAllowMultipleDns(allowMultipleDns);
    resolver.setConnectionFactory(connFactory);
    resolver.setUserFilter(userFilter);

    final Authenticator auth;
    if (isNullOrEmpty(userPasswordAttribute)) {
      auth = new Authenticator(resolver, getPooledBindAuthenticationHandler(connFactory));
    } else {
      auth = new Authenticator(resolver, getPooledCompareAuthenticationHandler(connFactory));
    }
    auth.setEntryResolver(entryResolver);

    return auth;
  }

  private Authenticator getDirectBindAuthenticator(PooledConnectionFactory connFactory) {
    checkRequiredProperty(DN_FORMAT_PROPERTY_NAME, dnFormat);
    final FormatDnResolver resolver = new FormatDnResolver(dnFormat);
    return new Authenticator(resolver, getPooledBindAuthenticationHandler(connFactory));
  }

  private Authenticator getActiveDirectoryAuthenticator(
      PooledConnectionFactory connFactory, EntryResolver entryResolver) {
    checkRequiredProperty(DN_FORMAT_PROPERTY_NAME, dnFormat);
    final FormatDnResolver resolver = new FormatDnResolver(dnFormat);
    final Authenticator authn =
        new Authenticator(resolver, getPooledBindAuthenticationHandler(connFactory));
    authn.setEntryResolver(entryResolver);
    return authn;
  }

  private PooledBindAuthenticationHandler getPooledBindAuthenticationHandler(
      PooledConnectionFactory connFactory) {
    final PooledBindAuthenticationHandler handler =
        new PooledBindAuthenticationHandler(connFactory);
    handler.setAuthenticationControls(new PasswordPolicyControl());
    return handler;
  }

  private PooledCompareAuthenticationHandler getPooledCompareAuthenticationHandler(
      PooledConnectionFactory connFactory) {
    final PooledCompareAuthenticationHandler handler =
        new PooledCompareAuthenticationHandler(connFactory);
    checkRequiredProperty(USER_PASSWORD_ATTRIBUTE_PROPERTY_NAME, userPasswordAttribute);
    handler.setPasswordAttribute(userPasswordAttribute);
    return handler;
  }

  final void checkRequiredProperty(String name, String value) {
    checkRequiredProperty(Pair.of(name, value));
  }

  @SafeVarargs
  @VisibleForTesting
  final void checkRequiredProperty(Pair<String, String>... nameValuePairs) {
    for (Pair<String, String> nameValuePair : nameValuePairs) {
      if (isNullOrEmpty(nameValuePair.second)) {
        throw new ConfigurationException(
            format(
                "Selected authentication type requires the property %s value to be not null or empty.",
                nameValuePair.first));
      }
    }
  }
}
