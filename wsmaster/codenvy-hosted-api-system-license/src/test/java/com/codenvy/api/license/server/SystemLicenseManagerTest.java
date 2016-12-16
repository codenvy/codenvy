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

import com.codenvy.api.license.SystemLicense;
import com.codenvy.api.license.SystemLicenseFactory;
import com.codenvy.api.license.exception.SystemLicenseNotFoundException;
import com.codenvy.api.license.server.dao.SystemLicenseActionDao;
import com.codenvy.api.license.server.model.impl.SystemLicenseActionImpl;
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

import static com.codenvy.api.license.SystemLicense.MAX_NUMBER_OF_FREE_SERVERS;
import static com.codenvy.api.license.SystemLicense.MAX_NUMBER_OF_FREE_USERS;
import static com.codenvy.api.license.shared.model.Constants.Action.ACCEPTED;
import static com.codenvy.api.license.shared.model.Constants.PaidLicense.FAIR_SOURCE_LICENSE;
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
public class SystemLicenseManagerTest {

    private static final String ACTIVATED_LICENSE_TEXT = "# (id: 1)\nactivated license text";
    private static final String LICENSE_TEXT           = "# (id: 1)\nlicense text";
    private static final String NEW_LICENSE_TEXT       = "# (id: 2)\nnew license text";
    private static final String LICENSE_ID             = "1";
    private static final long   USER_NUMBER            = 3;
    private static final int    NODES_NUMBER           = 2;

    @Mock
    private SystemLicense           systemLicense;
    @Mock
    private SystemLicense           newSystemLicense;
    @Mock
    private SystemLicenseFactory    licenseFactory;
    @Mock
    private SwarmDockerConnector    swarmDockerConnector;
    @Mock
    private UserManager             userManager;
    @Mock
    private List<DockerNode>        dockerNodes;
    @Mock
    private SystemLicenseActionDao  systemLicenseActionDao;
    @Mock
    private SystemLicenseActionImpl systemLicenseAction;
    @Mock
    private SystemLicenseStorage    systemLicenseStorage;
    @Mock
    private SystemLicenseActivator  systemLicenseActivator;

    private SystemLicenseManager systemLicenseManager;

    @BeforeMethod
    public void setUp() throws Exception {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(resource);

        when(licenseFactory.create(LICENSE_TEXT)).thenReturn(systemLicense);
        when(licenseFactory.create(NEW_LICENSE_TEXT)).thenReturn(newSystemLicense);
        when(systemLicense.getLicenseText()).thenReturn(LICENSE_TEXT);
        when(systemLicense.getLicenseId()).thenReturn(LICENSE_ID);
        when(newSystemLicense.getLicenseId()).thenReturn("2");
        when(newSystemLicense.getLicenseText()).thenReturn(NEW_LICENSE_TEXT);
        when(userManager.getTotalCount()).thenReturn(USER_NUMBER);
        when(systemLicenseAction.getLicenseId()).thenReturn(LICENSE_ID);

        setSizeOfAdditionalNodes(NODES_NUMBER);

        systemLicenseManager = spy(new SystemLicenseManager(licenseFactory,
                                                            userManager,
                                                            swarmDockerConnector,
                                                            systemLicenseActionDao,
                                                            systemLicenseStorage,
                                                            systemLicenseActivator));
    }

    @Test
    public void shouldActivateAndPersistLicense() throws Exception {
        when(systemLicenseActivator.activateIfRequired(systemLicense)).thenReturn(ACTIVATED_LICENSE_TEXT);

        systemLicenseManager.store(LICENSE_TEXT);

        verify(systemLicenseActivator).activateIfRequired(systemLicense);
        verify(systemLicenseStorage).persistLicense(LICENSE_TEXT);
        verify(systemLicenseStorage).persistActivatedLicense(ACTIVATED_LICENSE_TEXT);
    }

    @Test
    public void shouldDeleteLicense() throws Exception {
        when(systemLicenseStorage.loadLicense()).thenReturn(LICENSE_TEXT);

        systemLicenseManager.delete();

        verify(systemLicenseStorage).clean();
    }

    @Test
    public void testIfFairSourceLicenseIsNotAccepted() throws Exception {
        when(systemLicenseActionDao.getByLicenseTypeAndAction(eq(FAIR_SOURCE_LICENSE), eq(ACCEPTED)))
            .thenThrow(new NotFoundException("System license not found"));

        assertFalse(systemLicenseManager.isFairSourceLicenseAccepted());
    }

    @Test
    public void testIfFairSourceLicenseIsAccepted() throws Exception {
        when(systemLicenseActionDao.getByLicenseTypeAndAction(eq(FAIR_SOURCE_LICENSE), eq(ACCEPTED)))
            .thenReturn(mock(SystemLicenseActionImpl.class));

        assertTrue(systemLicenseManager.isFairSourceLicenseAccepted());
    }

    @Test
    public void shouldLoadLicenseAndValidateActivation() throws Exception {
        when(systemLicenseStorage.loadLicense()).thenReturn(LICENSE_TEXT);

        SystemLicense license = systemLicenseManager.load();

        verify(systemLicenseActivator).validateActivation(systemLicense);
        verify(systemLicenseStorage).loadLicense();
        verify(licenseFactory).create(LICENSE_TEXT);
        assertEquals(license, systemLicense);
    }

    @Test
    public void testIsSystemLicenseUsageLegal() throws IOException, ServerException {
        doReturn(systemLicense).when(systemLicenseManager).load();
        doReturn(true).when(systemLicense).isLicenseUsageLegal(USER_NUMBER, NODES_NUMBER);

        assertTrue(systemLicenseManager.isSystemUsageLegal());
    }

    @Test
    public void testIsSystemFreeUsageLegal() throws IOException, ServerException {
        when(userManager.getTotalCount()).thenReturn(MAX_NUMBER_OF_FREE_USERS);
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS);

        doThrow(SystemLicenseNotFoundException.class).when(systemLicenseManager).load();

        assertTrue(systemLicenseManager.isSystemUsageLegal());
    }

    @Test
    public void testIsSystemLicenseUsageNotLegal() throws IOException, ServerException {
        doReturn(systemLicense).when(systemLicenseManager).load();
        doReturn(false).when(systemLicense).isLicenseUsageLegal(USER_NUMBER, NODES_NUMBER);

        assertFalse(systemLicenseManager.isSystemUsageLegal());
    }

    @Test
    public void testIsCodenvyFreeUsageNotLegal() throws IOException, ServerException {
        when(userManager.getTotalCount()).thenReturn(MAX_NUMBER_OF_FREE_USERS + 1);
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS + 1);

        doThrow(SystemLicenseNotFoundException.class).when(systemLicenseManager).load();

        assertFalse(systemLicenseManager.isSystemUsageLegal());
    }

    @Test
    public void testIsCodenvyActualNodesUsageLegal() throws IOException, ServerException {
        doReturn(systemLicense).when(systemLicenseManager).load();
        doReturn(true).when(systemLicense).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertTrue(systemLicenseManager.isSystemNodesUsageLegal(null));
    }

    @Test
    public void testIsCodenvyGivenNodesUsageLegal() throws IOException, ServerException {
        doReturn(systemLicense).when(systemLicenseManager).load();
        doReturn(true).when(systemLicense).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertTrue(systemLicenseManager.isSystemNodesUsageLegal(NODES_NUMBER));
        verify(swarmDockerConnector, never()).getAvailableNodes();
    }

    @Test
    public void testIsCodenvyActualNodesUsageNotLegal() throws IOException, ServerException {
        doReturn(systemLicense).when(systemLicenseManager).load();
        doReturn(false).when(systemLicense).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertFalse(systemLicenseManager.isSystemNodesUsageLegal(null));
    }

    @Test
    public void testIsCodenvyGivenNodesUsageNotLegal() throws IOException, ServerException {
        doReturn(systemLicense).when(systemLicenseManager).load();
        doReturn(false).when(systemLicense).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertFalse(systemLicenseManager.isSystemNodesUsageLegal(NODES_NUMBER));
        verify(swarmDockerConnector, never()).getAvailableNodes();
    }

    @Test
    public void testIsCodenvyNodesFreeUsageLegal() throws IOException, ServerException {
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS);

        doThrow(SystemLicenseNotFoundException.class).when(systemLicenseManager).load();

        assertTrue(systemLicenseManager.isSystemNodesUsageLegal(null));
    }

    @Test
    public void testIsCodenvyNodesFreeUsageNotLegal() throws IOException, ServerException {
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS + 1);

        doThrow(SystemLicenseNotFoundException.class).when(systemLicenseManager).load();

        assertFalse(systemLicenseManager.isSystemNodesUsageLegal(null));
    }

    @Test
    public void shouldConfirmThatUserCanBeAddedDueToLicense() throws ServerException {
        when(userManager.getTotalCount()).thenReturn(USER_NUMBER);
        doReturn(systemLicense).when(systemLicenseManager).load();
        doReturn(true).when(systemLicense).isLicenseUsageLegal(USER_NUMBER + 1, 0);

        assertTrue(systemLicenseManager.canUserBeAdded());
    }

    @Test
    public void shouldConfirmThatUserCanBeAddedDueToFreeUsageTerms() throws ServerException {
        when(userManager.getTotalCount()).thenReturn(MAX_NUMBER_OF_FREE_USERS - 1);
        doThrow(SystemLicenseNotFoundException.class).when(systemLicenseManager).load();

        assertTrue(systemLicenseManager.canUserBeAdded());
    }

    @Test
    public void shouldDisproveThatUserCanBeAddedDueToLicense() throws ServerException {
        when(userManager.getTotalCount()).thenReturn(USER_NUMBER);
        doReturn(systemLicense).when(systemLicenseManager).load();
        doReturn(false).when(systemLicense).isLicenseUsageLegal(USER_NUMBER + 1, 0);

        assertFalse(systemLicenseManager.canUserBeAdded());
    }

    @Test
    public void shouldDisproveThatUserCanBeAddedDueToFreeUsageTerms() throws ServerException {
        when(userManager.getTotalCount()).thenReturn(MAX_NUMBER_OF_FREE_USERS);

        doThrow(SystemLicenseNotFoundException.class).when(systemLicenseManager).load();

        assertFalse(systemLicenseManager.canUserBeAdded());
    }

    @Test
    public void shouldReturnAllowedUserNumberDueToLicense() {
        doReturn(systemLicense).when(systemLicenseManager).load();
        doReturn((int)USER_NUMBER).when(systemLicense).getNumberOfUsers();

        assertEquals(systemLicenseManager.getAllowedUserNumber(), USER_NUMBER);
    }

    @Test
    public void shouldReturnAllowedUserNumberDueToFreeUsageTerms() {
        doThrow(SystemLicenseNotFoundException.class).when(systemLicenseManager).load();
        assertEquals(systemLicenseManager.getAllowedUserNumber(), MAX_NUMBER_OF_FREE_USERS);
    }

    @Test
    public void shouldReturnListOfIssuesWithExpiring() throws Exception {
        doReturn(false).when(systemLicenseManager).canUserBeAdded();
        doReturn(false).when(systemLicenseManager).isFairSourceLicenseAccepted();
        doReturn(true).when(systemLicenseManager).isPaidLicenseExpiring();
        doReturn("License expiring").when(systemLicenseManager).getMessageForLicenseExpiring();
        assertEquals(systemLicenseManager.getLicenseIssues(),
                     ImmutableList.of(newDto(IssueDto.class).withStatus(Issue.Status.USER_LICENSE_HAS_REACHED_ITS_LIMIT)
                                                            .withMessage(SystemLicenseManager.LICENSE_HAS_REACHED_ITS_USER_LIMIT_MESSAGE_FOR_REGISTRATION),
                                      newDto(IssueDto.class).withStatus(Issue.Status.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED)
                                                            .withMessage(SystemLicenseManager.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_MESSAGE),
                                      newDto(IssueDto.class).withStatus(Issue.Status.LICENSE_EXPIRING)
                                                            .withMessage("License expiring")));
    }

    @Test
    public void shouldReturnListOfIssuesWithExpired() throws Exception {
        doReturn(false).when(systemLicenseManager).canUserBeAdded();
        doReturn(false).when(systemLicenseManager).isFairSourceLicenseAccepted();
        doReturn(false).when(systemLicenseManager).isPaidLicenseExpiring();
        doReturn(true).when(systemLicenseManager).isPaidLicenseExpired();
        doReturn(false).when(systemLicenseManager).isSystemUsageLegal();
        doReturn("License expired").when(systemLicenseManager).getMessageForLicenseExpired();
        assertEquals(systemLicenseManager.getLicenseIssues(),
                     ImmutableList.of(newDto(IssueDto.class).withStatus(Issue.Status.USER_LICENSE_HAS_REACHED_ITS_LIMIT)
                                                            .withMessage(SystemLicenseManager.LICENSE_HAS_REACHED_ITS_USER_LIMIT_MESSAGE_FOR_REGISTRATION),
                                      newDto(IssueDto.class).withStatus(Issue.Status.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED)
                                                            .withMessage(SystemLicenseManager.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_MESSAGE),
                                      newDto(IssueDto.class).withStatus(Issue.Status.LICENSE_EXPIRED)
                                                            .withMessage("License expired")));
    }

    @Test
    public void shouldReturnEmptyListOfIssues() throws Exception {
        doReturn(true).when(systemLicenseManager).canUserBeAdded();
        doReturn(true).when(systemLicenseManager).isFairSourceLicenseAccepted();
        doReturn(false).when(systemLicenseManager).isPaidLicenseExpiring();
        doReturn(false).when(systemLicenseManager).isPaidLicenseExpired();
        assertEquals(systemLicenseManager.getLicenseIssues(), ImmutableList.of());
    }

    private void setSizeOfAdditionalNodes(int size) throws IOException {
        when(swarmDockerConnector.getAvailableNodes()).thenReturn(dockerNodes);
        when(dockerNodes.size()).thenReturn(size);
    }

}
