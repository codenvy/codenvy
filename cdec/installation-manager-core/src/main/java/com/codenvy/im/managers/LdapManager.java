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
package com.codenvy.im.managers;

import com.codenvy.api.dao.authentication.SSHAPasswordEncryptor;
import com.codenvy.im.artifacts.ArtifactFactory;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.artifacts.UnsupportedArtifactVersionException;
import com.codenvy.im.managers.helper.LdapManagerHelper;
import com.codenvy.im.managers.helper.LdapManagerHelperCodenvy3Impl;
import com.codenvy.im.managers.helper.LdapManagerHelperCodenvy4Impl;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.Version;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.auth.shared.dto.Credentials;
import org.eclipse.che.dto.server.DtoFactory;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

import static com.codenvy.im.utils.Commons.combinePaths;
import static java.lang.String.format;

/**
 * @author Alexander Reshetnyak
 * @author Dmytro Nochevnov
 */
@Singleton
public class LdapManager {

    private final ConfigManager configManager;
    private final HttpTransport httpTransport;

    private final Map<Integer, LdapManagerHelper> HELPERS;

    @Inject
    public LdapManager(ConfigManager configManager, HttpTransport httpTransport) throws IOException {
        this.configManager = configManager;
        this.httpTransport = httpTransport;

        HELPERS = ImmutableMap.of(
            3, new LdapManagerHelperCodenvy3Impl(configManager),
            4, new LdapManagerHelperCodenvy4Impl(configManager)
        );
    }

    /**
     * Modify Codenvy admin password.
     *
     * @throws IOException
     *         if any error occurred
     * @throws java.lang.IllegalStateException
     *         if current password is invalid
     */
    public void changeAdminPassword(byte[] currentPassword, byte[] newPassword) throws IOException, IllegalStateException {
        Config config = configManager.loadInstalledCodenvyConfig();

        validateCurrentPassword(currentPassword, config);

        try {
            InitialDirContext ldapContext = connect(config, getRootPrincipal(), config.getValue(Config.ADMIN_LDAP_PASSWORD));

            try {
                SSHAPasswordEncryptor sshaPasswordEncryptor = new SSHAPasswordEncryptor();
                String encryptedPwd = new String(sshaPasswordEncryptor.encrypt(newPassword), "UTF-8");

                ModificationItem[] mods = new ModificationItem[]{
                    new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userPassword", encryptedPwd))
                };

                ldapContext.modifyAttributes(getHelper().getNameOfObjectToChangePassword(), mods);
            } finally {
                ldapContext.close();
            }
        } catch (Exception e) {
            throw new IOException(format("Error in changing an admin password: %s" , e.getMessage()), e);
        }
    }

    /**
     * Shell command to get number of users:
     * sudo yum install openldap-clients
     * ldapsearch -D "cn=Admin,dc=codenvy-enterprise,dc=com" -w password -b "ou=People,dc=codenvy-enterprise,dc=com" -s base '(objectclass=inetOrgPerson)'
     *
     * Properties of ldap: /home/codenvy/codenvy-data/cloud-ide-local-configuration/ldap.properties
     * /etc/puppet/manifests/nodes/single_server/base_config.pp
     * /etc/puppet/manifests/nodes/multi_server/base_configurations.pp
     */
    public long getNumberOfUsers() throws IOException {
        Config config = configManager.loadInstalledCodenvyConfig();

        long resultCounter = 0;

        try {
            InitialDirContext ldapContext = connect(config, config.getValue(Config.JAVA_NAMING_SECURITY_PRINCIPAL), config.getValue(Config.USER_LDAP_PASSWORD));
            String ldapSearchBase = config.getValue(Config.USER_LDAP_USER_CONTAINER_DN);
            String ldapSearchFilter = format("(objectclass=%s)", config.getValue(Config.USER_LDAP_OBJECT_CLASSES));

            SearchControls ldapControls = new SearchControls();
            ldapControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);

            NamingEnumeration<SearchResult> results = null;
            try {
                results = ldapContext.search(ldapSearchBase, ldapSearchFilter, ldapControls);
                while (results.hasMore()) {
                    resultCounter++;
                    results.next();
                }
            } finally {
                ldapContext.close();

                if (Objects.nonNull(results)) {
                    results.close();
                }
            }
        } catch (Exception e) {
            throw new IOException("Error in getting a number of users", e);
        }

        return resultCounter;
    }

    protected void validateCurrentPassword(byte[] currentPassword, Config config) throws IOException {
        Credentials credentials = DtoFactory.newDto(Credentials.class);
        credentials.setPassword(new String(currentPassword, "UTF-8"));
        credentials.setUsername(config.getValue(Config.ADMIN_LDAP_USER_NAME));
        credentials.setRealm(getHelper().getRealm());

        String requestUrl = combinePaths(configManager.getApiEndpoint(), "/auth/login");
        try {
            httpTransport.doPost(requestUrl, credentials);
        } catch (IOException e) {
            throw new IllegalStateException("Invalid current password");
        }
    }

    private InitialDirContext connect(Config config, String secutiryPrincipal, String securityCredentials) throws NamingException, IOException {
        Hashtable<String, String> ldapEnv = new Hashtable<>(5);
        ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        ldapEnv.put(Context.PROVIDER_URL, format("%s://%s:%s",
                                                 config.getValue(Config.LDAP_PROTOCOL),
                                                 config.getValue(Config.LDAP_HOST),
                                                 config.getValue(Config.LDAP_PORT)));
        ldapEnv.put(Context.SECURITY_AUTHENTICATION, config.getValue(Config.JAVA_NAMING_SECURITY_AUTHENTICATION));
        ldapEnv.put(Context.SECURITY_PRINCIPAL, secutiryPrincipal);
        ldapEnv.put(Context.SECURITY_CREDENTIALS, securityCredentials);
        return new InitialDirContext(ldapEnv);
    }

    /** only 'cn=root' has rights to change admin password in default Codenvy ldap */
    protected String getRootPrincipal() throws IOException {
        return getHelper().getRootPrincipal();
    }

    /**
     * @throws IOException, UnsupportedArtifactVersionException
     */
    private LdapManagerHelper getHelper() throws IOException {
        Version codenvyVersion = Version.valueOf(configManager.loadInstalledCodenvyConfig().getValue(Config.VERSION));
        if (codenvyVersion.is3Major()) {
            return HELPERS.get(3);
        } else if (codenvyVersion.is4Major()) {
            return HELPERS.get(4);
        } else {
            throw new UnsupportedArtifactVersionException(ArtifactFactory.createArtifact(CDECArtifact.NAME),
                                                          codenvyVersion);
        }
    }
}
