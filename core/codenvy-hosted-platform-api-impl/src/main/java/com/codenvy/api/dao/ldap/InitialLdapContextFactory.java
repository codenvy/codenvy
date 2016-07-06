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

import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;

import java.util.Hashtable;

import static javax.naming.Context.INITIAL_CONTEXT_FACTORY;
import static javax.naming.Context.PROVIDER_URL;
import static javax.naming.Context.SECURITY_AUTHENTICATION;
import static javax.naming.Context.SECURITY_CREDENTIALS;
import static javax.naming.Context.SECURITY_PRINCIPAL;

/**
 * @author Eugene Voevodin
 */
@Singleton
public class InitialLdapContextFactory {

    private final Provider<String> serverUrlProvider;
    private final String           systemDn;
    private final String           systemPassword;
    private final String           authType;
    private final String           usePool;
    private final String           initPoolSize;
    private final String           maxPoolSize;
    private final String           prefPoolSize;
    private final String           poolTimeout;

    /**
     * Creates instance of {@link InitialLdapContextFactory}
     *
     * @param providerUrl
     *         URL of LDAP service provider, e.g. {@code ldap://localhost:389}.
     * @param systemDn
     *         principal used to open LDAP connection, e.g. {@code cn=Admin,ou=system,dc=codenvy,dc=com}. May be omitted if authentication
     *         is not needed, e.g. in tests. See {@link javax.naming.Context#SECURITY_PRINCIPAL}.
     * @param systemPassword
     *         password of principal to open LDAP connection.  May be omitted if authentication is not needed, e.g. in tests. See {@link
     *         javax.naming.Context#SECURITY_CREDENTIALS} .
     * @param authType
     *         authentication type, see {@link javax.naming.Context#SECURITY_AUTHENTICATION}
     * @param usePool
     *         setup policy for connection pooling. Allowed value of this parameter is "true" or "false". See <a
     *         href="http://docs.oracle.com/javase/jndi/tutorial/ldap/connect/config.html">details</a> about connection pooling.
     * @param initPoolSize
     *         initial size of connection pool. Parameter MUST be string representation of an integer. Make sense ONLY if parameter {@code
     *         usePool} is equals to "true".
     * @param maxPoolSize
     *         max size for connection poll. Parameter MUST be string representation of an integer. Make sense ONLY if parameter {@code
     *         usePool} is equals to "true".
     * @param prefPoolSize
     *         preferred size for connection poll. Parameter MUST be string representation of an integer. Make sense ONLY if parameter
     *         {@code usePool} is equals to "true". Often this parameter may be omitted.
     * @param poolTimeout
     *         time (in milliseconds) that an idle connection may remain in the pool. Parameter MUST be string representation of an
     *         integer.
     *         Make sense ONLY if parameter {@code usePool} is equals to "true".
     */
    @Inject
    public InitialLdapContextFactory(@Named(PROVIDER_URL) Provider<String> providerUrl,
                                     @Nullable @Named(SECURITY_PRINCIPAL) String systemDn,
                                     @Nullable @Named(SECURITY_CREDENTIALS) String systemPassword,
                                     @Nullable @Named(SECURITY_AUTHENTICATION) String authType,
                                     @Nullable @Named("com.sun.jndi.ldap.connect.pool") String usePool,
                                     @Nullable @Named("com.sun.jndi.ldap.connect.pool.initsize") String initPoolSize,
                                     @Nullable @Named("com.sun.jndi.ldap.connect.pool.maxsize") String maxPoolSize,
                                     @Nullable @Named("com.sun.jndi.ldap.connect.pool.prefsize") String prefPoolSize,
                                     @Nullable @Named("com.sun.jndi.ldap.connect.pool.timeout") String poolTimeout) {
        this.serverUrlProvider = providerUrl;
        this.systemDn = systemDn;
        this.systemPassword = systemPassword;
        this.authType = authType;
        this.usePool = usePool;
        this.initPoolSize = initPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.prefPoolSize = prefPoolSize;
        this.poolTimeout = poolTimeout;
    }

    /**
     * Creates {@link InitialLdapContext} instance
     *
     * @return initial ldap content
     * @throws NamingException
     *         when any error occurs while creating context
     */
    public InitialLdapContext createContext() throws NamingException {
        final Hashtable<String, String> env = new Hashtable<>();
        env.put("java.naming.ldap.deleteRDN", "false");
        env.put(INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(PROVIDER_URL, serverUrlProvider.get());
        if (authType != null) {
            env.put(SECURITY_AUTHENTICATION, authType);
        }
        if (systemDn != null) {
            env.put(SECURITY_PRINCIPAL, systemDn);
        }
        if (systemPassword != null) {
            env.put(SECURITY_CREDENTIALS, systemPassword);
        }
        if ("true".equalsIgnoreCase(usePool)) {
            env.put("com.sun.jndi.ldap.connect.pool", "true");
            if (initPoolSize != null) {
                env.put("com.sun.jndi.ldap.connect.pool.initsize", initPoolSize);
            }
            if (maxPoolSize != null) {
                env.put("com.sun.jndi.ldap.connect.pool.maxsize", maxPoolSize);
            }
            if (prefPoolSize != null) {
                env.put("com.sun.jndi.ldap.connect.pool.prefsize", prefPoolSize);
            }
            if (poolTimeout != null) {
                env.put("com.sun.jndi.ldap.connect.pool.timeout", poolTimeout);
            }
        }
        return new InitialLdapContext(env, null);
    }

    /**
     * Creates {@link InitialLdapContext} instance which uses
     * {@code principal} and {@code password} for authentication
     *
     * @param principal
     *         security principal
     * @param password
     *         security credentials
     * @return initial ldap context
     * @throws NamingException
     *         when any error occurs while creating context
     */
    public InitialLdapContext createContext(String principal, String password) throws NamingException {
        final Hashtable<String, String> env = new Hashtable<>();
        env.put(INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(PROVIDER_URL, serverUrlProvider.get());
        if (authType != null) {
            env.put(SECURITY_AUTHENTICATION, authType);
        }
        env.put(SECURITY_PRINCIPAL, principal);
        env.put(SECURITY_CREDENTIALS, password);
        env.put("com.sun.jndi.ldap.connect.pool", "false");
        return new InitialLdapContext(env, null);
    }
}
