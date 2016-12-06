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
package com.codenvy.api.license.server;

import com.codenvy.api.license.exception.SystemLicenseNotFoundException;

import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatolii Bazko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class SystemLicenseStorageTest {

    private static final String LICENSE_TEXT               = "# (id: 1)\nlicense text";
    private static final String ACTIVATED_LICENSE_TEXT     = "# (id: 1)\nactivated license text";
    private static final String NEW_LICENSE_TEXT           = "# (id: 2)\nnew license text";
    private static final String NEW_ACTIVATED_LICENSE_TEXT = "# (id: 2)\nnew activated license text";

    private Path testDirectory;
    private Path licenseFile;
    private Path activatedLicenseFile;

    private SystemLicenseStorage licenseStorage;

    @BeforeMethod
    public void setUp() throws Exception {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(resource);

        Path targetDir = Paths.get(resource.getPath()).getParent();
        testDirectory = targetDir.resolve(NameGenerator.generate("license-storage-", 4));
        licenseFile = testDirectory.resolve("license");
        activatedLicenseFile = testDirectory.resolve("license.activated");
        Files.createDirectories(testDirectory);

        licenseStorage = new SystemLicenseStorage(licenseFile.toString());
    }

    @AfterMethod
    public void tearDown() throws Exception {
        IoUtil.deleteRecursive(testDirectory.toFile());
    }

    @Test
    public void shouldPersistLicense() throws Exception {
        licenseStorage.persistLicense(LICENSE_TEXT);

        assertTrue(Files.exists(licenseFile));
        assertEquals(LICENSE_TEXT.getBytes(), Files.readAllBytes(licenseFile));
    }

    @Test
    public void shouldUpdateLicense() throws Exception {
        licenseStorage.persistLicense(LICENSE_TEXT);

        licenseStorage.persistLicense(NEW_LICENSE_TEXT);

        assertTrue(Files.exists(licenseFile));
        assertEquals(NEW_LICENSE_TEXT.getBytes(), Files.readAllBytes(licenseFile));
    }

    @Test
    public void shouldInitializeStorageAndPersist() throws Exception {
        IoUtil.deleteRecursive(testDirectory.toFile());

        licenseStorage.persistLicense(LICENSE_TEXT);

        assertTrue(Files.exists(licenseFile));
        assertEquals(LICENSE_TEXT.getBytes(), Files.readAllBytes(licenseFile));
    }

    @Test
    public void shouldPersistActivatedLicense() throws Exception {
        licenseStorage.persistActivatedLicense(ACTIVATED_LICENSE_TEXT);

        assertTrue(Files.exists(activatedLicenseFile));
        assertEquals(ACTIVATED_LICENSE_TEXT.getBytes(), Files.readAllBytes(activatedLicenseFile));
    }

    @Test
    public void shouldUpdateActivatedLicense() throws Exception {
        licenseStorage.persistActivatedLicense(ACTIVATED_LICENSE_TEXT);

        licenseStorage.persistActivatedLicense(NEW_ACTIVATED_LICENSE_TEXT);

        assertTrue(Files.exists(activatedLicenseFile));
        assertEquals(NEW_ACTIVATED_LICENSE_TEXT.getBytes(), Files.readAllBytes(activatedLicenseFile));
    }

    @Test
    public void shouldInitializeStorageAndPersistActivatedLicense() throws Exception {
        IoUtil.deleteRecursive(testDirectory.toFile());

        licenseStorage.persistActivatedLicense(ACTIVATED_LICENSE_TEXT);

        assertTrue(Files.exists(activatedLicenseFile));
        assertEquals(ACTIVATED_LICENSE_TEXT.getBytes(), Files.readAllBytes(activatedLicenseFile));
    }

    @Test
    public void shouldCleanStorage() throws Exception {
        licenseStorage.persistLicense(LICENSE_TEXT);
        licenseStorage.persistActivatedLicense(ACTIVATED_LICENSE_TEXT);

        assertTrue(Files.exists(licenseFile));
        assertTrue(Files.exists(activatedLicenseFile));

        licenseStorage.clean();

        assertFalse(Files.exists(licenseFile));
        assertFalse(Files.exists(activatedLicenseFile));
    }

    @Test
    public void shouldLoadLicense() throws Exception {
        licenseStorage.persistLicense(LICENSE_TEXT);

        assertEquals(LICENSE_TEXT, licenseStorage.loadLicense());
    }

    @Test(expectedExceptions = SystemLicenseNotFoundException.class)
    public void shouldThrowLicenseNotFoundExceptionIfLicenseAbsent() throws Exception {
        licenseStorage.loadLicense();
    }

    @Test
    public void shouldLoadActivatedLicense() throws Exception {
        licenseStorage.persistActivatedLicense(ACTIVATED_LICENSE_TEXT);

        assertEquals(ACTIVATED_LICENSE_TEXT, licenseStorage.loadActivatedLicense());
    }

    @Test(expectedExceptions = SystemLicenseNotFoundException.class)
    public void shouldThrowLicenseNotFoundExceptionIfActivatedLicenseAbsent() throws Exception {
        licenseStorage.loadActivatedLicense();
    }
}
