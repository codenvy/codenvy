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
public class TestInstallationManagerCliWithCodenvy extends BaseIntegrationTest {

    @Test
    public void testInstallSingleNodeAndUpdateCodenvyConfig() throws Exception {
        doTest("codenvy/install/test-install-single-node-and-update-codenvy-config.sh");
    }

    @Test
    public void testInstallSudoPasswordRequired() throws Exception {
        doTest("codenvy/install/test-install-sudo-password-required.sh");
    }

    @Test
    public void testInstallSingleNodeBehindTheProxy() throws Exception {
        doTest("codenvy/install/test-install-single-node-behind-the-proxy.sh");
    }

    @Test
    public void testAddRemoveCodenvyNodes() throws Exception {
        doTest("codenvy/node/test-add-remove-codenvy-nodes.sh");
    }

    @Test
    public void testBackupRestoreSingleNode() throws Exception {
        doTest("codenvy/backup/test-backup-restore-single-node.sh");
    }

    @Test
    public void testUpdateSingleNode() throws Exception {
        doTest("codenvy/update/test-update-single-node.sh");
    }

    @Test
    public void testUpdateSingleNodeFromBinary() throws Exception {
        doTest("codenvy/update/test-update-single-node-from-binary.sh");
    }

    @Test
    public void testVersionCommand() throws Exception {
        doTest("codenvy/test-version.sh");
    }

    @Test
    public void testAudit() throws Exception {
        doTest("codenvy/audit/test-audit.sh");
    }

    /* Repeat tests in RHEL OS*/
    @Test
    public void testAddRemoveCodenvyNodesInRhelOs() throws Exception {
        doTest("codenvy/node/test-add-remove-codenvy-nodes.sh", RHEL);
    }

    @Test
    public void testBackupRestoreSingleNodeInRhelOs() throws Exception {
        doTest("codenvy/backup/test-backup-restore-single-node.sh", RHEL);
    }

    @Test
    public void testInstallSingleNodeBehindTheProxyInRhelOs() throws Exception {
        doTest("codenvy/install/test-install-single-node-behind-the-proxy.sh", RHEL);
    }
}
