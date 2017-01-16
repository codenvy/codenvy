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
package com.codenvy.im.update;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Anatoliy Bazko
 */
public class BaseTest {

    protected static final Path DOWNLOAD_DIRECTORY = Paths.get("target", "download");

    @BeforeMethod
    public void setUp() throws Exception {
        Files.createDirectories(DOWNLOAD_DIRECTORY);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(DOWNLOAD_DIRECTORY.toFile());
    }
}
