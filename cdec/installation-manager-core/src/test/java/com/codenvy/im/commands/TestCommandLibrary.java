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
package com.codenvy.im.commands;

import com.codenvy.im.BaseTest;
import com.codenvy.im.agent.AgentException;
import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.InstallOptions;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.managers.NodeConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import static com.codenvy.im.commands.CommandLibrary.createFileBackupCommand;
import static com.codenvy.im.commands.CommandLibrary.createFileRestoreOrBackupCommand;
import static com.codenvy.im.commands.CommandLibrary.createReplaceCommand;
import static com.codenvy.im.commands.CommandLibrary.createUpdateFileCommand;
import static com.codenvy.im.commands.CommandLibrary.getFileRestoreOrBackupCommand;
import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.write;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** @author Dmytro Nochevnov */
public class TestCommandLibrary extends BaseTest {
    public static NodeConfig testApiNode = new NodeConfig(NodeConfig.NodeType.API, "localhost");
    public static NodeConfig testDataNode = new NodeConfig(NodeConfig.NodeType.DATA, "127.0.0.1");
    public static final Path TEST_FILE = Paths.get("target/testFile");

    @BeforeMethod
    public void setup() {
        testApiNode = new NodeConfig(NodeConfig.NodeType.API, "localhost");
        testDataNode = new NodeConfig(NodeConfig.NodeType.DATA, "127.0.0.1");
    }

    @Test
    public void testCreateLocalPuppetPropertyReplaceMultiplyLineCommand() throws IOException {
        write(TEST_FILE.toFile(), "$property = \"first\"\n"
                                  + "# $property = \"comment\"\n"
                                  + "$property = \"a\n" +
                                  "b\n" +
                                  "c\n" +
                                  "\"\n");

        Command testCommand = CommandLibrary.createPuppetPropertyReplaceCommand(TEST_FILE, "property", "1\n2\n3\n", false);
        assertEquals(testCommand.toString(), "{'command'='cat target/testFile " +
                                             "| sed ':a;N;$!ba;s/\\n/~n/g' " +
                                             "| sed -E 's|(~n[^#]*\\$)property *= *\"[^\"]*\"|\\1property = \"1\\n2\\n3\\n\"|g' " +
                                             "| sed 's|~n|\\n|g' > tmp.tmp " +
                                             "&& mv tmp.tmp target/testFile', " +
                                             "'agent'='LocalAgent'}");

        testCommand.execute();

        String content = readFileToString(TEST_FILE.toFile());
        assertEquals(content, "$property = \"first\"\n"
                              + "# $property = \"comment\"\n"
                              + "$property = \"1\n"
                              + "2\n"
                              + "3\n"
                              + "\"\n");
    }

    @Test
    public void testCreateLocalPuppetReplaceCommand() throws IOException {
        write(TEST_FILE.toFile(), "old\n");

        Command testCommand = createReplaceCommand(TEST_FILE.toString(), "old", "\\$new | &&", false);
        assertEquals(testCommand.toString(), "{'command'='cat target/testFile " +
                                             "| sed ':a;N;$!ba;s/\\n/~n/g' " +
                                             "| sed -E 's|old|\\\\$new \\| \\&\\&|g' " +
                                             "| sed 's|~n|\\n|g' > tmp.tmp " +
                                             "&& mv tmp.tmp target/testFile', 'agent'='LocalAgent'}");
        testCommand.execute();

        String content = readFileToString(TEST_FILE.toFile());
        assertEquals(content, "\\$new | &&\n");
    }

    @Test
    public void testCreateLocalPuppetReplaceMultilineCommand() throws IOException {
        write(TEST_FILE.toFile(), "old\n");

        Command testCommand = createReplaceCommand(TEST_FILE.toString(), "old", "\\$new\n"
                                                                                + "new\n"
                                                                                + "new\n", false);
        assertEquals(testCommand.toString(), "{'command'='cat target/testFile " +
                                             "| sed ':a;N;$!ba;s/\\n/~n/g' " +
                                             "| sed -E 's|old|\\\\$new\\nnew\\nnew\\n|g' " +
                                             "| sed 's|~n|\\n|g' > tmp.tmp " +
                                             "&& mv tmp.tmp target/testFile', 'agent'='LocalAgent'}");
        testCommand.execute();

        String content = readFileToString(TEST_FILE.toFile());
        assertEquals(content, "\\$new\n"
                              + "new\n"
                              + "new\n"
                              + "\n");
    }

    @Test
    public void testCreateLocalReplaceMultiplyLineCommand() throws IOException {
        write(TEST_FILE.toFile(), "old\n");

        Command testCommand = createReplaceCommand(TEST_FILE.toString(), "old", "new\nnew\nnew\n", false);
        assertEquals(testCommand.toString(), "{'command'='cat target/testFile " +
                                             "| sed ':a;N;$!ba;s/\\n/~n/g' " +
                                             "| sed -E 's|old|new\\nnew\\nnew\\n|g' " +
                                             "| sed 's|~n|\\n|g' > tmp.tmp " +
                                             "&& mv tmp.tmp target/testFile', 'agent'='LocalAgent'}");
        testCommand.execute();

        String content = readFileToString(TEST_FILE.toFile());
        assertEquals(content, "new\n" +
                              "new\n" +
                              "new\n" +
                              "\n");
    }

    @Test
    public void testCreateUpdateFileCommand() throws IOException {
        Command testCommand = createUpdateFileCommand(TEST_FILE, "pr2='value\"|&3\n'", "^pr2=.*", testApiNode);
        assertEquals(testCommand.toString(), format("{'command'='if test -n \"^pr2=.*\" && sudo grep -Eq \"^pr2=.*\" \"target/testFile\"; then\n"
                                                    + "  sudo sed -i \"s|^pr2=.*|pr2='value\\\"\\|\\&3\\n'|\" \"target/testFile\" &> /dev/null\n"
                                                    + "fi\n"
                                                    + "if ! sudo grep -Eq \"^pr2='value\\\"|&3\n'$\" \"target/testFile\"; then\n"
                                                    + "  echo \"pr2='value\\\"|&3\n'\" | sudo tee --append \"target/testFile\" &> /dev/null\n"
                                                    + "fi', 'agent'='{'host'='localhost', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
    }

    @Test
    public void testUpdateByUpdateFileCommand() throws IOException {
        write(TEST_FILE.toFile(), "#pr2=\n"
                                  + "pr2=value2\n"
                                  + "pr3=\n");

        Command testCommand = SimpleCommand.createCommand(CommandLibrary.getUpdateFileCommand(TEST_FILE, "pr2='value\"|&3\n'", "^pr2=.*", false));
        testCommand.execute();

        String content = readFileToString(TEST_FILE.toFile());
        assertEquals(content,
                     "#pr2=\n"
                      + "pr2='value\"|&3\n'\n"
                      + "pr3=\n",
                     "Command to execute: " + testCommand.toString());
    }

    @Test
    public void testAppendByUpdateFileCommand() throws IOException {
        write(TEST_FILE.toFile(), "#pr2=\n"
                                  + "pr3=value3\n");

        Command testCommand = SimpleCommand.createCommand(CommandLibrary.getUpdateFileCommand(TEST_FILE, "pr2='value\"|&3\n'", "^pr2=.*", false));
        testCommand.execute();

        String content = readFileToString(TEST_FILE.toFile());
        assertEquals(content, "#pr2=\n"
                              + "pr3=value3\n"
                              + "pr2='value\"|&3\n'\n",
                     "Command to execute: " + testCommand.toString());
    }

    @Test
    public void testAppendTextToFileCommand() throws IOException {
        write(TEST_FILE.toFile(), "127.0.0.1 codenvy.onprem localhost\n"
                                  + "192.168.1.1 host\n");

        Command testCommand = SimpleCommand.createCommand(CommandLibrary.getAppendTextToFileCommand(TEST_FILE, "\n127.0.0.1 codenvy", "^127.0.0.1.*\\scodenvy\\s.*$|^127.0.0.1.*\\scodenvy$", false));
        testCommand.execute();

        String content = readFileToString(TEST_FILE.toFile());
        assertEquals(content,
                     "127.0.0.1 codenvy.onprem localhost\n"
                     + "192.168.1.1 host\n"
                     + "\n"
                     + "127.0.0.1 codenvy\n",
                     "Command to execute: " + testCommand.toString());
    }

    @Test
    public void testDonNotAppendTextToFileCommand() throws IOException {
        write(TEST_FILE.toFile(), "127.0.0.1 codenvy codenvy.onprem localhost\n"
                                  + "192.168.1.1 host\n");

        Command testCommand = SimpleCommand.createCommand(CommandLibrary.getAppendTextToFileCommand(TEST_FILE, "\n127.0.0.1 codenvy", "^127.0.0.1.*\\scodenvy\\s.*$|^127.0.0.1.*\\scodenvy$", false));
        testCommand.execute();

        String content = readFileToString(TEST_FILE.toFile());
        assertEquals(content,
                     "127.0.0.1 codenvy codenvy.onprem localhost\n"
                        + "192.168.1.1 host\n",
                     "Command to execute: " + testCommand.toString());
    }

    @Test
    public void testGetRestoreOrBackupCommand() {
        String testCommand = getFileRestoreOrBackupCommand("testFile");
        assertEquals(testCommand, "if sudo test -f testFile; then" +
                                  "     if ! sudo test -f testFile.back; then" +
                                  "         sudo cp testFile testFile.back;" +
                                  "     else" +
                                  "         sudo cp testFile.back testFile;" +
                                  "     fi fi");
    }

    @Test
    public void testCreateLocalFileRestoreOrBackupCommand() throws AgentException {
        Command testCommand = createFileRestoreOrBackupCommand("testFile");
        assertEquals(testCommand.toString(), "{" +
                                             "'command'='if sudo test -f testFile; then" +
                                             "     if ! sudo test -f testFile.back; then" +
                                             "         sudo cp testFile testFile.back;" +
                                             "     else" +
                                             "         sudo cp testFile.back testFile;" +
                                             "     fi fi', 'agent'='LocalAgent'}");
    }

    @Test
    public void testCreateShellAgentFileRestoreOrBackupCommandForNode() throws AgentException {
        String expectedCommandString = format("[" +
                                              "{'command'='if sudo test -f testFile; then" +
                                              "     if ! sudo test -f testFile.back; then" +
                                              "         sudo cp testFile testFile.back;" +
                                              "     else" +
                                              "         sudo cp testFile.back testFile;" +
                                              "     fi fi', " +
                                              "'agent'='{'host'='localhost', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}" +
                                              "]",
                                              SYSTEM_USER_NAME);

        NodeConfig node = new NodeConfig(NodeConfig.NodeType.API, "localhost");

        Command testCommand = createFileRestoreOrBackupCommand("testFile", node);
        assertEquals(testCommand.toString(), expectedCommandString);
    }

    @Test
    public void testCreateShellFileRestoreOrBackupCommand() throws AgentException {
        String expectedCommandString = format("[" +
                                              "{'command'='if sudo test -f testFile; then" +
                                              "     if ! sudo test -f testFile.back; then" +
                                              "         sudo cp testFile testFile.back;" +
                                              "     else" +
                                              "         sudo cp testFile.back testFile;" +
                                              "     fi fi', " +
                                              "'agent'='{'host'='localhost', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, " +
                                              "{'command'='if sudo test -f testFile; then " +
                                              "    if ! sudo test -f testFile.back; then" +
                                              "         sudo cp testFile testFile.back;" +
                                              "     else" +
                                              "         sudo cp testFile.back testFile;" +
                                              "     fi fi', " +
                                              "'agent'='{'host'='127.0.0.1', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}" +
                                              "]",
                                              SYSTEM_USER_NAME);

        List<NodeConfig> nodes = ImmutableList.of(
            testApiNode,
            testDataNode
        );

        Command testCommand = CommandLibrary.createFileRestoreOrBackupCommand("testFile", nodes);
        assertEquals(testCommand.toString(), expectedCommandString);
    }

    @Test
    public void testCreateLocalAgentFileBackupCommand() {
        Command result = createFileBackupCommand("test_file");
        assertTrue(result.toString().matches(
                "\\{'command'='sudo cp test_file test_file.back ; sudo cp test_file test_file.back.[0-9]+ ; ', 'agent'='LocalAgent'\\}"),
                   result.toString());
    }

    @Test
    public void testCreatePatchCommand() throws Exception {
        Path patchDir = Paths.get("target/patches");
        File testPatchScript = patchDir.resolve(InstallType.MULTI_SERVER.toString().toLowerCase()).resolve("patch_before_update.sh").toFile();

        createDirectories(patchDir);
        writeStringToFile(testPatchScript, "${OLD_old_test_property1},${test_property1},$test_property2");

        write(testPatchScript, "${OLD_old_test_property1},${test_property1},$test_property2");

        ImmutableMap<String, String> configProperties = ImmutableMap.of("test_property1", "$test_property2", "test_property2", "property2");
        InstallOptions installOptions = new InstallOptions().setInstallType(InstallType.MULTI_SERVER)
                                                            .setConfigProperties(configProperties);

        Command command = CommandLibrary.createPatchCDECCommand(patchDir, CommandLibrary.PatchType.BEFORE_UPDATE, installOptions, new Config(ImmutableMap.of("old_test_property1", "old_property1")), new HashMap<>(ImmutableMap.of("PATCH_SCRIPT_VAR", "value")));
        assertEquals(command.toString(),
                     format("[{'command'='sudo cat target/patches/multi_server/patch_before_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed -E 's|\\$\\{OLD_old_test_property1\\}|old_property1|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp target/patches/multi_server/patch_before_update.sh', 'agent'='LocalAgent'}, "
                            + "{'command'='sudo cat target/patches/multi_server/patch_before_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed -E 's|\\$\\{test_property1\\}|property2|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp target/patches/multi_server/patch_before_update.sh', 'agent'='LocalAgent'}, "
                            + "{'command'='sudo cat target/patches/multi_server/patch_before_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed -E 's|\\$\\{test_property2\\}|property2|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp target/patches/multi_server/patch_before_update.sh', 'agent'='LocalAgent'}, "
                            + "{'command'='sudo cat target/patches/multi_server/patch_before_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed -E 's|\\$\\{PATCH_SCRIPT_VAR\\}|value|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp target/patches/multi_server/patch_before_update.sh', 'agent'='LocalAgent'}, "
                            + "{'command'='sudo cat target/patches/multi_server/patch_before_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed -E 's|\\$\\{PATH_TO_UPDATE_INFO\\}|/home/%s/codenvy/update.info|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp target/patches/multi_server/patch_before_update.sh', 'agent'='LocalAgent'}, "
                            + "{'command'='bash target/patches/multi_server/patch_before_update.sh', 'agent'='LocalAgent'}]", SYSTEM_USER_NAME));

        //  This is for manual testing propose only. After uncommenting the next commands there should be next content of file 'target/patches/multi_server/patch_before_update.sh': "old_property1,property2,$test_property2"
        //  command.execute();
        //  String content = readFileToString(testPatchScript);
        //  assertEquals(content, "");
    }

    @Test
    public void testCreateStopServiceCommand() throws AgentException {
        Command testCommand = CommandLibrary.createStopServiceCommand("test-service");
        assertEquals(testCommand.toString(), "{'command'='/bin/systemctl status test-service.service; if [ $? -eq 0 ]; then   sudo /bin/systemctl stop test-service.service; fi; ', " +
                                             "'agent'='LocalAgent'}");

        testCommand = CommandLibrary.createStopServiceCommand("test-service", testApiNode);
        assertEquals(testCommand.toString(), format("{'command'='/bin/systemctl status test-service.service; if [ $? -eq 0 ]; then   sudo /bin/systemctl stop test-service.service; fi; ', " +
                                                    "'agent'='{'host'='localhost', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
    }

    @Test
    public void testCreateStartServiceCommand() throws AgentException {
        Command testCommand = CommandLibrary.createStartServiceCommand("test-service");
        assertEquals(testCommand.toString(), "{'command'='/bin/systemctl status test-service.service; if [ $? -ne 0 ]; then   sudo /bin/systemctl start test-service.service; fi; ', " +
                                             "'agent'='LocalAgent'}");

        testCommand = CommandLibrary.createStartServiceCommand("test-service", testApiNode);
        assertEquals(testCommand.toString(), format("{'command'='/bin/systemctl status test-service.service; if [ $? -ne 0 ]; then   sudo /bin/systemctl start test-service.service; fi; ', " +
                                                    "'agent'='{'host'='localhost', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Service action 'unknown-action' isn't supported")
    public void testGetServiceManagementCommandException() {
        CommandLibrary.getServiceManagementCommand("test-service", "unknown-action");
    }

    @Test
    public void testCreateCopyFromLocalToRemoteCommand() {
        Command testCommand = CommandLibrary.createCopyFromLocalToRemoteCommand(Paths.get("local/path"), Paths.get("remote/path"),
                                                                                testApiNode.setUser(SYSTEM_USER_NAME));
        assertEquals(testCommand.toString(), format("{" +
                                                    "'command'='scp -P 22 -i '~/.ssh/id_rsa' -r -q -o StrictHostKeyChecking=no local/path %s@localhost:remote/path', 'agent'='LocalAgent'" +
                                                    "}", SYSTEM_USER_NAME));

        testCommand = CommandLibrary.createCopyFromLocalToRemoteCommand(Paths.get("local/path"), Paths.get("remote/path"), testDataNode.setUser(null));
        assertEquals(testCommand.toString(), "{" +
                                             "'command'='scp -P 22 -i '~/.ssh/id_rsa' -r -q -o StrictHostKeyChecking=no local/path 127.0.0.1:remote/path', 'agent'='LocalAgent'" +
                                             "}");

        testCommand = CommandLibrary.createCopyFromLocalToRemoteCommand(Paths.get("local/path"), Paths.get("remote/path"), testDataNode.setUser(""));
        assertEquals(testCommand.toString(), "{" +
                                             "'command'='scp -P 22 -i '~/.ssh/id_rsa' -r -q -o StrictHostKeyChecking=no local/path 127.0.0.1:remote/path', 'agent'='LocalAgent'" +
                                             "}");
    }

    @Test
    public void testCreateCopyFromRemoteToLocalCommand() {
        Command testCommand = CommandLibrary.createCopyFromRemoteToLocalCommand(Paths.get("remote/path"), Paths.get("local/path"), testApiNode.setUser(SYSTEM_USER_NAME));
        assertEquals(testCommand.toString(), format("{" +
                                                    "'command'='scp -P 22 -i '~/.ssh/id_rsa' -r -q -o StrictHostKeyChecking=no %s@localhost:remote/path local/path', 'agent'='LocalAgent'" +
                                                    "}", SYSTEM_USER_NAME));

        testCommand = CommandLibrary.createCopyFromRemoteToLocalCommand(Paths.get("remote/path"), Paths.get("local/path"), testDataNode.setUser(null));
        assertEquals(testCommand.toString(), "{" +
                                             "'command'='scp -P 22 -i '~/.ssh/id_rsa' -r -q -o StrictHostKeyChecking=no 127.0.0.1:remote/path local/path', 'agent'='LocalAgent'" +
                                             "}");

        testCommand = CommandLibrary.createCopyFromRemoteToLocalCommand(Paths.get("remote/path"), Paths.get("local/path"), testDataNode.setUser(""));
        assertEquals(testCommand.toString(), "{" +
                                             "'command'='scp -P 22 -i '~/.ssh/id_rsa' -r -q -o StrictHostKeyChecking=no 127.0.0.1:remote/path local/path', 'agent'='LocalAgent'" +
                                             "}");
    }

    @Test
    public void testCreateWaitServiceActiveCommand() throws AgentException {
        Command testCommand = CommandLibrary.createWaitServiceActiveCommand("test-service");
        assertEquals(testCommand.toString(), "{" +
                                             "'command'='doneState=\"Checking\"; " +
                                             "while [ \"${doneState}\" != \"Done\" ]; do" +
                                             "     sudo service test-service status | grep 'Active: active (running)';" +
                                             "     if [ $? -eq 0 ]; then doneState=\"Done\";" +
                                             "     else sleep 5;" +
                                             "     fi; " +
                                             "done', " +
                                             "'agent'='LocalAgent'" +
                                             "}");

        testCommand = CommandLibrary.createWaitServiceActiveCommand("test-service", testApiNode);
        assertEquals(testCommand.toString(), format("{" +
                                             "'command'='doneState=\"Checking\"; " +
                                             "while [ \"${doneState}\" != \"Done\" ]; do" +
                                             "     sudo service test-service status | grep 'Active: active (running)';" +
                                             "     if [ $? -eq 0 ]; then doneState=\"Done\";" +
                                             "     else sleep 5;" +
                                             "     fi; " +
                                             "done', " +
                                             "'agent'='{'host'='localhost', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'" +
                                             "}", SYSTEM_USER_NAME));
    }

    @Test
    public void testGetWaitServiceInactiveStatusCommand() {
        String testCommand = CommandLibrary.getWaitServiceStatusCommand("test-service", "inactive");
        assertEquals(testCommand, "doneState=\"Checking\"; " +
                                  "while [ \"${doneState}\" != \"Done\" ]; do" +
                                  "     sudo service test-service status | grep 'Active: active (running)';" +
                                  "     if [ $? -ne 0 ]; then doneState=\"Done\";" +
                                  "     else sleep 5;" +
                                  "     fi; " +
                                  "done");
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Service status 'unknown' isn't supported")
    public void testGetWaitServiceStatusCommandException() {
        CommandLibrary.getWaitServiceStatusCommand("test-service", "unknown");
    }

    @Test
    public void testCreateForcePuppetAgentCommand() throws AgentException {
        Command testCommand = CommandLibrary.createForcePuppetAgentCommand(testApiNode);
        assertEquals(testCommand.toString(), format("{" +
                                             "'command'='sudo puppet agent --onetime --ignorecache --no-daemonize --no-usecacheonfailure --no-splay --logdest=/var/log/puppet/puppet-agent.log;" +
                                             " exit 0;', " +
                                             "'agent'='{'host'='localhost', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
    }

    @Test
    public void testCreateUnpackCommand() throws AgentException {
        Path packFile = Paths.get("packFile");
        Path toDir = Paths.get("toDir");
        String pathWithinThePack = "pathWithinThePack";

        Command testCommand = CommandLibrary.createUnpackCommand(packFile, toDir);
        assertEquals(testCommand.toString(), "{'command'=' tar  -xf packFile -C toDir ', 'agent'='LocalAgent'}");

        testCommand = CommandLibrary.createUnpackCommand(packFile, toDir, pathWithinThePack);
        assertEquals(testCommand.toString(), "{'command'=' tar  -xf packFile -C toDir pathWithinThePack', 'agent'='LocalAgent'}");

        testCommand = CommandLibrary.createUnpackCommand(packFile, toDir, pathWithinThePack, true);
        assertEquals(testCommand.toString(), "{'command'='sudo tar  -xf packFile -C toDir pathWithinThePack', 'agent'='LocalAgent'}");

        testCommand = CommandLibrary.createUnpackCommand(packFile, toDir, pathWithinThePack, testApiNode);
        assertEquals(testCommand.toString(),
                     format("{'command'='sudo tar  -xf packFile -C toDir pathWithinThePack', 'agent'='{'host'='localhost', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}",
                            SYSTEM_USER_NAME));
    }

    @Test
    public void testCreateCompressCommand() throws AgentException {
        Path fromDir = Paths.get("fromDir");
        Path packFile = Paths.get("packFile");
        String pathWithinThePack = "pathWithinThePack";

        Command testCommand = CommandLibrary.createCompressCommand(fromDir, packFile, pathWithinThePack, testApiNode);
        assertEquals(testCommand.toString(),
                     format("{'command'='if sudo test -f packFile; then   sudo tar -H posix -C fromDir -z -rf packFile pathWithinThePack; else   sudo tar -H posix -C fromDir -z -cf packFile pathWithinThePack; fi; ', "
                            + "'agent'='{'host'='localhost', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}",
                            SYSTEM_USER_NAME));
    }

    @Test
    public void testCreateUncompressCommand() throws AgentException {
        Path packFile = Paths.get("packFile");
        Path toDir = Paths.get("toDir");

        Command testCommand = CommandLibrary.createUncompressCommand(packFile, toDir);
        assertEquals(testCommand.toString(), "{'command'=' tar -z -xf packFile -C toDir ', 'agent'='LocalAgent'}");
    }

    @Test
    public void testCreateReadFileCommand() throws AgentException {
        Path fileToRead = Paths.get("messages");
        Command command = CommandLibrary.createTailCommand(fileToRead, 8192, false);
        assertEquals(command.toString(), "{'command'='tail -c 8192 messages', 'agent'='LocalAgent'}");

        command = CommandLibrary.createTailCommand(fileToRead, 600, true);
        assertEquals(command.toString(), "{'command'='sudo tail -c 600 messages', 'agent'='LocalAgent'}");

        NodeConfig testNode = new NodeConfig(NodeConfig.NodeType.API, "host", "user");
        command = CommandLibrary.createTailCommand(fileToRead, 500, testNode, false);
        assertEquals(command.toString(), "{'command'='tail -c 500 messages', 'agent'='{'host'='host', 'port'='22', 'user'='user', 'identity'='[~/.ssh/id_rsa]'}'}");

        command = CommandLibrary.createTailCommand(fileToRead, 600, testNode, true);
        assertEquals(command.toString(),
                     "{'command'='sudo tail -c 600 messages', 'agent'='{'host'='host', 'port'='22', 'user'='user', 'identity'='[~/.ssh/id_rsa]'}'}");
    }

    @Test
    public void testCreateCopyCommand() throws AgentException {
        NodeConfig testNode = new NodeConfig(NodeConfig.NodeType.API, "host", "user");
        Command command = CommandLibrary.createCopyCommand(Paths.get("from"), Paths.get("to"), testNode, true);
        assertEquals(command.toString(), "{'command'='sudo cp from to', 'agent'='{'host'='host', 'port'='22', 'user'='user', 'identity'='[~/.ssh/id_rsa]'}'}");

        command = CommandLibrary.createCopyCommand(Paths.get("from"), Paths.get("to"), testNode, false);
        assertEquals(command.toString(), "{'command'='cp from to', 'agent'='{'host'='host', 'port'='22', 'user'='user', 'identity'='[~/.ssh/id_rsa]'}'}");

        command = CommandLibrary.createCopyCommand(Paths.get("from"), Paths.get("to"));
        assertEquals(command.toString(), "{'command'='cp from to', 'agent'='LocalAgent'}");

        command = CommandLibrary.createCopyCommand(Paths.get("from"), Paths.get("to"), true);
        assertEquals(command.toString(), "{'command'='sudo cp from to', 'agent'='LocalAgent'}");

        command = CommandLibrary.createCopyCommand(Paths.get("from"), Paths.get("to"), false);
        assertEquals(command.toString(), "{'command'='cp from to', 'agent'='LocalAgent'}");
    }

    @Test
    public void testCreateChmodCommand() throws AgentException {
        NodeConfig testNode = new NodeConfig(NodeConfig.NodeType.API, "host", "user");
        Command command = CommandLibrary.createChmodCommand("007", Paths.get("file"), testNode, true);
        assertEquals(command.toString(), "{'command'='sudo chmod 007 file', 'agent'='{'host'='host', 'port'='22', 'user'='user', 'identity'='[~/.ssh/id_rsa]'}'}");

        command = CommandLibrary.createChmodCommand("007", Paths.get("file"), testNode, false);
        assertEquals(command.toString(), "{'command'='chmod 007 file', 'agent'='{'host'='host', 'port'='22', 'user'='user', 'identity'='[~/.ssh/id_rsa]'}'}");

        command = CommandLibrary.createChmodCommand("007", Paths.get("file"), true);
        assertEquals(command.toString(), "{'command'='sudo chmod 007 file', 'agent'='LocalAgent'}");

        command = CommandLibrary.createChmodCommand("007", Paths.get("file"), false);
        assertEquals(command.toString(), "{'command'='chmod 007 file', 'agent'='LocalAgent'}");
    }

    @Test
    public void testCreateCheckAccessToHostCommand() {
        Command command = CommandLibrary.createCheckAccessToHostCommand("host");
        assertEquals(command.toString(), "{'command'='ping -c 1 'host' &> /dev/null', 'agent'='LocalAgent'}");
    }

    @AfterMethod
    public void tearDown() throws IOException {
        Files.deleteIfExists(TEST_FILE);
    }
}
