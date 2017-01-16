/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.im.testhelper.ldap;

import com.codenvy.im.BaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Reshetnyak
 * @author Dmytro Nochevnov
 */
abstract public class BaseLdapTest extends BaseTest {

    private EmbeddedADS ads;

    @BeforeClass
    public void initializationDirectoryService() throws Exception {
        File workDir = new File("target/server-work");
        workDir.mkdirs();

        // Create the server
        ads = new EmbeddedADS(workDir);
        importLdapData(ads);

        // optionally we can start a server too
        ads.startServer();
    }

    @AfterClass
    public void clearLdap() {
        ads.stopAndCleanupServer();
    }

    abstract protected void importLdapData(EmbeddedADS ads) throws Exception;

    protected Map<String, String> getTestSingleNodeProperties() {
        Map<String, String> properties = new HashMap<>(super.getTestSingleNodeProperties());
        properties.putAll(getLdapSpecificProperties());
        
        return properties;
    }

    abstract protected Map<String, String> getLdapSpecificProperties();
}
