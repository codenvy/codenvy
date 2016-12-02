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
import com.codenvy.api.license.exception.LicenseNotFoundException;
import com.codenvy.api.license.server.dao.CodenvyLicenseActionDao;
import com.codenvy.api.license.server.model.impl.CodenvyLicenseActionImpl;
import com.codenvy.api.license.shared.dto.IssueDto;
import com.codenvy.api.license.shared.model.Issue;
import com.codenvy.swarm.client.SwarmDockerConnector;
import com.codenvy.swarm.client.model.DockerNode;
import com.google.common.collect.ImmutableList;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.UserManager;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static com.codenvy.api.license.CodenvyLicense.MAX_NUMBER_OF_FREE_SERVERS;
import static com.codenvy.api.license.CodenvyLicense.MAX_NUMBER_OF_FREE_USERS;
import static com.codenvy.api.license.shared.model.Constants.Action.ACCEPTED;
import static com.codenvy.api.license.shared.model.Constants.License.FAIR_SOURCE_LICENSE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatoliy Bazko
 * @author Alexander Andrienko
 * @author Dmytro Nochevnov
 */
@Listeners(value = {MockitoTestNGListener.class})
public class CodenvyLicenseManagerTest {

    private static final String ACTIVATED_LICENSE_TEXT = "# (id: 1)\nactivated license text";
    private static final String LICENSE_TEXT           = "# (id: 1)\nlicense text";
    private static final String NEW_LICENSE_TEXT       = "# (id: 2)\nnew license text";
    private static final String LICENSE_QUALIFIER      = "1";
    private static final long   USER_NUMBER            = 3;
    private static final int    NODES_NUMBER           = 2;

    @Mock
    private CodenvyLicense           codenvyLicense;
    @Mock
    private CodenvyLicense           newCodenvyLicense;
    @Mock
    private CodenvyLicenseFactory    licenseFactory;
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
    @Mock
    private CodenvyLicenseStorage    codenvyLicenseStorage;
    @Mock
    private CodenvyLicenseActivator  codenvyLicenseActivator;

    private CodenvyLicenseManager codenvyLicenseManager;

    @BeforeMethod
    public void setUp() throws Exception {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(resource);

        when(licenseFactory.create(LICENSE_TEXT)).thenReturn(codenvyLicense);
        when(licenseFactory.create(NEW_LICENSE_TEXT)).thenReturn(newCodenvyLicense);
        when(codenvyLicense.getLicenseText()).thenReturn(LICENSE_TEXT);
        when(codenvyLicense.getLicenseId()).thenReturn("1");
        when(newCodenvyLicense.getLicenseId()).thenReturn("2");
        when(newCodenvyLicense.getLicenseText()).thenReturn(NEW_LICENSE_TEXT);
        when(userManager.getTotalCount()).thenReturn(USER_NUMBER);
        when(codenvyLicenseAction.getLicenseQualifier()).thenReturn(LICENSE_QUALIFIER);

        setSizeOfAdditionalNodes(NODES_NUMBER);

        codenvyLicenseManager = spy(new CodenvyLicenseManager(licenseFactory,
                                                          userManager,
                                                          swarmDockerConnector,
                                                          codenvyLicenseActionDao,
                                                          codenvyLicenseStorage,
                                                          codenvyLicenseActivator));
    }

    @Test
    public void shouldActivateAndPersistLicense() throws Exception {
        when(codenvyLicenseActivator.activateIfRequired(codenvyLicense)).thenReturn(ACTIVATED_LICENSE_TEXT);

        codenvyLicenseManager.store(LICENSE_TEXT);

        verify(codenvyLicenseActivator).activateIfRequired(codenvyLicense);
        verify(codenvyLicenseStorage).persistLicense(LICENSE_TEXT);
        verify(codenvyLicenseStorage).persistActivatedLicense(ACTIVATED_LICENSE_TEXT);
    }

    @Test
    public void shouldDeleteLicense() throws Exception {
        when(codenvyLicenseStorage.loadLicense()).thenReturn(LICENSE_TEXT);

        codenvyLicenseManager.delete();

        verify(codenvyLicenseStorage).clean();
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
    public void shouldLoadLicenseAndValidateActivation() throws Exception {
        when(codenvyLicenseStorage.loadLicense()).thenReturn(LICENSE_TEXT);

        CodenvyLicense license = codenvyLicenseManager.load();

        verify(codenvyLicenseActivator).validateActivation(codenvyLicense);
        verify(codenvyLicenseStorage).loadLicense();
        verify(licenseFactory).create(LICENSE_TEXT);
        assertEquals(license, codenvyLicense);
    }

    @Test
    public void testIsCodenvyLicenseUsageLegal() throws IOException, ServerException {
        doReturn(codenvyLicense).when(codenvyLicenseManager).load();
        doReturn(true).when(codenvyLicense).isLicenseUsageLegal(USER_NUMBER, NODES_NUMBER);

        assertTrue(codenvyLicenseManager.isSystemUsageLegal());
    }

    @Test
    public void testIsCodenvyFreeUsageLegal() throws IOException, ServerException {
        when(userManager.getTotalCount()).thenReturn(MAX_NUMBER_OF_FREE_USERS);
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS);

        doThrow(LicenseNotFoundException.class).when(codenvyLicenseManager).load();

        assertTrue(codenvyLicenseManager.isSystemUsageLegal());
    }

    @Test
    public void testIsCodenvyLicenseUsageNotLegal() throws IOException, ServerException {
        doReturn(codenvyLicense).when(codenvyLicenseManager).load();
        doReturn(false).when(codenvyLicense).isLicenseUsageLegal(USER_NUMBER, NODES_NUMBER);

        assertFalse(codenvyLicenseManager.isSystemUsageLegal());
    }

    @Test
    public void testIsCodenvyFreeUsageNotLegal() throws IOException, ServerException {
        when(userManager.getTotalCount()).thenReturn(MAX_NUMBER_OF_FREE_USERS + 1);
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS + 1);

        doThrow(LicenseNotFoundException.class).when(codenvyLicenseManager).load();

        assertFalse(codenvyLicenseManager.isSystemUsageLegal());
    }

    @Test
    public void testIsCodenvyActualNodesUsageLegal() throws IOException, ServerException {
        doReturn(codenvyLicense).when(codenvyLicenseManager).load();
        doReturn(true).when(codenvyLicense).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertTrue(codenvyLicenseManager.isSystemNodesUsageLegal(null));
    }

    @Test
    public void testIsCodenvyGivenNodesUsageLegal() throws IOException, ServerException {
        doReturn(codenvyLicense).when(codenvyLicenseManager).load();
        doReturn(true).when(codenvyLicense).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertTrue(codenvyLicenseManager.isSystemNodesUsageLegal(NODES_NUMBER));
        verify(swarmDockerConnector, never()).getAvailableNodes();
    }

    @Test
    public void testIsCodenvyActualNodesUsageNotLegal() throws IOException, ServerException {
        doReturn(codenvyLicense).when(codenvyLicenseManager).load();
        doReturn(false).when(codenvyLicense).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertFalse(codenvyLicenseManager.isSystemNodesUsageLegal(null));
    }

    @Test
    public void testIsCodenvyGivenNodesUsageNotLegal() throws IOException, ServerException {
        doReturn(codenvyLicense).when(codenvyLicenseManager).load();
        doReturn(false).when(codenvyLicense).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertFalse(codenvyLicenseManager.isSystemNodesUsageLegal(NODES_NUMBER));
        verify(swarmDockerConnector, never()).getAvailableNodes();
    }

    @Test
    public void testIsCodenvyNodesFreeUsageLegal() throws IOException, ServerException {
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS);

        doThrow(LicenseNotFoundException.class).when(codenvyLicenseManager).load();

        assertTrue(codenvyLicenseManager.isSystemNodesUsageLegal(null));
    }

    @Test
    public void testIsCodenvyNodesFreeUsageNotLegal() throws IOException, ServerException {
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS + 1);

        doThrow(LicenseNotFoundException.class).when(codenvyLicenseManager).load();

        assertFalse(codenvyLicenseManager.isSystemNodesUsageLegal(null));
    }

    @Test
    public void shouldConfirmThatUserCanBeAddedDueToLicense() throws ServerException {
        when(userManager.getTotalCount()).thenReturn(USER_NUMBER);
        doReturn(codenvyLicense).when(codenvyLicenseManager).load();
        doReturn(true).when(codenvyLicense).isLicenseUsageLegal(USER_NUMBER + 1, 0);

        assertTrue(codenvyLicenseManager.canUserBeAdded());
    }

    @Test
    public void shouldConfirmThatUserCanBeAddedDueToFreeUsageTerms() throws ServerException {
        when(userManager.getTotalCount()).thenReturn(MAX_NUMBER_OF_FREE_USERS - 1);
        doThrow(LicenseNotFoundException.class).when(codenvyLicenseManager).load();

        assertTrue(codenvyLicenseManager.canUserBeAdded());
    }

    @Test
    public void shouldDisproveThatUserCanBeAddedDueToLicense() throws ServerException {
        when(userManager.getTotalCount()).thenReturn(USER_NUMBER);
        doReturn(codenvyLicense).when(codenvyLicenseManager).load();
        doReturn(false).when(codenvyLicense).isLicenseUsageLegal(USER_NUMBER + 1, 0);

        assertFalse(codenvyLicenseManager.canUserBeAdded());
    }

    @Test
    public void shouldDisproveThatUserCanBeAddedDueToFreeUsageTerms() throws ServerException {
        when(userManager.getTotalCount()).thenReturn(MAX_NUMBER_OF_FREE_USERS);

        doThrow(LicenseNotFoundException.class).when(codenvyLicenseManager).load();

        assertFalse(codenvyLicenseManager.canUserBeAdded());
    }

    @Test
    public void shouldReturnAllowedUserNumberDueToLicense() {
        doReturn(codenvyLicense).when(codenvyLicenseManager).load();
        doReturn((int)USER_NUMBER).when(codenvyLicense).getNumberOfUsers();

        assertEquals(codenvyLicenseManager.getAllowedUserNumber(), USER_NUMBER);
    }

    @Test
    public void shouldReturnAllowedUserNumberDueToFreeUsageTerms() {
        doThrow(LicenseNotFoundException.class).when(codenvyLicenseManager).load();
        assertEquals(codenvyLicenseManager.getAllowedUserNumber(), MAX_NUMBER_OF_FREE_USERS);
    }

    @Test
    public void shouldReturnListOfIssues() throws ServerException {
        doReturn(false).when(codenvyLicenseManager).canUserBeAdded();
        assertEquals(codenvyLicenseManager.getLicenseIssues(),
                     ImmutableList.of(newDto(IssueDto.class).withStatus(Issue.Status.USER_LICENSE_HAS_REACHED_ITS_LIMIT)
                                                            .withMessage(CodenvyLicenseManager.LICENSE_HAS_REACHED_ITS_USER_LIMIT_MESSAGE)));
    }

    @Test
    public void shouldReturnEmptyListOfIssues() throws ServerException {
        doReturn(true).when(codenvyLicenseManager).canUserBeAdded();
        assertEquals(codenvyLicenseManager.getLicenseIssues(), ImmutableList.of());
    }

    private void setSizeOfAdditionalNodes(int size) throws IOException {
        when(swarmDockerConnector.getAvailableNodes()).thenReturn(dockerNodes);
        when(dockerNodes.size()).thenReturn(size);
    }

}
