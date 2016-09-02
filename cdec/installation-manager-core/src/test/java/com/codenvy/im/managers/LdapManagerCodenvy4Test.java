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

import com.codenvy.im.testhelper.ldap.BaseLdapTest;
import com.codenvy.im.testhelper.ldap.EmbeddedADS;
import com.codenvy.im.utils.HttpTransport;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.testng.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
public class LdapManagerCodenvy4Test extends BaseLdapTest {

    public static final String TEST_USER_LDAP_DN  = "dc=codenvy-enterprise,dc=com";

    @Mock
    private HttpTransport mockTransport;
    @Mock
    private ConfigManager mockConfigManager;

    private LdapManager spyLdapManager;

    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);

        prepareSingleNodeEnv(mockConfigManager);

        spyLdapManager = spy(new LdapManager(mockConfigManager));
        doReturn(EmbeddedADS.ADS_SECURITY_PRINCIPAL).when(spyLdapManager).getRootPrincipal();
    }

    @Test
    public void shouldReturnRootPrincipal() throws Exception {
        assertEquals(spyLdapManager.getRootPrincipal(), "uid=admin,ou=system");
    }

    @Test
    public void shouldReturnNumberOfUsers() throws Exception {
        assertEquals(spyLdapManager.getNumberOfUsers(), 3);
    }

    protected void importLdapData(EmbeddedADS ads) throws Exception {
        // Import codenvy 4 ldap user db
        JdbmPartition codenvyUserPartition = ads.addPartition("codenvy-user", TEST_USER_LDAP_DN);
        // use command "sudo slapcat -b 'dc=codenvy-enterprise,dc=com'" to obtain it
        ads.importEntriesFromLdif(codenvyUserPartition, Paths.get("target/test-classes/ldap/codenvy4-user-db.ldif"));
    }

    protected Map<String, String> getLdapSpecificProperties() {
        return new HashMap<String, String>() {{
            put(Config.VERSION, "4.0.0-M5-SNAPSHOT");
            put(Config.LDAP_PROTOCOL, EmbeddedADS.ADS_PROTOCOL);
            put(Config.LDAP_HOST, EmbeddedADS.ADS_HOST);
            put(Config.LDAP_PORT, EmbeddedADS.ADS_PORT);

            put(Config.JAVA_NAMING_SECURITY_AUTHENTICATION, EmbeddedADS.ADS_SECURITY_AUTHENTICATION);
            put(Config.JAVA_NAMING_SECURITY_PRINCIPAL, EmbeddedADS.ADS_SECURITY_PRINCIPAL);

            put(Config.ADMIN_LDAP_MAIL, "admin@codenvy.onprem");
            put(Config.USER_LDAP_PASSWORD, EmbeddedADS.ADS_SECURITY_CREDENTIALS);
            put(Config.ADMIN_LDAP_PASSWORD, "any-password");

            put(Config.USER_LDAP_USERS_OU, "users");
            put(Config.USER_LDAP_USER_CONTAINER_DN, "ou=$user_ldap_users_ou,$user_ldap_dn");
            put(Config.USER_LDAP_OBJECT_CLASSES, "inetOrgPerson");

            put(Config.USER_LDAP_DN, TEST_USER_LDAP_DN);
            
            put(Config.USER_LDAP_USER_DN, "uid");
        }};
    }
}
