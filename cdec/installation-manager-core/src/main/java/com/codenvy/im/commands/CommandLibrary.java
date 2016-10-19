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

import com.codenvy.im.agent.AgentException;
import com.codenvy.im.commands.decorators.PuppetErrorInterrupter;
import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.InstallOptions;
import com.codenvy.im.managers.NodeConfig;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.codenvy.im.commands.SimpleCommand.createCommand;
import static java.lang.String.format;

/** @author Dmytro Nochevnov */
public class CommandLibrary {

    private static final String STOP     = "stop";
    private static final String START    = "start";
    private static final String ACTIVE   = "active";
    private static final String INACTIVE = "inactive";

    /** @return Empty command which returns empty string after execution */
    public static final Command EMPTY_COMMAND = new EmptyCommand();

    /** @return Command to ping host by dns */
    public static Command createCheckAccessToHostCommand(String dns) {
        return createCommand(format("ping -c 1 '%s' &> /dev/null", dns));
    }

    /** @return Command to patch CDEC before or after install **/
    public static Command createPatchCDECCommand(Path patches, PatchType beforeUpdate, InstallOptions installOptions, Config oldCDECConfig, Map<String, String> patchScriptProperties) {
        return new PatchCDECCommand(patches, beforeUpdate, installOptions, oldCDECConfig, patchScriptProperties);
    }

    public enum PatchType {
        BEFORE_UPDATE, AFTER_UPDATE
    }

    private CommandLibrary() {
    }

    public static Command createPuppetPropertyReplaceCommand(Path file, String property, String value) {
        return createPuppetPropertyReplaceCommand(file, property, value, true);
    }

    public static Command createPuppetPropertyReplaceCommand(Path file, String property, String value, boolean withSudo) {
        String replacingToken = format("(~n[^#]*\\$)%s *= *\"[^\"]*\"", property);
        String replacement = format("\\1%s = \"%s\"", property, value);
        return createReplaceCommand(file.toString(), replacingToken, replacement, withSudo);
    }

    public static Command createReplaceCommand(Path file, String replacingToken, String replacement) {
        return createReplaceCommand(file.toString(), replacingToken, replacement, true);
    }

    public static Command createReplaceCommand(String file, String replacingToken, String replacement) {
        return createReplaceCommand(file, replacingToken, replacement, true);
    }

    public static Command createReplaceCommand(Path file, String replacingToken, String replacement, NodeConfig node) throws AgentException {
        return createCommand(getReplaceCommand(file.toString(), replacingToken, replacement, true), node);
    }

    public static Command createReplaceCommand(String file, String replacingToken, String replacement, boolean withSudo) {
        return createCommand(getReplaceCommand(file, replacingToken, replacement, withSudo));
    }

    /**
     * The idea is to treat file as a single line and replace text respectively.
     * Extended regular expressions syntax of 'sed' command is using for 'replacingToken' parameter (https://www.gnu.org/software/sed/manual/html_node/Extended-regexps.html)
     */
    private static String getReplaceCommand(String file, String replacingToken, String replacement, boolean withSudo) {

        return format("%4$scat %3$s | sed ':a;N;$!ba;s/\\n/~n/g' | sed -E 's|%1$s|%2$s|g' | sed 's|~n|\\n|g' > tmp.tmp && %4$smv tmp.tmp %3$s",
                      replacingToken,
                      replacement.replace("\\$", "\\\\$")                   // is needed to stay "\$" symbols as it
                                 .replace("\n", "\\n")                      // is needed to replace "\\n" on "~n" for transforming multi-line content of file into the one-line form
                                 .replace("|", "\\|").replace("&", "\\&"),
                      file,
                      withSudo ? "sudo " : "");
    }

    /**
     * Update existed text in the file on node, or append it to the end of the file if replacement pattern doesn't find any occurrences.
     * @param fileToUpdate
     * @param textToPutIntoFile  (symbol "$" doesn't supported)
     * @param replacementPattern
     * @param node
     * @return command to update file on node
     * @throws IOException
     */
    public static Command createUpdateFileCommand(Path fileToUpdate, String textToPutIntoFile, String replacementPattern, NodeConfig node) throws IOException {
        return createCommand(getUpdateFileCommand(fileToUpdate, textToPutIntoFile, replacementPattern, true),
                             node);
    }

    /**
     * Update existed text in the local file, or append it to the end of the file if replacement pattern doesn't find any occurrences.
     * @param fileToUpdate
     * @param textToPutIntoFile  (symbol "$" doesn't supported)
     * @param replacementPattern
     * @return command to update local file
     */
    public static Command createUpdateFileCommand(Path fileToUpdate, String textToPutIntoFile, String replacementPattern) {
        return createCommand(getUpdateFileCommand(fileToUpdate, textToPutIntoFile, replacementPattern, true));
    }

    static String getUpdateFileCommand(Path fileToUpdate, String textToPutIntoFile, String replacementPattern, boolean withSudo) {
        String command = format("if test -n \"%3$s\" && sudo grep -Eq \"%3$s\" \"%1$s\"; then\n"
                               + "  sudo sed -i \"s|%3$s|%2$s|\" \"%1$s\" &> /dev/null\n"        // replace
                               + "fi\n"
                               + "if ! sudo grep -Eq \"^%4$s$\" \"%1$s\"; then\n"
                               + "  echo \"%4$s\" | sudo tee --append \"%1$s\" &> /dev/null\n"   // append
                               + "fi",
                               fileToUpdate,
                               textToPutIntoFile.replace("\\$", "\\\\$").replace("\"", "\\\"").replace("\n", "\\n").replace("|", "\\|").replace("&", "\\&"),
                               replacementPattern,
                               textToPutIntoFile.replace("\"", "\\\""));
        return withSudo ? command : command.replaceAll("sudo ", "");
    }

    /**
     * Append text to file, if 'grep [checkIfTextPresentRegex]' command doesn't find any occurrences.
     * @param pathToFile
     * @param textToAppend
     * @param checkIfTextPresentRegex
     * @return command to append non-existed content to file
     */
    public static Command createAppendTextIfAbsentToFileCommand(Path pathToFile, String textToAppend, String checkIfTextPresentRegex) {
        return createCommand(getAppendTextToFileCommand(pathToFile, textToAppend, checkIfTextPresentRegex, true));
    }

    static String getAppendTextToFileCommand(Path pathToFile, String textToAppend, String checkIfTextPresentRegex, boolean withSudo) {
        String command = format("if ! sudo grep -Eq \"%3$s\" %1$s; then\n" +
                                "  echo \"%2$s\" | sudo tee --append %1$s > /dev/null\n" +
                                "fi",
                                pathToFile.toString(),
                                textToAppend,
                                checkIfTextPresentRegex);
        return withSudo ? command : command.replaceAll("sudo ", "");
    }

    public static Command createFileRestoreOrBackupCommand(final String file) {
        return createCommand(getFileRestoreOrBackupCommand(file));
    }

    public static Command createFileRestoreOrBackupCommand(final String file, List<NodeConfig> nodes) throws AgentException {
        return MacroCommand.createCommand(getFileRestoreOrBackupCommand(file), null, nodes);
    }

    public static Command createFileRestoreOrBackupCommand(final String file, NodeConfig node) throws AgentException {
        return MacroCommand.createCommand(getFileRestoreOrBackupCommand(file), null, ImmutableList.of(node));
    }

    protected static String getFileRestoreOrBackupCommand(final String file) {
        final String backupFile = file + ".back";
        return format("if sudo test -f %2$s; then " +
                      "    if ! sudo test -f %1$s; then " +
                      "        sudo cp %2$s %1$s; " +
                      "    else " +
                      "        sudo cp %1$s %2$s; " +
                      "    fi " +
                      "fi",
                      backupFile,
                      file);
    }

    public static Command createFileBackupCommand(String file, NodeConfig node) throws AgentException {
        return createCommand(getFileBackupCommand(file), node);
    }

    public static Command createFileBackupCommand(final String file) {
        return createCommand(getFileBackupCommand(file));
    }

    public static Command createFileBackupCommand(final Path file) {
        return createCommand(getFileBackupCommand(file.toString()));
    }

    protected static String getFileBackupCommand(final String file) {
        return format("sudo cp %1$s %1$s.back ; " +
                      "sudo cp %1$s %1$s.back.%2$s ; ",
                      file,
                      System.nanoTime());
    }

    public static Command createRepeatCommand(Command command) {
        return new RepeatCommand(command);
    }

    public static Command createStopServiceCommand(String serviceName) {
        return createCommand(getServiceManagementCommand(serviceName, STOP));
    }

    public static Command createStopServiceCommand(String serviceName, NodeConfig node) throws AgentException {
        return createCommand(getServiceManagementCommand(serviceName, STOP), node);
    }

    public static Command createStartServiceCommand(String serviceName) {
        return createCommand(getServiceManagementCommand(serviceName, START));
    }

    public static Command createStartServiceCommand(String serviceName, NodeConfig node) throws AgentException {
        return createCommand(getServiceManagementCommand(serviceName, START), node);
    }

    protected static String getServiceManagementCommand(String serviceName, String action) {
        switch (action) {
            case STOP:
                return format("/bin/systemctl status %1$s.service; "
                              + "if [ $? -eq 0 ]; then "
                              + "  sudo /bin/systemctl stop %1$s.service; "
                              + "fi; ",
                              serviceName);

            case START:
                return format("/bin/systemctl status %1$s.service; "
                              + "if [ $? -ne 0 ]; then "
                              + "  sudo /bin/systemctl start %1$s.service; "
                              + "fi; ",
                              serviceName);
            default:
                throw new IllegalArgumentException(format("Service action '%s' isn't supported", action));
        }
    }

    /**
     * @return command "sudo tar -C {fromDir} -rf {packFile} {pathWithinThePack}", if packFile exists, or
     * command "sudo tar -C {fromDir} -cf {packFile} {pathWithinThePack}", if packFile doesn't exists.
     *
     * Don't gzip to be able to update pack in future.
     */
    public static Command createPackCommand(Path fromDir, Path packFile, String pathWithinThePack, boolean needSudo) {
        return createCommand(getPackCommand(fromDir, packFile, pathWithinThePack, needSudo, false));
    }

    /**
     * @return for certain node, the command "sudo tar -C {fromDir} -rf {packFile} {pathWithinThePack}", if packFile exists, or
     * command "sudo tar -C {fromDir} -cf {packFile} {pathWithinThePack}", if packFile doesn't exists.
     *
     * Don't gzip to be able to update pack in future.
     */
    public static Command createPackCommand(Path fromDir, Path packFile, String pathWithinThePack, NodeConfig node) throws AgentException {
        return createCommand(getPackCommand(fromDir, packFile, pathWithinThePack, true, false), node);
    }

    /**
     * @return for certain node, the command "sudo tar -C {fromDir} -z -rf {packFile} {pathWithinThePack}", if packFile exists, or
     * command "sudo tar -C {fromDir} -z -cf {packFile} {pathWithinThePack}", if packFile doesn't exists.
     */
    public static Command createCompressCommand(Path fromDir, Path packFile, String pathWithinThePack, NodeConfig node) throws AgentException {
        return createCommand(getPackCommand(fromDir, packFile, pathWithinThePack, true, true), node);
    }

    private static String getPackCommand(Path fromDir, Path packFile, String pathWithinThePack, boolean needSudo, boolean useCompression) {
        return format("if %1$s test -f %3$s; then " +
                      "  %1$s tar -H posix -C %2$s %5$s -rf %3$s %4$s; " +
                      "else " +
                      "  %1$s tar -H posix -C %2$s %5$s -cf %3$s %4$s; " +
                      "fi; ",
                      needSudo ? "sudo" : "",
                      fromDir,
                      packFile,
                      pathWithinThePack != null ? pathWithinThePack : "",
                      useCompression ? "-z" : "");
    }

    public static Command createUnpackCommand(Path packFile, Path toDir, String pathWithinThePack, boolean needSudo) {
        return createCommand(getUnpackCommand(packFile, toDir, pathWithinThePack, needSudo, false));
    }

    public static Command createUnpackCommand(Path packFile, Path toDir, String pathWithinThePack) {
        return createCommand(getUnpackCommand(packFile, toDir, pathWithinThePack, false, false));
    }

    public static Command createUnpackCommand(Path packFile, Path toDir) {
        return createCommand(getUnpackCommand(packFile, toDir, null, false, false));
    }

    public static Command createUncompressCommand(Path packFile, Path toDir) {
        return createCommand(getUnpackCommand(packFile, toDir, null, false, true));
    }

    public static Command createUnpackCommand(Path packFile, Path toDir, String pathWithinThePack, NodeConfig node) throws AgentException {
        return createCommand(getUnpackCommand(packFile, toDir, pathWithinThePack, true, false), node);
    }

    private static String getUnpackCommand(Path packFile, Path toDir, String pathWithinThePack, boolean needSudo, boolean useCompression) {
        return format("%s tar %s -xf %s -C %s %s",
                      needSudo ? "sudo" : "",
                      useCompression ? "-z" : "",
                      packFile,
                      toDir,
                      pathWithinThePack != null ? pathWithinThePack : "");
    }

    public static Command createCopyFromRemoteToLocalCommand(Path fromPath, Path toPath, NodeConfig remote) {
        String userNamePrefix = "";
        if (remote.getUser() != null
            && !remote.getUser().isEmpty()) {
            userNamePrefix = format("%s@", remote.getUser());
        }

        String fromRemotePath = format("%s%s:%s", userNamePrefix, remote.getHost(), fromPath);
        return createCommand(getScpCommand(fromRemotePath, toPath.toString(), remote.getPort(), remote.getPrivateKeyFile()));
    }

    public static Command createCopyFromLocalToRemoteCommand(Path fromPath, Path toPath, NodeConfig remote) {
        String userNamePrefix = "";
        if (remote.getUser() != null
            && !remote.getUser().isEmpty()) {
            userNamePrefix = format("%s@", remote.getUser());
        }

        String toRemotePath = format("%s%s:%s", userNamePrefix, remote.getHost(), toPath);
        return createCommand(getScpCommand(fromPath.toString(), toRemotePath, remote.getPort(), remote.getPrivateKeyFile()));
    }

    private static String getScpCommand(String fromPath, String toPath, int port, Path privateKeyFile) {
        return format("scp -P %s -i '%s' -r -q -o StrictHostKeyChecking=no %s %s", port, privateKeyFile, fromPath, toPath);
    }

    public static Command createWaitServiceActiveCommand(String service) {
        return createCommand(getWaitServiceStatusCommand(service, ACTIVE));
    }

    public static Command createWaitServiceActiveCommand(String service, NodeConfig node) throws AgentException {
        return createCommand(getWaitServiceStatusCommand(service, ACTIVE), node);
    }

    protected static String getWaitServiceStatusCommand(String service, String status) {
        String operator;
        switch (status) {
            case ACTIVE:
                operator = "-eq";
                break;

            case INACTIVE:
                operator = "-ne";
                break;

            default:
                throw new IllegalArgumentException(format("Service status '%s' isn't supported", status));
        }

        return format("doneState=\"Checking\"; " +
                      "while [ \"${doneState}\" != \"Done\" ]; do " +
                      "    sudo service %s status | grep 'Active: active (running)'; " +
                      "    if [ $? %s 0 ]; then doneState=\"Done\"; " +
                      "    else sleep 5; " +
                      "    fi; " +
                      "done", service, operator);
    }

    /**
     * Creates command to force running puppet agent.
     */
    public static Command createForcePuppetAgentCommand(NodeConfig node) throws AgentException {
        return createCommand(getForcePuppetAgentCommand(), node);
    }


    /**
     * Creates command to force running puppet agent.
     */
    public static Command createForcePuppetAgentCommand() throws AgentException {
        return createCommand(getForcePuppetAgentCommand());
    }

    /**
     * Creates command to force running puppet agent. Log destination defined by constant PuppetErrorInterrupter.PATH_TO_PUPPET_LOG
     */
    private static String getForcePuppetAgentCommand() {
        return format("sudo puppet agent --onetime --ignorecache --no-daemonize --no-usecacheonfailure --no-splay --logdest=%s; exit 0;", // make sure there is no "--detailed-exitcodes" option
        PuppetErrorInterrupter.PATH_TO_PUPPET_LOG);
    }

    public static Command createTailCommand(Path file, int bytes, boolean needSudo) {
        return SimpleCommand.createCommandWithoutLogging(getTailCommand(file, bytes, needSudo));
    }

    public static Command createTailCommand(Path file, int bytes, NodeConfig node, boolean needSudo) throws AgentException {
        return SimpleCommand.createCommandWithoutLogging(getTailCommand(file, bytes, needSudo), node);
    }

    private static String getTailCommand(Path file, int bytes, boolean needSudo) {
        String command = format("tail -c %s %s", bytes, file);
        if (needSudo) {
            command = "sudo " + command;
        }

        return command;
    }

    public static Command createChmodCommand(String mode, Path file, boolean useSudo) {
        return createCommand(getChmodCommand(mode, file, useSudo));
    }

    public static Command createChmodCommand(String mode, Path file, NodeConfig node, boolean useSudo) throws AgentException {
        return createCommand(getChmodCommand(mode, file, useSudo), node);
    }

    private static String getChmodCommand(String mode, Path file, boolean useSudo) {
        String command = format("chmod %s %s", mode, file);

        if (useSudo) {
            command = "sudo " + command;
        }

        return command;
    }

    public static Command createCopyCommand(Path from, Path to, NodeConfig node, boolean useSudo) throws AgentException {
        return createCommand(getCopyCommand(from, to, useSudo), node);
    }

    /**
     * @return copy bash command where sudo isn't used.
     */
    public static Command createCopyCommand(Path from, Path to) {
        return createCommand(getCopyCommand(from, to, false));
    }

    public static Command createCopyCommand(Path from, Path to, boolean useSudo) {
        return createCommand(getCopyCommand(from, to, useSudo));
    }

    private static String getCopyCommand(Path from, Path to, boolean useSudo) {
        String command = format("cp %s %s", from, to);

        if (useSudo) {
            command = "sudo " + command;
        }

        return command;
    }

    public static Command createCheckSudoRightsWithoutPasswordCommand(NodeConfig node) throws AgentException {
        return createCommand(getCheckSudoRightsWithoutPasswordCommand(), node);
    }

    private static String getCheckSudoRightsWithoutPasswordCommand() {
        return "sudo -k -n true 2> /dev/null";
    }

    /**
     * @param remoteHost to check
     * @param remotePort to check
     * @param node from where to check
     */
    public static Command createCheckRemotePortOpenedCommand(String remoteHost, int remotePort, NodeConfig node) throws AgentException {
        return createCommand(getCheckRemotePortOpenedCommand(remoteHost, remotePort), node);
    }

    /**
     * @see "http://www.unix.com/302910488-post2.html?s=c351328813d6bc7a509c0a5dee5dc9e7"
     */
    private static String getCheckRemotePortOpenedCommand(String remoteHost, int remotePort) {
        return format("sleep 3 && echo >/dev/tcp/%s/%s", remoteHost, remotePort);
    }

    private static class EmptyCommand implements Command {
        @Override
        public String execute() throws CommandException {
            return "";
        }

        @Override
        public String getDescription() {
            return "Empty command";
        }

        @Override
        public String toString() {
            return getDescription();
        }
    }
}
