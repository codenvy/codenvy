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
package com.codenvy.im.testhelper.ssh;

import com.codenvy.im.BaseTest;
import com.google.common.collect.ImmutableSet;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.util.SecurityUtils;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;

/**
 * Create SSH server for testing proposes.
 */
public class SshServerFactory {
    public static final String TEST_SSH_USER = "testUser";
    public static final String TEST_SSH_HOST = "127.0.0.1";
    public static final int    TEST_SSH_PORT = 2224;

    private static final Path TEST_SSH_AUTH_PUBLIC_KEY =
        Paths.get(BaseTest.class.getClassLoader().getResource("../test-classes/test_rsa.pub.txt").getFile());

    public static final Path TEST_SSH_AUTH_PRIVATE_KEY =
        Paths.get(BaseTest.class.getClassLoader().getResource("../test-classes/test_rsa.txt").getFile());

    static {
        restrictAccessToPrivateKey();
    }

    private static void restrictAccessToPrivateKey() {
        try {
            Files.setPosixFilePermissions(TEST_SSH_AUTH_PRIVATE_KEY, ImmutableSet.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get SSH server with shell support bound to separate port
     */
    public static SshServer createSshd() {
        // it is needed to fix test execution on Jenkins so as it registers own JCE Provider when use 'Ssh Plugin'
        // before running tests, and it leads to error 'Algorithm negotiation fail' (issue CDEC-469).
//        if (java.security.Security.getProvider(SecurityUtils.BOUNCY_CASTLE) != null) {
//            java.security.Security.removeProvider(SecurityUtils.BOUNCY_CASTLE);
//        }

        SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(TEST_SSH_PORT);

        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(TEST_SSH_AUTH_PUBLIC_KEY.toAbsolutePath().toString()));
        sshd.setPublickeyAuthenticator((s, publicKey, serverSession) -> true);
        sshd.setCommandFactory(command -> new ProcessShellFactory(command.split(" ")).create());

        return sshd;
    }
}
