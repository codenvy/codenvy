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
package com.codenvy.im.commands;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.codenvy.im.commands.ReadRedHatVersionCommand.fetchRedHatVersion;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Anatoliy Bazko
 */
public class TestReadRedHatVersionCommand {
    private static final Path RELEASE_FILE = Paths.get("target", "redhat-release");

    @Test
    public void shouldReturnNullIfFileAbsent() throws Exception {
        assertNull(fetchRedHatVersion());
    }

    @BeforeMethod
    @AfterMethod
    public void clean() throws IOException {
        if(Files.exists(RELEASE_FILE)) {
            Files.delete(RELEASE_FILE);
        }
    }

    @Test(dataProvider = "GetTestedVersions")
    public void shouldReturnHostNameNoWhiteSpacesInLine(String str, String expectedVersion) throws Exception {
        FileUtils.writeStringToFile(RELEASE_FILE.toFile(), str);
        assertEquals(fetchRedHatVersion(), expectedVersion, "'" + str + "'");
    }

    @DataProvider(name = "GetTestedVersions")
    public static Object[][] GetTestedVersions() {
        return new Object[][]{{"RedHat 7.7 OS", "7.7"},
                              {"RedHat 7.7 ", "7.7"},
                              {" 7 ", "7"}};
    }
}
