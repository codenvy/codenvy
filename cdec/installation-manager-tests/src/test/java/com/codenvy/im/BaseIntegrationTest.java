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
package com.codenvy.im;


import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.assertEquals;

/** @author Anatoliy Bazko */
public class BaseIntegrationTest {

    public static final String RHEL = "rhel";

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private Path baseDir;
    private Path pathToLibScript;

    @BeforeClass
    public void setUp() throws Exception {
        baseDir = Paths.get(getClass().getClassLoader().getResource("bin").getFile());
        pathToLibScript = baseDir.resolve("lib.sh");

        doExecute(baseDir.toFile(), "chmod", "+x", "lib.sh");
        doExecute(baseDir.toFile(), "chmod", "+x", "config.sh");
    }

    protected void doTest(String testScript) throws Exception {
        doExecute(baseDir.toFile(), "chmod", "+x", testScript);
        doExecute(baseDir.toFile(), "./" + testScript, pathToLibScript.toString());
    }

    protected void doTest(String testScript, String argument) throws Exception {
        doExecute(baseDir.toFile(), "chmod", "+x", testScript);
        doExecute(baseDir.toFile(), "./" + testScript, pathToLibScript.toString(), argument);
    }

    private void doExecute(File directory, String... commands) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(directory);
        Process process = builder.start();

        int exitCode = process.waitFor();

        String output = IOUtils.toString(process.getInputStream());
        if (!output.trim().isEmpty()) {
            log.info(output);
        }

        output = IOUtils.toString(process.getErrorStream());
        if (!output.trim().isEmpty()) {
            log.error(output);
        }

        assertEquals(exitCode, 0);
    }
}
