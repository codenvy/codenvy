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
package com.codenvy.im.utils;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;

/** @author Dmytro Nochevnov */
public class TestTarUtils {
    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Packing file doesn't exist")
    public void testPackNonExistsFileException() throws IOException {
        TarUtils.compressFile(Paths.get("non-exists"), Paths.get("somePack"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Packing item is a directory")
    public void testPackDirectoryAsFileException() throws IOException {
        TarUtils.compressFile(Paths.get("target"), Paths.get("somePack"));
    }
}
