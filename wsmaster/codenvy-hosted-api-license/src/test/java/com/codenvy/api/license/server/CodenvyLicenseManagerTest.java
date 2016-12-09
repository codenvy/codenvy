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

import com.codenvy.api.license.CodenvyLicense;
import com.codenvy.api.license.CodenvyLicenseFactory;
import com.codenvy.api.license.LicenseNotFoundException;
import com.codenvy.api.license.server.dao.CodenvyLicenseActionDao;
import com.codenvy.api.license.server.model.impl.CodenvyLicenseActionImpl;
import com.codenvy.api.license.server.model.impl.FairSourceLicenseAcceptanceImpl;
import com.codenvy.swarm.client.SwarmDockerConnector;
import com.codenvy.swarm.client.model.DockerNode;
import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

import static com.codenvy.api.license.CodenvyLicense.MAX_NUMBER_OF_FREE_SERVERS;
import static com.codenvy.api.license.CodenvyLicense.MAX_NUMBER_OF_FREE_USERS;
import static com.codenvy.api.license.model.Constants.Action.ACCEPTED;
import static com.codenvy.api.license.model.Constants.Action.EXPIRED;
import static com.codenvy.api.license.model.Constants.License.FAIR_SOURCE_LICENSE;
import static com.codenvy.api.license.model.Constants.License.PRODUCT_LICENSE;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatoliy Bazko
 * @author Alexander Andrienko
 * @author Dmytro Nochevnov
 */
@Listeners(value = {MockitoTestNGListener.class})
public class CodenvyLicenseManagerTest {

    private static final String LICENSE_TEXT               = "# (id: 1)\nlicense text";
    private static final String NEW_LICENSE_TEXT           = "# (id: 2)\nnew license text";
    private static final String LICENSE_STORAGE_PREFIX_DIR = "licenseStorage-";
    private static final String LICENSE                    = "license";
    private static final String LICENSE_QUALIFIER          = "1";
    private static final long   USER_NUMBER                = 3;
    private static final int    NODES_NUMBER               = 2;

    @Mock
    private CodenvyLicense           mockCodenvyLicense;
    @Mock
    private CodenvyLicenseFactory    licenseFactory;
    @Mock
    private CodenvyLicense           codenvyLicense;
    @Mock
    private CodenvyLicense           newCodenvyLicense;
    @Mock
    private SwarmDockerConnector     swarmDockerConnector;
    @Mock
    private UserManager              userManager;
    @Mock
    private List<DockerNode>         dockerNodes;
    @Mock
    private CodenvyLicenseActionDao  codenvyLicenseActionDao;
    @Mock
    private CodenvyLicenseActionImpl codenvyLicenseAction;

    private File testDirectory;
    private File licenseFile;

    private CodenvyLicenseManager codenvyLicenseManager;

    @BeforeMethod
    public void setUp() throws Exception {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(resource);

        File targetDir = new File(resource.getPath()).getParentFile();
        testDirectory = new File(targetDir, NameGenerator.generate(LICENSE_STORAGE_PREFIX_DIR, 4));
        licenseFile = new File(testDirectory, LICENSE);
        Files.createDirectories(testDirectory.toPath());

        when(licenseFactory.create(LICENSE_TEXT)).thenReturn(codenvyLicense);
        when(licenseFactory.create(NEW_LICENSE_TEXT)).thenReturn(newCodenvyLicense);
        when(codenvyLicense.getLicenseText()).thenReturn(LICENSE_TEXT);
        when(newCodenvyLicense.getLicenseText()).thenReturn(NEW_LICENSE_TEXT);
        when(userManager.getTotalCount()).thenReturn(USER_NUMBER);

        when(codenvyLicenseAction.getLicenseQualifier()).thenReturn(LICENSE_QUALIFIER);
        when(codenvyLicenseActionDao.getByLicenseAndAction(PRODUCT_LICENSE, ACCEPTED))
                .thenThrow(new NotFoundException("Codenvy license action not found"))
                .thenReturn(codenvyLicenseAction);

        setSizeOfAdditionalNodes(NODES_NUMBER);

        codenvyLicenseManager = spy(new CodenvyLicenseManager(licenseFile.getAbsolutePath(),
                                                              licenseFactory,
                                                              userManager,
                                                              swarmDockerConnector,
                                                              codenvyLicenseActionDao));
    }

    @AfterMethod
    public void tearDown() throws Exception {
        IoUtil.deleteRecursive(testDirectory);
    }

    /**
     * Use case:
     *  - user adds license
     * Verify:
     *  - license is stored in the file
     *  - license action is stored in the DB
     */
    @Test
    public void shouldStoreLicense() throws Exception {
        codenvyLicenseManager.store(LICENSE_TEXT);

        ArgumentCaptor<CodenvyLicenseActionImpl> actionCaptor = ArgumentCaptor.forClass(CodenvyLicenseActionImpl.class);
        verify(codenvyLicenseActionDao).store(actionCaptor.capture());
        CodenvyLicenseActionImpl action = actionCaptor.getValue();
        assertEquals(action.getLicenseType(), PRODUCT_LICENSE);
        assertEquals(action.getActionType(), ACCEPTED);
        assertFalse(isNullOrEmpty(action.getLicenseQualifier()));

        verify(codenvyLicense).getLicenseText();
        assertTrue(Files.exists(licenseFile.toPath()));
        assertEquals(LICENSE_TEXT, readFileToString(licenseFile, UTF_8));
    }

    /**
     * Use case:
     *  - user adds the same license twice
     * Verify:
     *  - license action is stored in the DB only once
     */
    @Test
    public void shouldStoreSameLicenseTwice() throws Exception {
        codenvyLicenseManager.store(LICENSE_TEXT);
        codenvyLicenseManager.store(LICENSE_TEXT);

        verify(codenvyLicenseActionDao, times(1)).store(any(CodenvyLicenseActionImpl.class));
    }

    /**
     * Use case:
     *  - user adds license
     *  - user deletes license
     *  - user adds license
     * Verify:
     *  - license {@link com.codenvy.api.license.model.Constants.Action#EXPIRED} action is removed from the DB
     *  - license {@link com.codenvy.api.license.model.Constants.Action#ACCEPTED} action is stored in the DB
     */
    @Test
    public void shouldStoreLicenseAfterDeletion() throws Exception {
        when(codenvyLicenseActionDao.getByLicenseAndAction(PRODUCT_LICENSE, EXPIRED))
                .thenThrow(new NotFoundException("License action not found"))
                .thenReturn(mock(CodenvyLicenseActionImpl.class));

        codenvyLicenseManager.store(LICENSE_TEXT);

        verify(codenvyLicenseActionDao, times(1)).store(any(CodenvyLicenseActionImpl.class));

        codenvyLicenseManager.delete();

        verify(codenvyLicenseActionDao, times(1)).remove(eq(PRODUCT_LICENSE), eq(EXPIRED));
        ArgumentCaptor<CodenvyLicenseActionImpl> actionCaptor = ArgumentCaptor.forClass(CodenvyLicenseActionImpl.class);
        verify(codenvyLicenseActionDao, times(2)).store(actionCaptor.capture());
        CodenvyLicenseActionImpl value = actionCaptor.getAllValues().get(1);
        assertEquals(value.getLicenseType(), PRODUCT_LICENSE);
        assertEquals(value.getActionType(), EXPIRED);
        assertEquals(value.getLicenseQualifier(), LICENSE_QUALIFIER);

        when(codenvyLicenseActionDao
                     .getByLicenseAndAction(PRODUCT_LICENSE, ACCEPTED)).thenThrow(new NotFoundException("License action not found"));

        codenvyLicenseManager.store(LICENSE_TEXT);
        verify(codenvyLicenseActionDao, times(1)).remove(eq(PRODUCT_LICENSE), eq(ACCEPTED));
        verify(codenvyLicenseActionDao, times(2)).remove(eq(PRODUCT_LICENSE), eq(EXPIRED));
        actionCaptor = ArgumentCaptor.forClass(CodenvyLicenseActionImpl.class);
        verify(codenvyLicenseActionDao, times(3)).store(actionCaptor.capture());
        value = actionCaptor.getAllValues().get(2);
        assertEquals(value.getLicenseType(), PRODUCT_LICENSE);
        assertEquals(value.getActionType(), ACCEPTED);
        assertEquals(value.getLicenseQualifier(), LICENSE_QUALIFIER);
    }


    /**
     * Use case:
     *  - user stores license
     *  - user stores a new license
     * Verify:
     *  - a new license replaces an old one in the file
     *  - new license action replaces an old one in the DB
     */
    @Test
    public void shouldUpdateLicense() throws Exception {
        codenvyLicenseManager.store(LICENSE_TEXT);

        Mockito.reset(codenvyLicenseActionDao);
        when(codenvyLicenseActionDao.getByLicenseAndAction(PRODUCT_LICENSE, ACCEPTED))
                .thenReturn(codenvyLicenseAction);
        when(codenvyLicenseActionDao.getByLicenseAndAction(PRODUCT_LICENSE, EXPIRED))
                .thenThrow(new NotFoundException("License action not found"));

        codenvyLicenseManager.store(NEW_LICENSE_TEXT);

        assertEquals(NEW_LICENSE_TEXT, readFileToString(licenseFile, UTF_8));
        assertTrue(Files.exists(licenseFile.toPath()));

        verify(codenvyLicenseActionDao).remove(PRODUCT_LICENSE, ACCEPTED);
        verify(codenvyLicenseActionDao).remove(PRODUCT_LICENSE, EXPIRED);
        verify(codenvyLicenseActionDao).store(any(CodenvyLicenseActionImpl.class));
    }

    /**
     * Use case:
     *  - user stores license
     *  - user removes license
     * Verify:
     *  - license file is absent
     *  - license {@link com.codenvy.api.license.model.Constants.Action#ACCEPTED} action isn't removed in the DB
     *  - license {@link com.codenvy.api.license.model.Constants.Action#EXPIRED} action is added in the DB
     */
    @Test
    public void shouldRemoveLicense() throws Exception {
        codenvyLicenseManager.store(LICENSE_TEXT);

        Mockito.reset(codenvyLicenseActionDao);
        codenvyLicenseManager.delete();

        ArgumentCaptor<CodenvyLicenseActionImpl> actionCaptor = ArgumentCaptor.forClass(CodenvyLicenseActionImpl.class);
        verify(codenvyLicenseActionDao, times(1)).store(actionCaptor.capture());
        CodenvyLicenseActionImpl expireAction = actionCaptor.getValue();
        assertEquals(expireAction.getLicenseType(), PRODUCT_LICENSE);
        assertEquals(expireAction.getActionType(), EXPIRED);
        assertEquals(expireAction.getLicenseQualifier(), LICENSE_QUALIFIER);

        assertFalse(Files.exists(licenseFile.toPath()));
    }

    /**
     * Use case:
     *  - no license in the system
     *  - user tries to delete it
     * Verify:
     *  - license file is absent
     *  - no interaction with the DB
     */
    @Test(expectedExceptions = LicenseNotFoundException.class)
    public void shouldNotRemoveLicenseIfAbsent() throws Exception {
        IoUtil.deleteRecursive(testDirectory);

        codenvyLicenseManager.delete();

        verify(codenvyLicenseActionDao, never()).remove(eq(PRODUCT_LICENSE), eq(EXPIRED));
        verify(codenvyLicenseActionDao, never()).store(any());
    }

    @Test
    public void shouldAcceptFairSourceLicense() throws Exception {
        when(codenvyLicenseActionDao.getByLicenseAndAction(eq(FAIR_SOURCE_LICENSE), eq(ACCEPTED)))
                .thenThrow(new NotFoundException("Codenvy license not found"));

        FairSourceLicenseAcceptanceImpl fairSourceLicenseAcceptance = new FairSourceLicenseAcceptanceImpl("fn", "ln", "em@codenvy.com");
        codenvyLicenseManager.acceptFairSourceLicense(fairSourceLicenseAcceptance);

        ArgumentCaptor<CodenvyLicenseActionImpl> actionCaptor = ArgumentCaptor.forClass(CodenvyLicenseActionImpl.class);
        verify(codenvyLicenseActionDao).store(actionCaptor.capture());
        CodenvyLicenseActionImpl value = actionCaptor.getValue();
        assertEquals(value.getLicenseType(), FAIR_SOURCE_LICENSE);
        assertEquals(value.getActionType(), ACCEPTED);
        assertEquals(value.getAttributes(), ImmutableMap.of("firstName", "fn", "lastName", "ln", "email", "em@codenvy.com"));
        assertNull(value.getLicenseQualifier());
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldNotAcceptFairSourceLicenseTwice() throws Exception {
        when(codenvyLicenseActionDao.getByLicenseAndAction(eq(FAIR_SOURCE_LICENSE), eq(ACCEPTED)))
                .thenThrow(new NotFoundException("Codenvy license not found"))
                .thenReturn(any(CodenvyLicenseActionImpl.class));

        FairSourceLicenseAcceptanceImpl fairSourceLicenseAcceptance = new FairSourceLicenseAcceptanceImpl("fn", "ln", "em@codenvy.com");
        codenvyLicenseManager.acceptFairSourceLicense(fairSourceLicenseAcceptance);

        verify(codenvyLicenseActionDao, times(1)).store(any(CodenvyLicenseActionImpl.class));

        codenvyLicenseManager.acceptFairSourceLicense(fairSourceLicenseAcceptance);
        verify(codenvyLicenseActionDao, times(1)).store(any(CodenvyLicenseActionImpl.class));
        verify(codenvyLicenseActionDao, never()).remove(eq(FAIR_SOURCE_LICENSE), eq(ACCEPTED));
    }

    @Test(expectedExceptions = BadRequestException.class)
    public void shouldNotAcceptIfRequestInvalid() throws Exception {
        when(codenvyLicenseActionDao.getByLicenseAndAction(eq(FAIR_SOURCE_LICENSE), eq(ACCEPTED)))
                .thenThrow(new NotFoundException("Codenvy license not found"));

        IoUtil.deleteRecursive(testDirectory);
        FairSourceLicenseAcceptanceImpl fairSourceLicenseAcceptance = new FairSourceLicenseAcceptanceImpl("fn", "ln", null);

        codenvyLicenseManager.acceptFairSourceLicense(fairSourceLicenseAcceptance);
    }

    @Test
    public void testIfFairSourceLicenseNotAccepted() throws Exception {
        when(codenvyLicenseActionDao.getByLicenseAndAction(eq(FAIR_SOURCE_LICENSE), eq(ACCEPTED)))
                .thenThrow(new NotFoundException("Codenvy license not found"));

        assertFalse(codenvyLicenseManager.hasAcceptedFairSourceLicense());
    }

    @Test
    public void testIfFairSourceLicenseAccepted() throws Exception {
        when(codenvyLicenseActionDao.getByLicenseAndAction(eq(FAIR_SOURCE_LICENSE), eq(ACCEPTED)))
                .thenReturn(mock(CodenvyLicenseActionImpl.class));

        assertTrue(codenvyLicenseManager.hasAcceptedFairSourceLicense());
    }

    @Test
    public void shouldLoadLicense() throws Exception {
        codenvyLicenseManager.store(LICENSE_TEXT);

        CodenvyLicense license = codenvyLicenseManager.load();

        verify(licenseFactory, times(2)).create(LICENSE_TEXT);
        assertEquals(license, codenvyLicense);
    }

    @Test(expectedExceptions = LicenseNotFoundException.class)
    public void shouldThrowLicenseNotFoundExceptionIfLicenseIsAbsent() {
        codenvyLicenseManager.load();
    }

    @Test
    public void testIsCodenvyLicenseUsageLegal() throws IOException, ServerException {
        doReturn(mockCodenvyLicense).when(codenvyLicenseManager).load();
        doReturn(true).when(mockCodenvyLicense).isLicenseUsageLegal(USER_NUMBER, NODES_NUMBER);

        assertTrue(codenvyLicenseManager.isCodenvyUsageLegal());
    }

    @Test
    public void testIsCodenvyFreeUsageLegal() throws IOException, ServerException {
        when(userManager.getTotalCount()).thenReturn(MAX_NUMBER_OF_FREE_USERS);
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS);

        doThrow(LicenseNotFoundException.class).when(codenvyLicenseManager).load();

        assertTrue(codenvyLicenseManager.isCodenvyUsageLegal());
    }

    @Test
    public void testIsCodenvyLicenseUsageNotLegal() throws IOException, ServerException {
        doReturn(mockCodenvyLicense).when(codenvyLicenseManager).load();
        doReturn(false).when(mockCodenvyLicense).isLicenseUsageLegal(USER_NUMBER, NODES_NUMBER);

        assertFalse(codenvyLicenseManager.isCodenvyUsageLegal());
    }

    @Test
    public void testIsCodenvyFreeUsageNotLegal() throws IOException, ServerException {
        when(userManager.getTotalCount()).thenReturn(MAX_NUMBER_OF_FREE_USERS + 1);
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS + 1);

        doThrow(LicenseNotFoundException.class).when(codenvyLicenseManager).load();

        assertFalse(codenvyLicenseManager.isCodenvyUsageLegal());
    }

    @Test
    public void testIsCodenvyActualNodesUsageLegal() throws IOException, ServerException {
        doReturn(mockCodenvyLicense).when(codenvyLicenseManager).load();
        doReturn(true).when(mockCodenvyLicense).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertTrue(codenvyLicenseManager.isCodenvyNodesUsageLegal(null));
    }

    @Test
    public void testIsCodenvyGivenNodesUsageLegal() throws IOException, ServerException {
        doReturn(mockCodenvyLicense).when(codenvyLicenseManager).load();
        doReturn(true).when(mockCodenvyLicense).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertTrue(codenvyLicenseManager.isCodenvyNodesUsageLegal(NODES_NUMBER));
        verify(swarmDockerConnector, never()).getAvailableNodes();
    }

    @Test
    public void testIsCodenvyActualNodesUsageNotLegal() throws IOException, ServerException {
        doReturn(mockCodenvyLicense).when(codenvyLicenseManager).load();
        doReturn(false).when(mockCodenvyLicense).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertFalse(codenvyLicenseManager.isCodenvyNodesUsageLegal(null));
    }

    @Test
    public void testIsCodenvyGivenNodesUsageNotLegal() throws IOException, ServerException {
        doReturn(mockCodenvyLicense).when(codenvyLicenseManager).load();
        doReturn(false).when(mockCodenvyLicense).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertFalse(codenvyLicenseManager.isCodenvyNodesUsageLegal(NODES_NUMBER));
        verify(swarmDockerConnector, never()).getAvailableNodes();
    }

    @Test
    public void testIsCodenvyNodesFreeUsageLegal() throws IOException, ServerException {
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS);

        doThrow(LicenseNotFoundException.class).when(codenvyLicenseManager).load();

        assertTrue(codenvyLicenseManager.isCodenvyNodesUsageLegal(null));
    }

    @Test
    public void testIsCodenvyNodesFreeUsageNotLegal() throws IOException, ServerException {
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS + 1);

        doThrow(LicenseNotFoundException.class).when(codenvyLicenseManager).load();

        assertFalse(codenvyLicenseManager.isCodenvyNodesUsageLegal(null));
    }

    private void setSizeOfAdditionalNodes(int size) throws IOException {
        when(swarmDockerConnector.getAvailableNodes()).thenReturn(dockerNodes);
        when(dockerNodes.size()).thenReturn(size);
    }

}
