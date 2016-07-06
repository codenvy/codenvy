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

import org.eclipse.che.api.user.server.spi.tck.ProfileDaoTest;
import org.eclipse.che.api.user.server.spi.tck.UserDaoTest;
import org.eclipse.che.commons.lang.IoUtil;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.net.URL;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Yevhenii Voevodin
 */
public class LdapEmbeddedServerListener implements ITestListener {

    public static final String LDAP_SERVER_URL_ATTRIBUTE_NAME = "ldap_server_url";

    private File               server;
    private EmbeddedLdapServer embeddedLdapServer;

    @Override
    public void onStart(ITestContext context) {
        final String suiteName = context.getSuite().getName();
        if (UserDaoTest.SUITE_NAME.equals(suiteName) || ProfileDaoTest.SUITE_NAME.equals(suiteName)) {
            final URL u = Thread.currentThread().getContextClassLoader().getResource(".");
            assertNotNull(u);
            try {
                final File target = new File(u.toURI()).getParentFile();
                server = new File(target, "server");
                assertTrue(server.mkdirs(), "Unable to create directory for temporary data");
                embeddedLdapServer = EmbeddedLdapServer.start(server);
                context.setAttribute(LDAP_SERVER_URL_ATTRIBUTE_NAME, embeddedLdapServer.getUrl());
            } catch (Exception x) {
                throw new RuntimeException(x.getMessage(), x);
            }
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        final String suiteName = context.getSuite().getName();
        if (UserDaoTest.SUITE_NAME.equals(suiteName) || ProfileDaoTest.SUITE_NAME.equals(suiteName)) {
            try {
                embeddedLdapServer.stop();
                assertTrue(IoUtil.deleteRecursive(server), "Unable remove temporary data");
            } catch (Exception x) {
                throw new RuntimeException(x.getMessage(), x);
            }
        }
    }

    @Override
    public void onTestStart(ITestResult result) {}

    @Override
    public void onTestSuccess(ITestResult result) {}

    @Override
    public void onTestFailure(ITestResult result) {}

    @Override
    public void onTestSkipped(ITestResult result) {}

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
}
