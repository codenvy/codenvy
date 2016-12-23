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
import com.codenvy.api.license.exception.SystemLicenseException;
import com.codenvy.api.license.exception.SystemLicenseNotFoundException;
import com.codenvy.api.license.server.dao.SystemLicenseActionDao;
import com.codenvy.api.license.server.model.impl.SystemLicenseActionImpl;
import com.codenvy.api.license.shared.dto.IssueDto;
import com.codenvy.api.license.shared.model.Issue;
import com.codenvy.api.permission.server.SystemDomain;
import com.codenvy.swarm.client.SwarmDockerConnector;
import com.codenvy.swarm.client.model.DockerNode;
import com.google.common.collect.ImmutableList;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static com.codenvy.api.license.SystemLicense.MAX_NUMBER_OF_FREE_SERVERS;
import static com.codenvy.api.license.SystemLicense.MAX_NUMBER_OF_FREE_USERS;
import static com.codenvy.api.license.shared.model.Constants.Action.ACCEPTED;
import static com.codenvy.api.license.shared.model.Constants.Action.EXPIRED;
import static com.codenvy.api.license.shared.model.Constants.PaidLicense.FAIR_SOURCE_LICENSE;
import static com.codenvy.api.license.shared.model.Constants.PaidLicense.PRODUCT_LICENSE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Matchers.any;
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
    private static final long   USER_NUMBER            = 4;
    private static final int    NODES_NUMBER           = 2;

    @Mock
    private SystemLicense                           license;
    @Mock
    private SystemLicense                           newSystemLicense;
    @Mock
    private SystemLicenseFactory                    licenseFactory;
    @Mock
    private SwarmDockerConnector                    swarmDockerConnector;
    @Mock
    private UserManager                             userManager;
    @Mock
    private List<DockerNode>                        dockerNodes;
    @Mock
    private SystemLicenseActionDao                  systemLicenseActionDao;
    @Mock
    private SystemLicenseActionImpl                 systemLicenseAction;
    @Mock
    private SystemLicenseStorage                    systemLicenseStorage;
    @Mock
    private SystemLicenseActivator                  systemLicenseActivator;
    @Mock
    private Subject                                 subject;
    @Captor
    private ArgumentCaptor<SystemLicenseActionImpl> actionCaptor;

    private SystemLicenseManager licenseManager;

    @BeforeMethod
    public void setUp() throws Exception {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(resource);

        when(licenseFactory.create(LICENSE_TEXT)).thenReturn(license);
        when(licenseFactory.create(NEW_LICENSE_TEXT)).thenReturn(newSystemLicense);
        when(license.getLicenseText()).thenReturn(LICENSE_TEXT);
        when(license.getLicenseId()).thenReturn(LICENSE_ID);
        when(newSystemLicense.getLicenseId()).thenReturn("2");
        when(newSystemLicense.getLicenseText()).thenReturn(NEW_LICENSE_TEXT);
        when(userManager.getTotalCount()).thenReturn(USER_NUMBER);
        when(systemLicenseAction.getLicenseId()).thenReturn(LICENSE_ID);

        setSizeOfAdditionalNodes(NODES_NUMBER);

        licenseManager = spy(new SystemLicenseManager(licenseFactory,
                                                      userManager,
                                                      swarmDockerConnector,
                                                      systemLicenseActionDao,
                                                      systemLicenseStorage,
                                                      systemLicenseActivator));

        doReturn(license).when(licenseManager).load();

        EnvironmentContext.getCurrent().setSubject(subject);
    }

    @AfterMethod
    public void tearDown() {
        EnvironmentContext.reset();
    }

    @Test
    public void shouldActivateAndPersistLicense() throws Exception {
        when(systemLicenseActivator.activateIfRequired(license)).thenReturn(ACTIVATED_LICENSE_TEXT);

        licenseManager.store(LICENSE_TEXT);

        verify(systemLicenseActivator).activateIfRequired(license);
        verify(systemLicenseStorage).persistLicense(LICENSE_TEXT);
        verify(systemLicenseStorage).persistActivatedLicense(ACTIVATED_LICENSE_TEXT);
    }

    @Test
    public void shouldDeleteLicense() throws Exception {
        when(systemLicenseStorage.loadLicense()).thenReturn(LICENSE_TEXT);

        licenseManager.delete();

        verify(systemLicenseStorage).clean();
    }

    @Test
    public void testIfFairSourceLicenseIsNotAccepted() throws Exception {
        when(systemLicenseActionDao.getByLicenseTypeAndAction(eq(FAIR_SOURCE_LICENSE), eq(ACCEPTED)))
            .thenThrow(new NotFoundException("System license not found"));

        assertFalse(licenseManager.isFairSourceLicenseAccepted());
    }

    @Test
    public void testIfFairSourceLicenseIsAccepted() throws Exception {
        when(systemLicenseActionDao.getByLicenseTypeAndAction(eq(FAIR_SOURCE_LICENSE), eq(ACCEPTED)))
            .thenReturn(mock(SystemLicenseActionImpl.class));

        assertTrue(licenseManager.isFairSourceLicenseAccepted());
    }

    @Test
    public void shouldLoadLicenseAndValidateActivation() throws Exception {
        when(licenseManager.load()).thenCallRealMethod();
        when(systemLicenseStorage.loadLicense()).thenReturn(LICENSE_TEXT);

        SystemLicense license = licenseManager.load();

        verify(systemLicenseActivator).validateActivation(this.license);
        verify(systemLicenseStorage).loadLicense();
        verify(licenseFactory).create(LICENSE_TEXT);
        assertEquals(license, this.license);
    }

    @Test
    public void testIsSystemLicenseUsageLegal() throws IOException, ServerException {
        doReturn(true).when(license).isLicenseUsageLegal(USER_NUMBER, NODES_NUMBER);

        assertTrue(licenseManager.isSystemUsageLegal());
    }

    @Test
    public void testIsSystemFreeUsageLegal() throws IOException, ServerException {
        when(userManager.getTotalCount()).thenReturn(MAX_NUMBER_OF_FREE_USERS);
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS);

        doThrow(SystemLicenseNotFoundException.class).when(licenseManager).load();

        assertTrue(licenseManager.isSystemUsageLegal());
    }

    @Test
    public void testIsSystemLicenseUsageNotLegal() throws IOException, ServerException {
        doReturn(false).when(license).isLicenseUsageLegal(USER_NUMBER, NODES_NUMBER);

        assertFalse(licenseManager.isSystemUsageLegal());
    }

    @Test
    public void testIsCodenvyFreeUsageNotLegal() throws IOException, ServerException {
        when(userManager.getTotalCount()).thenReturn(MAX_NUMBER_OF_FREE_USERS + 1);
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS + 1);

        doThrow(SystemLicenseNotFoundException.class).when(licenseManager).load();

        assertFalse(licenseManager.isSystemUsageLegal());
    }

    @Test
    public void testIsCodenvyActualNodesUsageLegal() throws IOException, ServerException {
        doReturn(true).when(license).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertTrue(licenseManager.isSystemNodesUsageLegal(null));
    }

    @Test
    public void testIsCodenvyGivenNodesUsageLegal() throws IOException, ServerException {
        doReturn(true).when(license).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertTrue(licenseManager.isSystemNodesUsageLegal(NODES_NUMBER));
        verify(swarmDockerConnector, never()).getAvailableNodes();
    }

    @Test
    public void testIsCodenvyActualNodesUsageNotLegal() throws IOException, ServerException {
        doReturn(false).when(license).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertFalse(licenseManager.isSystemNodesUsageLegal(null));
    }

    @Test
    public void testIsCodenvyGivenNodesUsageNotLegal() throws IOException, ServerException {
        doReturn(false).when(license).isLicenseNodesUsageLegal(NODES_NUMBER);

        assertFalse(licenseManager.isSystemNodesUsageLegal(NODES_NUMBER));
        verify(swarmDockerConnector, never()).getAvailableNodes();
    }

    @Test
    public void testIsCodenvyNodesFreeUsageLegal() throws IOException, ServerException {
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS);

        doThrow(SystemLicenseNotFoundException.class).when(licenseManager).load();

        assertTrue(licenseManager.isSystemNodesUsageLegal(null));
    }

    @Test
    public void testIsCodenvyNodesFreeUsageNotLegal() throws IOException, ServerException {
        setSizeOfAdditionalNodes(MAX_NUMBER_OF_FREE_SERVERS + 1);

        doThrow(SystemLicenseNotFoundException.class).when(licenseManager).load();

        assertFalse(licenseManager.isSystemNodesUsageLegal(null));
    }

    @Test
    public void shouldConfirmThatUserCanBeAddedDueToLicense() throws ServerException {
        when(userManager.getTotalCount()).thenReturn(USER_NUMBER);
        doReturn(true).when(license).isLicenseUsageLegal(USER_NUMBER + 1, 0);

        assertTrue(licenseManager.canUserBeAdded());
    }

    @Test
    public void shouldConfirmThatUserCanBeAddedDueToFreeUsageTerms() throws ServerException {
        when(userManager.getTotalCount()).thenReturn(MAX_NUMBER_OF_FREE_USERS - 1);
        doThrow(SystemLicenseNotFoundException.class).when(licenseManager).load();

        assertTrue(licenseManager.canUserBeAdded());
    }

    @Test
    public void shouldDisproveThatUserCanBeAddedDueToLicense() throws ServerException {
        when(userManager.getTotalCount()).thenReturn(USER_NUMBER);
        doReturn(false).when(license).isLicenseUsageLegal(USER_NUMBER + 1, 0);

        assertFalse(licenseManager.canUserBeAdded());
    }

    @Test
    public void shouldDisproveThatUserCanBeAddedDueToFreeUsageTerms() throws ServerException {
        when(userManager.getTotalCount()).thenReturn(MAX_NUMBER_OF_FREE_USERS);

        doThrow(SystemLicenseNotFoundException.class).when(licenseManager).load();

        assertFalse(licenseManager.canUserBeAdded());
    }

    @Test
    public void shouldReturnAllowedUserNumberDueToLicense() {
        doReturn((int)USER_NUMBER).when(license).getNumberOfUsers();
        doReturn(false).when(license).isExpiredCompletely();

        assertEquals(licenseManager.getAllowedUserNumber(), USER_NUMBER);
    }

    @Test
    public void shouldReturnAllowedUserNumberDueToFreeUsageTerms() {
        doThrow(SystemLicenseNotFoundException.class).when(licenseManager).load();
        assertEquals(licenseManager.getAllowedUserNumber(), MAX_NUMBER_OF_FREE_USERS);
    }

    @Test
    public void shouldReturnAllowedUserNumberDueToFreeUsageTermsWhenLicenseCompletelyExpired() {
        doReturn(true).when(license).isExpiredCompletely();
        assertEquals(licenseManager.getAllowedUserNumber(), MAX_NUMBER_OF_FREE_USERS);
    }

    @Test
    public void shouldReturnListOfIssuesWithExpiring() throws Exception {
        doReturn(false).when(licenseManager).canUserBeAdded();
        doReturn(false).when(licenseManager).isFairSourceLicenseAccepted();
        doReturn(true).when(licenseManager).isPaidLicenseExpiring();
        doReturn("License expiring").when(licenseManager).getMessageForLicenseExpiring();
        assertEquals(licenseManager.getLicenseIssues(),
                     ImmutableList.of(newDto(IssueDto.class).withStatus(Issue.Status.USER_LICENSE_HAS_REACHED_ITS_LIMIT)
                                                            .withMessage(SystemLicenseManager.LICENSE_HAS_REACHED_ITS_USER_LIMIT_MESSAGE_FOR_REGISTRATION),
                                      newDto(IssueDto.class).withStatus(Issue.Status.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED)
                                                            .withMessage(SystemLicenseManager.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_MESSAGE),
                                      newDto(IssueDto.class).withStatus(Issue.Status.LICENSE_EXPIRING)
                                                            .withMessage("License expiring")));
    }

    @Test
    public void shouldReturnListOfIssuesWithExpired() throws Exception {
        doReturn(false).when(licenseManager).canUserBeAdded();
        doReturn(false).when(licenseManager).isFairSourceLicenseAccepted();
        doReturn(false).when(licenseManager).isPaidLicenseExpiring();
        doReturn(true).when(licenseManager).isPaidLicenseCompletelyExpired();
        doReturn(false).when(licenseManager).isSystemUsageLegal();
        doReturn("License expired").when(licenseManager).getMessageForLicenseCompletelyExpired();
        assertEquals(licenseManager.getLicenseIssues(),
                     ImmutableList.of(newDto(IssueDto.class).withStatus(Issue.Status.USER_LICENSE_HAS_REACHED_ITS_LIMIT)
                                                            .withMessage(SystemLicenseManager.LICENSE_HAS_REACHED_ITS_USER_LIMIT_MESSAGE_FOR_REGISTRATION),
                                      newDto(IssueDto.class).withStatus(Issue.Status.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED)
                                                            .withMessage(SystemLicenseManager.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_MESSAGE),
                                      newDto(IssueDto.class).withStatus(Issue.Status.LICENSE_EXPIRED)
                                                            .withMessage("License expired")));
    }

    @Test
    public void shouldReturnEmptyListOfIssues() throws Exception {
        doReturn(true).when(licenseManager).canUserBeAdded();
        doReturn(true).when(licenseManager).isFairSourceLicenseAccepted();
        doReturn(false).when(licenseManager).isPaidLicenseExpiring();
        doReturn(false).when(licenseManager).isPaidLicenseCompletelyExpired();
        assertEquals(licenseManager.getLicenseIssues(), ImmutableList.of());
    }

    @Test
    public void testCanStartWorkspace() throws ServerException {
        // given
        doReturn(true).when(licenseManager).isLicenseUsageLegal(USER_NUMBER);

        // when
        boolean result = licenseManager.canStartWorkspace();

        // then
        assertTrue(result);
    }

    @Test
    public void testCannotStartWorkspace() throws ServerException {
        // given
        doReturn(false).when(licenseManager).isLicenseUsageLegal(USER_NUMBER);

        // when
        boolean result = licenseManager.canStartWorkspace();

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnExpiringStatus() {
        // given
        doReturn(true).when(license).isExpiring();

        // when
        boolean result = licenseManager.isPaidLicenseExpiring();

        // then
        assertTrue(result);
    }

    @Test
    public void shouldNotReturnExpiringStatus() {
        // given
        doReturn(false).when(license).isExpiring();

        // when
        boolean result = licenseManager.isPaidLicenseExpiring();

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnMessageForLicenseExpiring() {
        // given
        doReturn(5).when(license).daysBeforeCompleteExpiration();

        // when
        String result = licenseManager.getMessageForLicenseExpiring();

        // then
        assertEquals(result, "License expired. Codenvy will downgrade to a 3 user Fair Source license in 5 days.");
    }

    @Test
    public void shouldReturnExpiredStatus() throws ServerException, ConflictException {
        // given
        doReturn(true).when(license).isExpiredCompletely();

        // when
        Boolean result = licenseManager.isPaidLicenseCompletelyExpired();

        // then
        assertTrue(result);
        verify(licenseManager).revertToFairSourceLicense(license);
    }

    @Test
    public void shouldReturnNonExpiredStatus() throws ServerException, ConflictException {
        // given
        doReturn(false).when(license).isExpiredCompletely();

        // when
        Boolean result = licenseManager.isPaidLicenseCompletelyExpired();

        // then
        assertFalse(result);
        verify(licenseManager, never()).revertToFairSourceLicense(license);
    }

    @Test
    public void testGetMessageForLicenseExpiredForAdmin() throws ServerException {
        // given
        doReturn(true).when(subject).hasPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);

        // when
        String result = licenseManager.getMessageForLicenseCompletelyExpired();

        // then
        assertEquals(result, "There are currently 4 users registered in Codenvy but your license only allows 3. Users cannot start workspaces.");
        verify(licenseManager, never()).load();
    }

    @Test
    public void testGetMessageForLicenseExpiredForNonAdmin() throws ServerException {
        // given
        EnvironmentContext.getCurrent().setSubject(null);

        // when
        String result = licenseManager.getMessageForLicenseCompletelyExpired();

        // then
        assertEquals(result, "The Codenvy license is expired - you can access the user dashboard but not the IDE.");
    }

    @Test
    public void testGetMessageForLicenseExpiredWhenLicenseAbsentForNonAdmin() throws ServerException {
        // given
        doReturn(false).when(subject).hasPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
        doThrow(SystemLicenseException.class).when(licenseManager).load();

        // when
        String result = licenseManager.getMessageForLicenseCompletelyExpired();

        // then
        assertEquals(result, "The Codenvy license has reached its user limit - you can access the user dashboard but not the IDE.");
    }

    @Test
    public void shouldRevertToFairSourceLicense() throws ServerException, ConflictException, NotFoundException {
        // given
        doReturn(LICENSE_ID).when(license).getLicenseId();
        doThrow(NotFoundException.class).when(systemLicenseActionDao).getByLicenseIdAndAction(LICENSE_ID, EXPIRED);

        // when
        licenseManager.revertToFairSourceLicense(license);

        // then
        verify(systemLicenseActionDao).upsert(actionCaptor.capture());

        final SystemLicenseActionImpl action = actionCaptor.getValue();
        assertEquals(action.getLicenseType(), PRODUCT_LICENSE);
        assertEquals(action.getActionType(), EXPIRED);
        assertEquals(action.getLicenseId(), LICENSE_ID);
        assertEquals(action.getAttributes(), Collections.emptyMap());
    }

    @Test
    public void shouldNotRevertToFairSourceLicense() throws ServerException, ConflictException, NotFoundException {
        // when
        licenseManager.revertToFairSourceLicense(license);

        // then
        verify(systemLicenseActionDao, never()).upsert(any());
    }

    private void setSizeOfAdditionalNodes(int size) throws IOException {
        when(swarmDockerConnector.getAvailableNodes()).thenReturn(dockerNodes);
        when(dockerNodes.size()).thenReturn(size);
    }

}
