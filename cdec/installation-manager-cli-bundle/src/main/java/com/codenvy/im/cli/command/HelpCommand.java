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
package com.codenvy.im.cli.command;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.impl.jline.Branding;
import org.fusesource.jansi.Ansi;

import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD_OFF;
import static org.fusesource.jansi.Ansi.Color.CYAN;

/**
 * Defines a global help for Codenvy commands available.
 * This will be used when using for example ./codenvy without arguments as the name of this command is empty and is in the codenvy prefix.
 *
 * @author Florent Benoit
 * @author Anatoliy Bazko
 */
@Command(scope = "codenvy", name = "", description = "Help")
public class HelpCommand extends AbstractIMCommand {

    @Override
    protected void doExecuteCommand() throws Exception {
        Ansi buffer = Ansi.ansi();

        // add the branding banner
        buffer.a(Branding.loadBrandingProperties().getProperty("banner"));

        // display commands
        buffer.a(INTENSITY_BOLD).a("COMMANDS").a(INTENSITY_BOLD_OFF).a("\n");

        String value = buildAsciiForm().withEntry(withCyan("add-node"), "Add new Codenvy node such as builder or runner")
                                       .withEntry(withCyan("audit"), "Download Audit report and print it on the screen")
                                       .withEntry(withCyan("backup"), "Backup all Codenvy data")
                                       .withEntry(withCyan("config"), "Get installation manager configuration")
                                       .withEntry(withCyan("download"), "Download artifacts or print the list of installed ones")
                                       .withEntry(withCyan("install"), "Install, update artifact or print the list of already installed ones")
                                       .withEntry(withCyan("login"), "Login to Codenvy")
                                       .withEntry(withCyan("remove-node"), "Remove Codenvy node")
                                       .withEntry(withCyan("restore"), "Restore Codenvy data")
                                       .withEntry(withCyan("version"), "Print the list of available latest versions and installed ones")
                                       .alphabeticalSort().toAscii();

        buffer.a(value);

        buffer.a("\n");
        buffer.a("Use '\u001B[1m[command] --help\u001B[0m' for help on a specific command.\r\n");
        System.out.println(buffer.toString());
    }

    private String withCyan(String name) {
        return Ansi.ansi().fg(CYAN).a(name).reset().toString();
    }
}
