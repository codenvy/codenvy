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
package com.codenvy.im.artifacts;

import com.codenvy.im.BaseTest;
import com.codenvy.im.commands.Command;
import com.codenvy.im.managers.InstallOptions;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.Version;
import org.mockito.Mock;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.codenvy.im.artifacts.ArtifactFactory.createArtifact;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Dmytro Nochevnov
 */
public class TestInstallManagerArtifact {
    public static final Path PATH_TO_BINARIES = Paths.get("/parent/im-update.tar.gz");
    private InstallManagerArtifact imArtifact;
    private Path testExecutionPath;

    @Mock
    private HttpTransport mockTransport;

    @BeforeClass
    public void setUp() throws Exception {
        testExecutionPath = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        imArtifact = (InstallManagerArtifact)createArtifact(InstallManagerArtifact.NAME);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetInstallInfo() throws Exception {
        imArtifact.getInstallInfo(null);
    }

    @Test
    public void testGetUpdateInfo() throws Exception {
        List<String> info = imArtifact.getUpdateInfo(null);
        assertNotNull(info);
        assertEquals(info.toString(), "[Initialize updating installation manager]");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGeInstallCommand() throws Exception {
        InstallOptions options = new InstallOptions();
        options.setStep(0);
        imArtifact.getInstallCommand(Version.valueOf("1.0.0"), PATH_TO_BINARIES, options);
    }

    @Test
    public void testGetUpdateCommand() throws Exception {
        InstallOptions options = new InstallOptions();

        options.setStep(0);
        Command command = imArtifact.getUpdateCommand(Version.valueOf("1.0.0"), PATH_TO_BINARIES, options);
        assertEquals(command.toString(), format("[{'command'='cat >> /home/%1$s/codenvy/update-im-cli << EOF\n"
                                                + "#!/bin/bash\n"
                                                + "\n"
                                                + "moveCliDir=false\n"
                                                + "if [[ -d /home/%1$s/codenvy/codenvy-cli ]]; then\n"
                                                + "    moveCliDir=true\n"
                                                + "\n"
                                                + "    if [[ \\$PWD =~ /home/%1$s/codenvy/codenvy-cli ]]; then\n"
                                                + "        echo \"Current directory '\\$PWD' is being removed...\"\n"
                                                + "        cd /home/%1$s/codenvy\n"
                                                + "    fi\n"
                                                + "\n"
                                                + "    rm -rf /home/%1$s/codenvy/codenvy-cli\n"
                                                + "\n"
                                                + "    if [[ $? == 0 ]]; then\n"
                                                + "        mkdir /home/%1$s/codenvy/cli\n"
                                                + "        sed -i \"s|codenvy-cli/bin|cli/bin|\" ~/.bashrc &> /dev/null\n"
                                                + "        source ~/.bashrc\n"
                                                + "    fi\n"
                                                + "else\n"
                                                + "    rm -rf /home/%1$s/codenvy/cli/bin/* \n"
                                                + "    find /home/%1$s/codenvy/cli/* ! -name 'bin' -type d -exec rm -rf {} + \n"
                                                + "fi\n"
                                                + "\n"
                                                + "tar -xzf /home/%1$s/codenvy/im-update.tar.gz -C /home/%1$s/codenvy/cli/ \n"
                                                + "chmod +x /home/%1$s/codenvy/cli/bin/* \n"
                                                + "sed -i \"2iJAVA_HOME=/home/%1$s/codenvy/jre\" /home/%1$s/codenvy/cli/bin/codenvy \n"
                                                + "rm -f /home/%1$s/codenvy/update-im-cli \n"
                                                + "rm -f /home/%1$s/codenvy/im-update.tar.gz \n"
                                                + "/home/%1$s/codenvy/cli/bin/codenvy \\$@\n"
                                                + "\n"
                                                + "if [[ \\$moveCliDir == true ]]; then\n"
                                                + "    echo \"Please, execute 'source ~/.bashrc' command to apply an update of installation manager CLI client.\"\n"
                                                + "fi\n"
                                                + "EOF', 'agent'='LocalAgent'}, "
                                                + "{'command'='chmod 775 /home/%1$s/codenvy/update-im-cli', 'agent'='LocalAgent'}, "
                                                + "{'command'='cp /parent/im-update.tar.gz /home/%1$s/codenvy/im-update.tar.gz', 'agent'='LocalAgent'}]",
                                                BaseTest.SYSTEM_USER_NAME));
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Step number 1000 is out of range")
    public void testGetUpdateCommandError() throws Exception {
        InstallOptions options = new InstallOptions();
        options.setStep(1000);

        imArtifact.getUpdateCommand(Version.valueOf("1.0.0"), PATH_TO_BINARIES, options);
    }

    @Test
    public void testGetInstalledVersion() throws Exception {
        assertTrue(imArtifact.getInstalledVersion().isPresent());
    }

    @Test
    public void testGetInstalledPath() throws Exception {
        assertEquals(imArtifact.getInstalledPath(), testExecutionPath);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testBackupCommand() throws IOException {
        imArtifact.getBackupCommand(null);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testRestoreCommand() throws IOException {
        imArtifact.getRestoreCommand(null);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testUpdateConfig() throws IOException {
        imArtifact.updateConfig(null);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetReinstallCodenvyCommandSingleServer() throws IOException {
        imArtifact.getReinstallCommand();
    }

    @Test
    public void shouldBeAlive() {
        assertTrue(imArtifact.isAlive());
    }

    @Test
    public void testGetConfig() throws Exception {
        Artifact imArtifact = createArtifact(InstallManagerArtifact.NAME);

        Map<String, String> m = imArtifact.getConfig();
        assertEquals(m.size(), 6);
        assertEquals(m.toString(), format("{download_directory=target/updates, "
                                          + "update_server_url=http://update.endpoint, "
                                          + "saas_server_url=http://saas.api.endpoint, "
                                          + "base_directory=/home/%s/codenvy, "
                                          + "backup_directory=target/backups, "
                                          + "report_directory=target/reports}", BaseTest.SYSTEM_USER_NAME));
    }
}
