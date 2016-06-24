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
package com.codenvy.im;

import org.testng.annotations.Test;

/** @author Anatoliy Bazko */
public class TestInstallationManagerCli extends BaseIntegrationTest {

    @Test
    public void testInstallImCli() throws Exception {
        doTest("install/test-install-im-cli.sh");
    }

    @Test
    public void testInstallImCliInRhelOs() throws Exception {
        doTest("install/test-install-im-cli-in-rhel-os.sh");
    }

    @Test
    public void testInstallExceptionCases() throws Exception {
        doTest("install/test-install-exception-cases.sh");
    }

    @Test
    public void testInstallUpdateImCliClient() throws Exception {
        doTest("install/test-install-update-im-cli-client.sh");
    }

    @Test
    public void testLoginWithUsernameAndPassword() throws Exception {
        doTest("login/test-login-to-saas-through-im-cli.sh");
    }

    @Test
    public void testDownloadAllUpdates() throws Exception {
        doTest("download/test-download-all-updates.sh");
    }

    @Test
    public void testHelpCommand() throws Exception {
        doTest("help/test-help.sh");
    }

}
