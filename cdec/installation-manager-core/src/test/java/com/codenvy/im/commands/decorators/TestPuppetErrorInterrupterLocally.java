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
package com.codenvy.im.commands.decorators;

import com.codenvy.im.agent.AgentException;
import com.codenvy.im.commands.CommandException;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.managers.NodeConfig;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/** @author Dmytro Nochevnov */
public class TestPuppetErrorInterrupterLocally extends BaseTestPuppetErrorInterrupter {

    @Test(timeOut = MOCK_COMMAND_TIMEOUT_MILLIS * 10)
    public void testInterruptWhenAddError() throws InterruptedException, IOException {
        final String[] failMessage = {null};

        final String puppetErrorMessages =
            "2015-07-29 16:01:00 +0100 /Stage[main]/Third_party::Openldap_servers::Package/Package[openldap-servers] (notice): Dependency Package[openldap] has failures: true\n"
            + "2015-07-29 16:02:00 +0100 /Stage[main]/Third_party::Openldap_servers::Package/Package[openldap-servers] (notice): Dependency Package[openldap] has failures: true\n"
            + "2015-07-29 16:03:00 +0100 /Stage[main]/Third_party::Openldap_servers::Package/Package[openldap-servers] (notice): Dependency Package[openldap] has failures: true\n";

        doAnswer(invocationOnMock -> {
            try {
                Thread.sleep(MOCK_COMMAND_TIMEOUT_MILLIS);
                failMessage[0] = "mockCommand should be interrupted by testInterrupter, but wasn't";
                return null;
            } catch (InterruptedException e) {
                // it's okay here
                return null;
            }
        }).when(mockCommand).execute();

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                Thread.sleep(MOCK_COMMAND_TIMEOUT_MILLIS / 2);

                // append error messages to puppet log file
                FileUtils.write(spyInterrupter.getPuppetLogFile().toFile(), puppetErrorMessages, true);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        });

        try {
            spyInterrupter.execute();
        } catch (Exception e) {
            assertEquals(e.getClass(), PuppetErrorException.class, Arrays.toString(e.getStackTrace()));

            String errorMessage = e.getMessage();

            Pattern errorMessagePattern = Pattern.compile("Puppet error: 'Dependency Package\\[openldap\\] has failures: true'. "
                                                          +
                                                          "At the time puppet is continue Codenvy installation in background and is trying to fix " +
                                                          "this issue. "
                                                          +
                                                          "Check administrator dashboard page http://localhost/admin to verify installation success. "
                                                          +
                                                          "If the installation eventually fails, contact support with error report " +
                                                          "target/reports/error_report_.*.tar.gz. "
                                                          +
                                                          "Installation & Troubleshooting Docs: http://docs.codenvy" +
                                                          ".com/onpremises/installation-single-node/#install-troubleshooting.");

            assertTrue(errorMessagePattern.matcher(errorMessage).find(), "Actual errorMessage: " + errorMessage);

            assertLocalErrorReport(errorMessage, logWithoutErrorMessages + puppetErrorMessages);
            return;
        }

        if (failMessage[0] != null) {
            fail(failMessage[0]);
        }

        fail("testInterrupter.execute() should throw PuppetErrorException");
    }

    @Test(timeOut = MOCK_COMMAND_TIMEOUT_MILLIS * 10)
    public void testNotInterruptWhenAddNoError() throws InterruptedException, IOException {
        final String[] failMessage = {null};
        final String expectedResult = "okay";

        doAnswer(invocationOnMock -> {
                try {
                    Thread.sleep(MOCK_COMMAND_TIMEOUT_MILLIS);
                    return expectedResult;
                } catch (InterruptedException e) {
                    failMessage[0] = "mockCommand should not be interrupted by testInterrupter, but was.";
                    return null;
                }
        }).when(mockCommand).execute();

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                Thread.sleep(MOCK_COMMAND_TIMEOUT_MILLIS / 2);

                // append non-error message into puppet log file
                String errorMessage = "2015-06-08 15:56:59 test puppet-agent[10240]: dummy message";

                FileUtils.write(spyInterrupter.getPuppetLogFile().toFile(), errorMessage, true);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        });

        String result = spyInterrupter.execute();
        assertEquals(result, expectedResult);

        if (failMessage[0] != null) {
            fail(failMessage[0]);
        }
    }

    @Test(expectedExceptions = CommandException.class,
            expectedExceptionsMessageRegExp = "error",
            timeOut = MOCK_COMMAND_TIMEOUT_MILLIS * 10)
    public void testRethrowCommandExceptionByInterrupter() throws InterruptedException, IOException {
        final String[] failMessage = {null};

        doAnswer(invocationOnMock -> {
            try {
                Thread.sleep(MOCK_COMMAND_TIMEOUT_MILLIS);
                throw new CommandException("error");
            } catch (InterruptedException e) {
                failMessage[0] = "mockCommand should not be interrupted by testInterrupter, but was.";
                return null;
            }
        }).when(mockCommand).execute();

        spyInterrupter.execute();

        if (failMessage[0] != null) {
            fail(failMessage[0]);
        }
    }

    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "error",
            timeOut = MOCK_COMMAND_TIMEOUT_MILLIS * 10)
    public void testRethrowRuntimeExceptionByInterrupter() throws InterruptedException, IOException {
        final String[] failMessage = {null};

        doAnswer(invocationOnMock -> {
            try {
                Thread.sleep(MOCK_COMMAND_TIMEOUT_MILLIS);
                throw new RuntimeException("error");
            } catch (InterruptedException e) {
                failMessage[0] = "mockCommand should not be interrupted by spyInterrupter, but was.";
                return null;
            }
        }).when(mockCommand).execute();

        spyInterrupter.execute();

        if (failMessage[0] != null) {
            fail(failMessage[0]);
        }
    }

    @Test(dataProvider = "getDataForTestReadNLines")
    public void testReadNLines(String lines, List<String> expectedLines) throws CommandException, AgentException {
        doReturn(lines).when(mockCommand).execute();
        doReturn(mockCommand).when(spyInterrupter).createReadFileCommand(null);

        List<String> result = spyInterrupter.readNLines(null);
        assertEquals(result, expectedLines);
    }

    @DataProvider
    public Object[][] getDataForTestReadNLines() {
        return new Object[][] {
            {null, Collections.emptyList()},
            {"", Collections.singletonList("")},
            {"line1", Collections.singletonList("line1")},
            {"line1\nline2", Arrays.asList("line1", "line2")}
        };
    }

    @Test
    public void testGetPuppetLogFile() {
        PuppetErrorInterrupter puppetErrorInterrupter = new PuppetErrorInterrupter(null, (List<NodeConfig>) null ,null);
        assertEquals(puppetErrorInterrupter.getPuppetLogFile().toString(), "/var/log/puppet/puppet-agent.log");
    }

    @Test
    public void testIsReadPuppetLogCommand() {
        String anyCommand = "test";
        assertFalse(PuppetErrorInterrupter.isReadPuppetLogCommand(anyCommand));

        String readPuppetLocCommand = "sudo tailf " + spyInterrupter.getPuppetLogFile();
        assertFalse(PuppetErrorInterrupter.isReadPuppetLogCommand(readPuppetLocCommand));
    }

    @Override
    public PuppetError getTestPuppetError() {
        return new PuppetError(null, "Dependency Exec[import_base_schema] has failures: true");
    }

    @Override
    public NodeConfig getTestNode() {
        return null;
    }

    public InstallType getInstallType() {
        return InstallType.SINGLE_SERVER;
    }

    @Override
    public PuppetErrorInterrupter getSpyInterrupter() {
        return spy(new PuppetErrorInterrupter(mockCommand, mockConfigManager));
    }

}
