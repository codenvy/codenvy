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

import com.codenvy.im.cli.preferences.PreferenceNotFoundException;
import com.codenvy.im.utils.InjectorBootstrap;
import com.google.common.io.Files;
import com.google.inject.Key;
import com.google.inject.name.Names;

import org.apache.karaf.shell.commands.Command;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Installation manager Audit command.
 *
 * @author Igor Vinokur
 */
@Command(scope = "codenvy", name = "audit", description = "Download Audit report and print it to screen")
public class AuditCommand extends AbstractIMCommand {

    @Override
    protected void doExecuteCommand() throws IOException {
        try {
            getFacade().requestAuditReport(getCodenvyOnpremPreferences().getAuthToken(), getCodenvyOnpremPreferences().getUrl());
        } catch (PreferenceNotFoundException e) {
            getConsole().printErrorAndExit("Please, login into Codenvy");
            return;
        }

        File auditDirectory =
                new File(InjectorBootstrap.INJECTOR.getInstance(Key.get(String.class, Names.named("installation-manager.audit_dir"))));
        File[] reports = auditDirectory.listFiles();

        if (reports == null || reports.length == 0) {
            getConsole().printErrorAndExit("Audit directory is empty");
            return;
        }

        File lastModifiedFile = reports[0];
        for (int i = 1; i < reports.length; i++) {
            if (lastModifiedFile.lastModified() < reports[i].lastModified()) {
                lastModifiedFile = reports[i];
            }
        }

        List<String> lines = Files.readLines(lastModifiedFile, Charset.defaultCharset());
        lines.forEach(line -> getConsole().print(line + "\n"));
    }
}
