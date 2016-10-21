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
package com.codenvy.api.audit.server;

import com.codenvy.api.license.CodenvyLicense;
import com.codenvy.api.license.LicenseException;
import com.codenvy.api.license.server.CodenvyLicenseManager;
import com.codenvy.api.permission.server.PermissionsManager;
import com.codenvy.api.permission.server.model.impl.AbstractPermissions;

import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import static java.nio.file.Files.createTempFile;
import static java.util.Arrays.asList;
import static java.util.Calendar.JANUARY;
import static java.util.Collections.singletonList;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link AuditManager}.
 *
 * @author Igor Vinokur
 */
@Listeners(value = MockitoTestNGListener.class)
public class AuditManagerTest {

    private static final String FULL_AUDIT_REPORT                                  =
            "Number of all users: 2\n" +
            "Number of users licensed: 15\n" +
            "Date when license expires: 01 January 2016\n" +
            "user@email.com is owner of 1 workspace and has permissions in 2 workspaces\n" +
            "   └ Workspace1Name, is owner: true, permissions: [read, use, run, configure, setPermissions, delete]\n" +
            "   └ Workspace2Name, is owner: false, permissions: [read, use, run, configure, setPermissions]\n" +
            "user2@email.com is owner of 1 workspace and has permissions in 1 workspace\n" +
            "   └ Workspace2Name, is owner: true, permissions: [read, use, run, configure, setPermissions, delete]\n";
    private static final String AUDIT_REPORT_WITHOUT_LICENSE                       =
            "Number of all users: 2\n" +
            "[ERROR] Failed to retrieve license!\n" +
            "user@email.com is owner of 1 workspace and has permissions in 2 workspaces\n" +
            "   └ Workspace1Name, is owner: true, permissions: [read, use, run, configure, setPermissions, delete]\n" +
            "   └ Workspace2Name, is owner: false, permissions: [read, use, run, configure, setPermissions]\n" +
            "user2@email.com is owner of 1 workspace and has permissions in 1 workspace\n" +
            "   └ Workspace2Name, is owner: true, permissions: [read, use, run, configure, setPermissions, delete]\n";
    private static final String AUDIT_REPORT_WITHOUT_USER_WORKSPACES               =
            "Number of all users: 2\n" +
            "Number of users licensed: 15\n" +
            "Date when license expires: 01 January 2016\n" +
            "[ERROR] Failed to receive list of related workspaces for user User1Id!\n" +
            "user2@email.com is owner of 1 workspace and has permissions in 1 workspace\n" +
            "   └ Workspace2Name, is owner: true, permissions: [read, use, run, configure, setPermissions, delete]\n";
    private static final String AUDIT_REPORT_WITHOUT_USER_PERMISSIONS_TO_WORKSPACE =
            "Number of all users: 2\n" +
            "Number of users licensed: 15\n" +
            "Date when license expires: 01 January 2016\n" +
            "user@email.com is owner of 0 workspaces and has permissions in 2 workspaces\n" +
            "   └ Workspace1Name, is owner: false, permissions: [read, use, run, configure, setPermissions, delete]\n" +
            "   └ Workspace2Name, is owner: false, permissions: [read, use, run, configure, setPermissions]\n" +
            "user2@email.com is owner of 2 workspaces and has permissions in 1 workspace\n" +
            "   └ Workspace1Name, is owner: true, permissions: []\n" +
            "   └ Workspace2Name, is owner: true, permissions: [read, use, run, configure, setPermissions, delete]\n";

    private Path auditReport;

    @Mock
    private UserManager           userManager;
    @Mock
    private WorkspaceManager      workspaceManager;
    @Mock
    private PermissionsManager    permissionsManager;
    @Mock
    private CodenvyLicenseManager licenseManager;
    @Mock
    private WorkspaceImpl         workspace1;
    @Mock
    private WorkspaceImpl         workspace2;

    private AuditManager auditManager;

    @BeforeMethod
    public void setUp() throws Exception {
        //License
        CodenvyLicense license = mock(CodenvyLicense.class);
        when(license.getNumberOfUsers()).thenReturn(15);
        when(license.getExpirationDate()).thenReturn(new GregorianCalendar(2016, JANUARY, 1).getTime());
        when(licenseManager.load()).thenReturn(license);
        //User
        UserImpl user1 = mock(UserImpl.class);
        UserImpl user2 = mock(UserImpl.class);
        when(user1.getEmail()).thenReturn("user@email.com");
        when(user2.getEmail()).thenReturn("user2@email.com");
        when(user1.getId()).thenReturn("User1Id");
        when(user2.getId()).thenReturn("User2Id");
        when(user1.getName()).thenReturn("User1");
        when(user2.getName()).thenReturn("User2");
        //Workspace config
        WorkspaceConfigImpl ws1config = mock(WorkspaceConfigImpl.class);
        WorkspaceConfigImpl ws2config = mock(WorkspaceConfigImpl.class);
        when(ws1config.getName()).thenReturn("Workspace1Name");
        when(ws2config.getName()).thenReturn("Workspace2Name");
        //Workspace
        when(workspace1.getNamespace()).thenReturn("User1");
        when(workspace2.getNamespace()).thenReturn("User2");
        when(workspace1.getId()).thenReturn("Workspace1Id");
        when(workspace2.getId()).thenReturn("Workspace2Id");
        when(workspace1.getConfig()).thenReturn(ws1config);
        when(workspace2.getConfig()).thenReturn(ws2config);
        when(workspaceManager.getWorkspaces("User1Id")).thenReturn(asList(workspace1, workspace2));
        when(workspaceManager.getWorkspaces("User2Id")).thenReturn(singletonList(workspace2));
        //Permissions
        AbstractPermissions ws1User1Permissions = mock(AbstractPermissions.class);
        AbstractPermissions ws2User1Permissions = mock(AbstractPermissions.class);
        AbstractPermissions ws2User2Permissions = mock(AbstractPermissions.class);
        when(ws1User1Permissions.getUserId()).thenReturn("User1Id");
        when(ws2User1Permissions.getUserId()).thenReturn("User1Id");
        when(ws2User2Permissions.getUserId()).thenReturn("User2Id");
        when(ws1User1Permissions.getActions()).thenReturn(asList("read", "use", "run", "configure", "setPermissions", "delete"));
        when(ws2User1Permissions.getActions()).thenReturn(asList("read", "use", "run", "configure", "setPermissions"));
        when(ws2User2Permissions.getActions()).thenReturn(asList("read", "use", "run", "configure", "setPermissions", "delete"));
        when(ws1User1Permissions.getInstanceId()).thenReturn("Workspace1Id");
        when(ws2User1Permissions.getInstanceId()).thenReturn("Workspace2Id");
        when(ws2User2Permissions.getInstanceId()).thenReturn("Workspace2Id");
        when(permissionsManager.get(eq("User1Id"), anyString(), eq("Workspace1Id"))).thenReturn(ws1User1Permissions);
        when(permissionsManager.get(eq("User1Id"), anyString(), eq("Workspace2Id"))).thenReturn(ws2User1Permissions);
        when(permissionsManager.get(eq("User2Id"), anyString(), eq("Workspace2Id"))).thenReturn(ws2User2Permissions);
        //Page
        Page page = mock(Page.class);
        when(page.getItems()).thenReturn(asList(user1, user2));
        when(page.hasNextPage()).thenReturn(false);
        when(userManager.getAll(1, 0)).thenReturn(page);
        when(userManager.getAll(30, 0)).thenReturn(page);

        when(userManager.getTotalCount()).thenReturn(2L);

        auditManager =
                new AuditManager(userManager, workspaceManager, permissionsManager, licenseManager, new AuditReportPrinter());
        auditReport = createTempFile("report", ".txt");
    }

    @AfterMethod
    public void tearDown() throws Exception {
        auditManager.deleteReportDirectory(auditReport);
    }

    @Test
    public void shouldReturnFullAuditReport() throws Exception {
        //when
        auditReport = auditManager.generateAuditReport();

        //then
        assertEquals(readFileToString(auditReport.toFile()), FULL_AUDIT_REPORT);
    }

    @Test
    public void shouldReturnFullAuditReportWithWorkspaceThatBelongsToUserButWithoutPermissionsToUser() throws Exception {
        //given
        List<WorkspaceImpl> workspaces = new ArrayList<>();
        workspaces.add(workspace2);
        when(workspace1.getNamespace()).thenReturn("User2");
        when(workspace2.getNamespace()).thenReturn("User2");
        when(workspaceManager.getWorkspaces("User2Id")).thenReturn(workspaces);
        when(workspaceManager.getByNamespace("User2")).thenReturn(asList(workspace1, workspace2));

        //when
        auditReport = auditManager.generateAuditReport();

        //then
        assertEquals(readFileToString(auditReport.toFile()), AUDIT_REPORT_WITHOUT_USER_PERMISSIONS_TO_WORKSPACE);
    }

    @Test
    public void shouldReturnAuditReportWithoutLicenseInfoIfFailedToRetrieveLicense() throws Exception {
        //given
        when(licenseManager.load()).thenThrow(new LicenseException("Failed to retrieve license info"));

        //when
        auditReport = auditManager.generateAuditReport();

        //then
        assertEquals(readFileToString(auditReport.toFile()), AUDIT_REPORT_WITHOUT_LICENSE);
    }

    @Test
    public void shouldReturnAuditReportWithoutUserWorkspacesIfFailedToRetrieveTheListOfHisWorkspaces() throws Exception {
        //given
        when(workspaceManager.getWorkspaces(eq("User1Id"))).thenThrow(new ServerException("Failed to retrieve workspaces"));

        //when
        auditReport = auditManager.generateAuditReport();

        //then
        assertEquals(readFileToString(auditReport.toFile()), AUDIT_REPORT_WITHOUT_USER_WORKSPACES);
    }
}
