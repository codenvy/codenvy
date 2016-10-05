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
 * 192.168.56.19 master.codenvy
 * 192.168.56.18 analytics.codenvy
 * 192.168.56.13 api.codenvy
 * 192.168.56.15 runner1.codenvy
 * 192.168.56.20 runner2.codenvy
 * 192.168.56.21 runner3.codenvy
 *
 * @author Anatoliy Bazko
 */
public class TestInstallationManagerCliWithCodenvy3 extends BaseIntegrationTest {

    @Test
    public void testInstallMultiNodeAndChangeConfig() throws Exception {
        doTest("codenvy3/install/test-install-multi-nodes-and-change-config-with-codenvy3.sh");
    }

    @Test
    public void testInstallSingleNodeAndChangeConfig() throws Exception {
        doTest("codenvy3/install/test-install-single-node-and-change-config-with-codenvy3.sh");
    }

//    @Test
//    public void testUpdateSingleNodeFromBinary() throws Exception {
//        doTest("codenvy3/update/test-update-single-node-from-binary-with-codenvy3.sh");
//    }
//
//    @Test
//    public void testUpdateMultiNodesFromBinary() throws Exception {
//        doTest("codenvy3/update/test-update-multi-nodes-from-binary-with-codenvy3.sh");
//    }

    @Test
    public void testAddRemoveCodenvyNodes() throws Exception {
        doTest("codenvy3/node/test-add-remove-codenvy-nodes-with-codenvy3.sh");
    }

    @Test
    public void testBackupRestoreSingleNode() throws Exception {
        doTest("codenvy3/backup/test-backup-restore-single-node-with-codenvy3.sh");
    }

//    @Test(priority = 9)
//    public void testMigrationData() throws Exception {
//        doTest("codenvy3/update/test-migration-data-with-codenvy3.sh");
//    }
//
//    @Test(priority = 10)
//    public void testBackupRestoreMultiNodes() throws Exception {
//        doTest("codenvy3/update/test-backup-restore-multi-nodes-with-codenvy3.sh");
//    }

}
