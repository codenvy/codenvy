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

import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.InstallOptions;
import com.codenvy.im.managers.InstallType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codenvy.im.commands.CommandLibrary.createReplaceCommand;
import static com.codenvy.im.commands.SimpleCommand.createCommand;
import static com.codenvy.im.utils.InjectorBootstrap.INSTALLATION_MANAGER_BASE_DIR;
import static com.codenvy.im.utils.InjectorBootstrap.getProperty;
import static java.lang.String.format;
import static java.nio.file.Files.exists;

/**
 * Command to patch CDEC before or after install.
 * @author Dmytro Nochevnov
 */
public class PatchCDECCommand implements Command {

    public static final String UPDATE_INFO                            = "update.info";
    public static final String PATH_TO_UPDATE_INFO_SCRIPT_VARIABLE    = "PATH_TO_UPDATE_INFO";
    public static final String PATH_TO_MANIFEST_PATCH_SCRIPT_VARIABLE = "PATH_TO_MANIFEST";

    private Path patchDir;

    private final CommandLibrary.PatchType patchType;
    private final InstallOptions           installOptions;
    private final Config                   oldCDECConfig;
    private final Map<String, String>      patchProperties;

    public PatchCDECCommand(Path patchDir, CommandLibrary.PatchType patchType, InstallOptions installOptions, Config oldCDECConfig, Map<String, String> patchScriptProperties) {
        this.patchDir = patchDir;
        this.patchType = patchType;
        this.installOptions = installOptions;
        this.oldCDECConfig = oldCDECConfig;
        this.patchProperties = patchScriptProperties;
    }

    @Override
    public String execute() throws CommandException {
        return getCommand().execute();
    }

    @Override
    public String getDescription() {
        return "Command to patch CDEC before or after install.";
    }

    @Override
    public String toString() {
        return getCommand().toString();
    }

    private Command getCommand() {
        List<Command> commands = new ArrayList<>();

        Path relativePatchFilePath = getRelativePatchFilePath(patchType, installOptions.getInstallType());
        Path patchFile = patchDir.resolve(relativePatchFilePath);
        if (exists(patchFile)) {
            // substitute old CDEC properties
            for (Map.Entry<String, String> e : oldCDECConfig.getProperties().entrySet()) {
                String property = e.getKey();
                String value = oldCDECConfig.getValue(property);  // work around enclosed properties like "user_ldap_user_container_dn=ou=$user_ldap_users_ou,$user_ldap_dn"

                commands.add(createReplaceCommand(patchFile.toString(),
                                                  format("\\$\\{OLD_%s\\}", property),
                                                  value));
            }

            // substitute new CDEC properties
            Config config = new Config(installOptions.getConfigProperties());
            for (Map.Entry<String, String> e : config.getProperties().entrySet()) {
                String property = e.getKey();
                String value = config.getValue(property);  // work around enclosed properties like "user_ldap_user_container_dn=ou=$user_ldap_users_ou,$user_ldap_dn"

                commands.add(createReplaceCommand(patchFile.toString(),
                                                  format("\\$\\{%s\\}", property),
                                                  value));
            }

            // substitute patch script properties
            patchProperties.put(PatchCDECCommand.PATH_TO_UPDATE_INFO_SCRIPT_VARIABLE,
                                Paths.get(getProperty(INSTALLATION_MANAGER_BASE_DIR),
                                          PatchCDECCommand.UPDATE_INFO).toString());

            for (Map.Entry<String, String> e : patchProperties.entrySet()) {
                String property = e.getKey();
                String value = e.getValue();

                commands.add(createReplaceCommand(patchFile.toString(),
                                                  format("\\$\\{%s\\}", property),
                                                  value));
            }

            commands.add(createCommand(format("bash %s", patchFile)));
        }

        return new MacroCommand(commands, getDescription());
    }

    /**
     * Return relative path to patch file, for example:  "single_server/patch_before_update.sh"
     */
    private Path getRelativePatchFilePath(CommandLibrary.PatchType patchType, InstallType installType) {
        String pathFilename = format("patch_%s.sh", patchType.toString().toLowerCase());
        return Paths.get(installType.toString().toLowerCase())
                    .resolve(pathFilename);
    }

}
