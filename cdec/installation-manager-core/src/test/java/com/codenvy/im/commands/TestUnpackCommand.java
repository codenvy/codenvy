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
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static org.apache.commons.io.FileUtils.listFiles;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Dmytro Nochevnov
 */
public class TestUnpackCommand {
    private static final Path TEST_RESOURCE_ROOT = Paths.get(TestUnpackCommand.class.getClassLoader().getResource("../test-classes/").getPath());

    private static final String PACK_NAME = "installation-manager-1.0.0-binary.tar.gz";
    private static final Path PATH_TO_PACK = TEST_RESOURCE_ROOT.resolve(PACK_NAME);

    private static final String DIR_TO_UNPACK_NAME = "unpack";
    private static final Path DIR_TO_UNPACK = TEST_RESOURCE_ROOT.resolve(DIR_TO_UNPACK_NAME);

    @AfterMethod
    public void deleteUnpackedFiles() throws IOException {
        FileUtils.deleteDirectory(DIR_TO_UNPACK.toFile());
    }

    @Test
    public void testDescription() {
        UnpackCommand testCommand = new UnpackCommand(PATH_TO_PACK, DIR_TO_UNPACK, "unpack");
        assertEquals(testCommand.getDescription(), "unpack");
    }

    @Test
    public void testToString() {
        UnpackCommand testCommand = new UnpackCommand(Paths.get("archive"), Paths.get("dirToUnpack"), "unpack");
        assertEquals(testCommand.toString(), "Unpack 'archive' into 'dirToUnpack'");
    }

    @Test
    public void testExecution() throws CommandException {
        UnpackCommand testCommand = new UnpackCommand(PATH_TO_PACK, DIR_TO_UNPACK, "unpack");
        String result = testCommand.execute();
        assertNull(result);

        Collection<File> unpackedFiles = listFiles(DIR_TO_UNPACK.toFile(), null, true);
        assertEquals(unpackedFiles.size(), 3);
        assertTrue(unpackedFiles.toString().contains("installation-manager-1.0.0-binary.tar.gz"));
        assertTrue(unpackedFiles.toString().contains("installation-manager-cli-1.0.0-binary.tar.gz"));
        assertTrue(unpackedFiles.toString().contains("testDir/test.txt"));
    }

    @Test(expectedExceptions = CommandException.class,
          expectedExceptionsMessageRegExp = "non-existed")
    public void testCommandException() throws CommandException {
        UnpackCommand testCommand = new UnpackCommand(Paths.get("non-existed"), DIR_TO_UNPACK, "unpack");
        testCommand.execute();
    }
}
