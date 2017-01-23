package com.codenvy.ldap.auth;

import com.codenvy.api.dao.authentication.AuthenticationHandler;
import com.codenvy.api.dao.authentication.PasswordEncryptor;
import com.codenvy.api.dao.authentication.SSHAPasswordEncryptor;
import com.codenvy.ldap.EmbeddedLdapServer;
import com.codenvy.ldap.LdapUserIdNormalizer;

import org.apache.directory.shared.ldap.entry.ServerEntry;
import org.eclipse.che.api.auth.AuthenticationException;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.EntryResolver;
import org.ldaptive.pool.PooledConnectionFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.fail;

/**
 * Tests different authentication types defined by {@link AuthenticationType}.
 *
 * @author Yevhenii Voevodin
 */
public class AuthenticationTest {

    private PasswordEncryptor    encryptor    = new SSHAPasswordEncryptor();
    private LdapUserIdNormalizer cnNormalizer = new LdapUserIdNormalizer("cn");
    private EmbeddedLdapServer server;

    /**
     * Ups ldap test server & initializes the following directory structure:
     *
     * <pre>
     * dc=codenvy,dc=com
     *   ou=developers
     *     cn=mike
     *      -objectClass=inetOrgPerson
     *      -uid=user1
     *      -cn=mike
     *      -sn=mike
     *      -userPassword=sha(mike)
     *     cn=john
     *      -objectClass=inetOrgPerson
     *      -uid=user2
     *      -cn=john
     *      -sn=john
     *      -userPassword=sha(john)
     *   ou=managers
     *     cn=brad
     *      -objectClass=inetOrgPerson
     *      -uid=user3
     *      -cn=brad
     *      -sn=brad
     *      -userPassword=sha(brad)
     *     cn=ivan
     *      -objectClass=inetOrgPerson
     *      -uid=user4
     *      -cn=ivan
     *      -sn=ivan
     *      -userPassword=sha(ivan)
     * </pre>
     */
    @BeforeMethod
    public void startServer() throws Exception {
        server = EmbeddedLdapServer.builder()
                                   .setPartitionId("codenvy")
                                   .setPartitionDn("dc=codenvy,dc=com")
                                   .useTmpWorkingDir()
                                   .setMaxSizeLimit(1000)
                                   .build();
        server.start();

        // developers
        ServerEntry ouDevelopers = server.newEntry("ou", "developers");
        ouDevelopers.add("objectClass", "organizationalUnit");
        ouDevelopers.add("ou", "developers");
        server.addEntry(ouDevelopers);

        ServerEntry mike = server.newEntry("cn", "mike", ouDevelopers);
        mike.add("objectClass", "inetOrgPerson");
        mike.add("uid", "user1");
        mike.add("cn", "mike");
        mike.add("sn", "mike");
        mike.add("userPassword", encryptor.encrypt("mike".getBytes(UTF_8)));
        server.addEntry(mike);

        ServerEntry john = server.newEntry("cn", "john", ouDevelopers);
        john.add("objectClass", "inetOrgPerson");
        john.add("uid", "user2");
        john.add("cn", "john");
        john.add("sn", "john");
        john.add("userPassword", encryptor.encrypt("john".getBytes(UTF_8)));
        server.addEntry(john);

        // managers
        ServerEntry ouManagers = server.newEntry("ou", "managers");
        ouManagers.add("objectClass", "organizationalUnit");
        ouManagers.add("ou", "managers");
        server.addEntry(ouManagers);

        ServerEntry brad = server.newEntry("cn", "brad", ouManagers);
        brad.add("objectClass", "inetOrgPerson");
        brad.add("uid", "user3");
        brad.add("cn", "brad");
        brad.add("sn", "brad");
        brad.add("userPassword", encryptor.encrypt("brad".getBytes(UTF_8)));
        server.addEntry(brad);

        ServerEntry ivan = server.newEntry("cn", "ivan", ouManagers);
        ivan.add("objectClass", "inetOrgPerson");
        ivan.add("uid", "user4");
        ivan.add("cn", "ivan");
        ivan.add("sn", "ivan");
        ivan.add("userPassword", encryptor.encrypt("ivan".getBytes(UTF_8)));
        server.addEntry(ivan);
    }

    @Test
    public void directAuth() throws Exception {
        PooledConnectionFactory connFactory = server.getConnectionFactory();
        EntryResolver entryResolver = new EntryResolverProvider(connFactory,
                                                                "ou=developers,dc=codenvy,dc=com", // <- base dn
                                                                null, // <- user filter
                                                                null).get(); // <- subtree search
        Authenticator authenticator = new AuthenticatorProvider(connFactory,
                                                                entryResolver,
                                                                "ou=developers,dc=codenvy,dc=com", // <- base dn
                                                                "DIRECT", // <- auth type
                                                                "cn=%s,ou=developers,dc=codenvy,dc=com", // <- dn format
                                                                null, // <- user password attribute
                                                                null, // <- user filter
                                                                null, // <- allow multiple dns
                                                                null).get(); // <- subtree search

        LdapAuthenticationHandler handler = new LdapAuthenticationHandler(authenticator, cnNormalizer);

        mustAuthenticate(handler, "mike", "mike");
        mustAuthenticate(handler, "john", "john");
        mustNotAuthenticate(handler, "brad", "brad");
        mustNotAuthenticate(handler, "ivan", "ivan");
    }

    @Test
    public void authenticatedAuthUsingRootBaseDN() {
        PooledConnectionFactory connFactory = server.getConnectionFactory();
        EntryResolver entryResolver = new EntryResolverProvider(connFactory,
                                                                "dc=codenvy,dc=com", // <- base dn
                                                                "cn={user}", // <- user filter
                                                                "true").get(); // <- subtree search
        Authenticator authenticator = new AuthenticatorProvider(connFactory,
                                                                entryResolver,
                                                                "dc=codenvy,dc=com", // <- base dn
                                                                "AUTHENTICATED", // <- auth type
                                                                null, // <- dn format
                                                                null, // <- user password attribute
                                                                "cn={user}", // <- user filter
                                                                null, // <- allow multiple dns
                                                                "true").get(); // <- subtree search

        LdapAuthenticationHandler handler = new LdapAuthenticationHandler(authenticator, cnNormalizer);

        mustAuthenticate(handler, "mike", "mike");
        mustAuthenticate(handler, "john", "john");
        mustAuthenticate(handler, "ivan", "ivan");
        mustAuthenticate(handler, "brad", "brad");
    }

    @Test
    public void authenticatedAuthUsingCertainBaseDn() {
        PooledConnectionFactory connFactory = server.getConnectionFactory();
        EntryResolver entryResolver = new EntryResolverProvider(connFactory,
                                                                "ou=managers,dc=codenvy,dc=com", // <- base dn
                                                                "(&(objectClass=inetOrgPerson)(cn={user}))", // <- user filter
                                                                "true").get(); // <- subtree search
        Authenticator authenticator = new AuthenticatorProvider(connFactory,
                                                                entryResolver,
                                                                "ou=managers,dc=codenvy,dc=com", // <- base dn
                                                                "AUTHENTICATED", // <- auth type
                                                                null, // <- dn format
                                                                null, // <- user password attribute
                                                                "(&(objectClass=inetOrgPerson)(cn={user}))", // <- user filter
                                                                null, // <- allow multiple dns
                                                                "true").get(); // <- subtree search

        LdapAuthenticationHandler handler = new LdapAuthenticationHandler(authenticator, cnNormalizer);

        mustAuthenticate(handler, "ivan", "ivan");
        mustAuthenticate(handler, "brad", "brad");
        mustNotAuthenticate(handler, "mike", "mike");
        mustNotAuthenticate(handler, "john", "john");
    }

    private static void mustAuthenticate(AuthenticationHandler handler, String name, String password) {
        try {
            handler.authenticate(name, password);
        } catch (AuthenticationException x) {
            fail(format("Failed to authenticate user '%s' with password '%s' due to an error: %s",
                        name,
                        password,
                        x.getMessage()));
        }
    }

    private static void mustNotAuthenticate(AuthenticationHandler handler, String name, String password) {
        try {
            handler.authenticate(name, password);
            fail(format("Authentication must fail for user '%s' and password '%s', but it was successful", name, password));
        } catch (AuthenticationException ignored) {
            // good
        }
    }

    @AfterMethod
    public void shutdown() {
        server.shutdown();
    }
}
