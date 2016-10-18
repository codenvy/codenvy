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
import com.google.common.io.LineProcessor;
import com.google.inject.Key;
import com.google.inject.name.Names;

import org.apache.karaf.shell.commands.Command;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.io.Files.readLines;

/**
 * Installation manager Audit command.
 *
 * @author Igor Vinokur
 */
@Command(scope = "codenvy", name = "audit", description = "Download Audit report and print it on the screen")
public class AuditCommand extends AbstractIMCommand {
    private final File auditDirectory;

    public AuditCommand() {
        auditDirectory =
                new File(InjectorBootstrap.INJECTOR.getInstance(Key.get(String.class, Names.named("installation-manager.audit_dir"))));
    }

    @Override
    protected void doExecuteCommand() throws IOException {
        try {
            getFacade().requestAuditReport(getCodenvyOnpremPreferences().getAuthToken(), getCodenvyOnpremPreferences().getUrl());
        } catch (PreferenceNotFoundException e) {
            getConsole().printErrorAndExit("Please, login into Codenvy");
            return;
        }

        Optional<File> lastModifiedFile = Stream.of(firstNonNull(auditDirectory.listFiles(), new File[0]))
                                                .max((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
        if (!lastModifiedFile.isPresent()) {
            getConsole().printErrorAndExit("Audit directory is empty");
            return;
        }

        readLines(lastModifiedFile.get(), Charset.defaultCharset(), new LineProcessor<List<String>>() {
            @Override
            public boolean processLine(String line) throws IOException {
                getConsole().print(line + "\n");
                return true;
            }

            @Override
            public List<String> getResult() {
                return null;
            }
        });
    }
}
