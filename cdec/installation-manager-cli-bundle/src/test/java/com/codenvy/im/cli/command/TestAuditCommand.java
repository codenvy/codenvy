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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.setLastModifiedTime;
import static java.nio.file.Files.write;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

/** @author Igor Vinokur */
public class TestAuditCommand extends AbstractTestCommand {
    private final static Path REPORT_DIRECTORY = Paths.get("target/audit");

    private AuditCommand spyCommand;

    @BeforeMethod
    public void initMocks() throws IOException {
        spyCommand = spy(new AuditCommand());
        performBaseMocks(spyCommand, true);
    }

    @AfterMethod
    public void tearDown() {
        deleteQuietly(REPORT_DIRECTORY.toFile());
    }

    @Test
    public void shouldPrintContentOfAuditReport() throws Exception {
        createDirectory(REPORT_DIRECTORY);
        doAnswer(invocation -> {
            Path report = createFile(REPORT_DIRECTORY.resolve("report.txt"));
            write(report, "Report_Content".getBytes());
            return null;
        }).when(mockFacade).requestAuditReport(anyString(), anyString());
        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);

        String reportContent = commandInvoker.invoke().getOutputStream();

        assertEquals(reportContent, "Report_Content\n");
    }

    @Test
    public void shouldPrintContentOfNewestAuditReport() throws Exception {
        createDirectory(REPORT_DIRECTORY);
        Path report1 = createFile(REPORT_DIRECTORY.resolve("report1.txt"));
        write(report1, "First_Report".getBytes());
        doAnswer(invocation -> {
            Path report2 = createFile(REPORT_DIRECTORY.resolve("report2.txt"));
            write(report2, "Second_Report".getBytes());
            //Creating of files in this test can be performed during the current second but
            //getLastModifiedTime() in AuditCommand returns time rounded to seconds and
            //comparing the modification time of this files will return wrong result.
            //Adding 5 second to modification time to last created file resolves this problem.
            setLastModifiedTime(report2, FileTime.from(getLastModifiedTime(report2).toMillis() + 5000, MILLISECONDS));
            return null;
        }).when(mockFacade).requestAuditReport(anyString(), anyString());
        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);

        String reportContent = commandInvoker.invoke().getOutputStream();

        assertEquals(reportContent, "Second_Report\n");
    }

    @Test
    public void shouldPrintErrorWhenExecutingAuditNotFromAdmin() throws Exception {
        doThrow(PreferenceNotFoundException.class).when(mockFacade).requestAuditReport(anyString(), anyString());

        String errorMessage = commandInvoker.invoke().disableAnsi().getOutputStream();

        assertEquals(errorMessage, "Please, login into Codenvy\n");
    }

    @Test
    public void shouldPrintErrorWhenAuditDirectoryIsEmpty() throws Exception {
        createDirectory(REPORT_DIRECTORY);
        doNothing().when(mockFacade).requestAuditReport(anyString(), anyString());

        String errorMessage = commandInvoker.invoke().disableAnsi().getOutputStream();

        assertEquals(errorMessage, "Audit directory is empty\n");
    }
}
