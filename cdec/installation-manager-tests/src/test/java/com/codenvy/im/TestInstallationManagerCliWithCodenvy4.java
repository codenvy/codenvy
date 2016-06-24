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

/**
 * Modify /etc/hosts:
 *
 * 192.168.56.110 codenvy
 * 192.168.56.110 test.codenvy
 * 192.168.56.15 node1.codenvy
 * 192.168.56.20 node2.test.codenvy
 *
 * @author Dmytro Nochevnov
 */
public class TestInstallationManagerCliWithCodenvy4 extends BaseIntegrationTest {

    @Test
    public void testInstallSingleNodeAndChangeConfigWithCodenvy4() throws Exception {
        doTest("codenvy4/test-install-single-node-and-change-config-with-codenvy4.sh");
    }

    @Test
    public void testAddRemoveCodenvyNodesWithCodenvy4() throws Exception {
        doTest("codenvy4/test-add-remove-codenvy-nodes-with-codenvy4.sh");
    }

    @Test
    public void testBackupRestoreSingleNodeWithCodenvy4() throws Exception {
        doTest("codenvy4/test-backup-restore-single-node-with-codenvy4.sh");
    }

    @Test
    public void testVersionCommand() throws Exception {
        doTest("codenvy4/test-version.sh");
    }

    @Test
    public void testUpdateSingleNodeWithCodenvy4() throws Exception {
        doTest("codenvy4/test-update-single-node-with-codenvy4.sh");
    }

    @Test
    public void testLoginToCodenvySaaSThroughIMServerWithCodenvy4() throws Exception {
        doTest("codenvy4/test-login-to-saas-through-ims-with-codenvy4.sh");
    }

    @Test
    public void testUpdateSingleNodeFromBinaryWithCodenvy4() throws Exception {
        doTest("codenvy4/test-update-single-node-from-binary-with-codenvy4.sh");
    }

    @Test
    public void testInstallSingleNodeBehindTheProxyWithCodenvy4() throws Exception {
        doTest("codenvy4/test-install-single-node-behind-the-proxy-with-codenvy4.sh");
    }

    @Test
    public void testSetupAndUpdateCodenvy4Config() throws Exception {
        doTest("codenvy4/test-setup-and-update-codenvy4-config.sh");
    }

    /* Repeat tests in RHEL OS*/
    @Test
    public void testAddRemoveCodenvyNodesWithCodenvy4InRhelOs() throws Exception {
        doTest("codenvy4/test-add-remove-codenvy-nodes-with-codenvy4.sh", RHEL);
    }

    @Test
    public void testBackupRestoreSingleNodeWithCodenvy4InRhelOs() throws Exception {
        doTest("codenvy4/test-backup-restore-single-node-with-codenvy4.sh", RHEL);
    }

    @Test
    public void testInstallSingleNodeBehindTheProxyWithCodenvy4InRhelOs() throws Exception {
        doTest("codenvy4/test-install-single-node-behind-the-proxy-with-codenvy4.sh", RHEL);
    }
}
