/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.spi.invite.tck;

import com.codenvy.api.invite.InviteImpl;
import com.codenvy.api.workspace.server.WorkspaceDomain;
import com.codenvy.organization.api.permissions.OrganizationDomain;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.codenvy.spi.invite.InviteDao;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Pages;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests {@link InviteDao} contract.
 *
 * @author Sergii Leshchenko
 */
@Listeners(TckListener.class)
@Test(suiteName = InviteDaoTest.SUITE_NAME)
public class InviteDaoTest {
    public static final String SUITE_NAME = "InviteDaoTck";

    private static final String EMAIL_1 = "user1@test.com";
    private static final String EMAIL_2 = "user2@test.com";

    private OrganizationImpl organization;

    private WorkspaceImpl workspace;

    private InviteImpl[] invites;

    @Inject
    private InviteDao inviteDao;

    @Inject
    private TckRepository<OrganizationImpl> organizationRepo;

    @Inject
    private TckRepository<WorkspaceImpl> workspaceRepo;

    @Inject
    private TckRepository<InviteImpl> inviteRepo;

    @BeforeMethod
    private void setUp() throws TckRepositoryException {
        organization = new OrganizationImpl("org123", "test", null);

        workspace = new WorkspaceImpl("ws123", organization.getAccount(), null);

        invites = new InviteImpl[3];
        invites[0] = new InviteImpl(EMAIL_1, OrganizationDomain.DOMAIN_ID, organization.getId(), asList("read", "delete"));
        invites[1] = new InviteImpl(EMAIL_1, WorkspaceDomain.DOMAIN_ID, workspace.getId(), asList("read", "update", "delete"));
        invites[2] = new InviteImpl(EMAIL_2, WorkspaceDomain.DOMAIN_ID, workspace.getId(), asList("read", "update"));

        organizationRepo.createAll(singletonList(organization));
        workspaceRepo.createAll(singletonList(workspace));
        inviteRepo.createAll(Stream.of(invites)
                                   .map(InviteImpl::new)
                                   .collect(Collectors.toList()));
    }

    @AfterMethod
    private void cleanup() throws TckRepositoryException {
        inviteRepo.removeAll();
        workspaceRepo.removeAll();
        organizationRepo.removeAll();
    }

    @Test
    public void shouldCreateOrganizationInvite() throws Exception {
        final InviteImpl invite = new InviteImpl(EMAIL_2,
                                                 OrganizationDomain.DOMAIN_ID,
                                                 organization.getId(),
                                                 asList("read", "update", "delete"));

        Optional<InviteImpl> existing = inviteDao.store(new InviteImpl(invite));

        assertFalse(existing.isPresent());
        assertEquals(inviteDao.getInvite(invite.getDomainId(), invite.getInstanceId(), invite.getEmail()), invite);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNPEOnStoringNullInvite() throws Exception {
        inviteDao.store(null);
    }

    @Test
    public void shouldUpdateInviteIfItIsAlreadyExistOnStoring() throws Exception {
        InviteImpl toUpdate = new InviteImpl(invites[0].getEmail(),
                                             invites[0].getDomainId(),
                                             invites[0].getInstanceId(),
                                             singletonList("read"));

        Optional<InviteImpl> existing = inviteDao.store(toUpdate);

        assertTrue(existing.isPresent());
        assertEquals(existing.get(), invites[0]);
        assertEquals(inviteDao.getInvite(toUpdate.getDomainId(), toUpdate.getInstanceId(), toUpdate.getEmail()), toUpdate);
    }

    @Test
    public void shouldReturnInvitesByEmail() throws Exception {
        List<InviteImpl> email1Invitations = Pages.stream((maxItems, skipCount) ->
                                                                  inviteDao.getInvites(EMAIL_1, maxItems, skipCount),
                                                          1)
                                                  .collect(Collectors.toList());

        assertEquals(email1Invitations.size(), 2);
        assertTrue(email1Invitations.contains(invites[0]));
        assertTrue(email1Invitations.contains(invites[1]));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNPEOnGettingInvitesByNullEmail() throws Exception {
        inviteDao.getInvites(null, 1, 0);
    }

    @Test
    public void shouldReturnInvitesByWorkspace() throws Exception {
        List<InviteImpl> workspaceInvites = Pages.stream((maxItems, skipCount) ->
                                                                 inviteDao.getInvites(WorkspaceDomain.DOMAIN_ID, workspace.getId(),
                                                                                      skipCount, maxItems),
                                                         1)
                                                 .collect(Collectors.toList());

        assertEquals(workspaceInvites.size(), 2);
        assertTrue(workspaceInvites.contains(invites[1]));
        assertTrue(workspaceInvites.contains(invites[2]));
    }

    @Test
    public void shouldReturnInvitesByOrganization() throws Exception {
        List<InviteImpl> organizationInvites = Pages.stream((maxItems, skipCount) ->
                                                                    inviteDao.getInvites(OrganizationDomain.DOMAIN_ID, organization.getId(),
                                                                                         skipCount, maxItems),
                                                            1)
                                                    .collect(Collectors.toList());

        assertEquals(organizationInvites.size(), 1);
        assertTrue(organizationInvites.contains(invites[0]));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNPEOnGettingInvitesByInstanceWithNullDomainId() throws Exception {
        inviteDao.getInvites(null, "test123", 0, 1);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNPEOnGettingInvitesByNullInstance() throws Exception {
        inviteDao.getInvites("test", null, 0, 1);
    }

    @Test
    public void shouldReturnInvite() throws Exception {
        InviteImpl fetched = inviteDao.getInvite(OrganizationDomain.DOMAIN_ID, organization.getId(), EMAIL_1);

        assertEquals(fetched, invites[0]);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnGettingInviteByNullEmail() throws Exception {
        inviteDao.getInvite(null, OrganizationDomain.DOMAIN_ID, organization.getId());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnGettingInviteByNullDomainId() throws Exception {
        inviteDao.getInvite(EMAIL_1, null, organization.getId());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnGettingInviteByNullInstanceId() throws Exception {
        inviteDao.getInvite(EMAIL_1, OrganizationDomain.DOMAIN_ID, null);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldRemoveInvite() throws Exception {
        inviteDao.remove(EMAIL_1, OrganizationDomain.DOMAIN_ID, organization.getId());

        inviteDao.getInvite("non-existing@test.com", "test", "test123");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnRemovingInviteByNullEmail() throws Exception {
        inviteDao.remove(null, OrganizationDomain.DOMAIN_ID, organization.getId());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnRemovingInviteByNullDomainId() throws Exception {
        inviteDao.remove(EMAIL_1, null, organization.getId());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnRemovingInviteByNullInstanceId() throws Exception {
        inviteDao.remove(EMAIL_1, OrganizationDomain.DOMAIN_ID, null);
    }
}
