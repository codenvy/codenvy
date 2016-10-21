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
package com.codenvy.api.workspace.server.filters;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.environment.server.MachineService;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.resource.GenericResourceMethod;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static com.codenvy.api.workspace.server.WorkspaceDomain.CONFIGURE;
import static com.codenvy.api.workspace.server.WorkspaceDomain.DOMAIN_ID;
import static com.codenvy.api.workspace.server.WorkspaceDomain.READ;
import static com.codenvy.api.workspace.server.WorkspaceDomain.RUN;
import static com.codenvy.api.workspace.server.WorkspaceDomain.USE;
import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link WorkspacePermissionsFilter}.
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class WorkspacePermissionsFilterTest {
    private static final String             USERNAME = "userok";
    @SuppressWarnings("unused")
    private static final ApiExceptionMapper MAPPER   = new ApiExceptionMapper();
    @SuppressWarnings("unused")
    private static final EnvironmentFilter  FILTER   = new EnvironmentFilter();

    @Mock
    WorkspaceManager workspaceManager;

    @Mock
    AccountManager accountManager;

    @Mock
    AccountImpl account;

    @InjectMocks
    @Spy
    WorkspacePermissionsFilter permissionsFilter;

    @Mock
    private static Subject subject;

    @Mock
    WorkspaceService workspaceService;

    @Mock
    MachineService machineService;

    @Mock
    WorkspaceImpl workspace;

    @BeforeMethod
    public void setUp() throws Exception {
        doThrow(new ForbiddenException("")).when(permissionsFilter).checkNamespaceAccess(any(), any(), anyVararg());

        when(subject.getUserName()).thenReturn(USERNAME);
        when(workspaceManager.getWorkspace(any())).thenReturn(workspace);
        when(workspace.getNamespace()).thenReturn("namespace");
        when(workspace.getId()).thenReturn("workspace123");

        when(accountManager.getByName(any())).thenReturn(account);
    }

    @Test
    public void shouldCheckNamespaceAccessOnWorkspaceCreation() throws Exception {
        doNothing().when(permissionsFilter).checkNamespaceAccess(any(), any(), anyVararg());

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/workspace?namespace=userok");

        assertEquals(response.getStatusCode(), 204);
        verify(workspaceService).create(any(), any(), any(), eq("userok"));
        verify(permissionsFilter).checkNamespaceAccess(any(), eq("userok"), anyVararg());
        verifyZeroInteractions(subject);
    }

    @Test
    public void shouldCheckNamespaceAccessOnFetchingWorkspacesByNamespace() throws Exception {
        doNothing().when(permissionsFilter).checkNamespaceAccess(any(), any(), anyVararg());

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/workspace/namespace/userok");

        assertEquals(response.getStatusCode(), 200);
        verify(workspaceService).getByNamespace(any(), eq("userok"));
        verify(permissionsFilter).checkNamespaceAccess(any(), eq("userok"), anyVararg());
        verifyZeroInteractions(subject);
    }

    @Test
    public void shouldCheckNamespaceAccessOnStaringWorkspaceFromConfig() throws Exception {
        doNothing().when(permissionsFilter).checkNamespaceAccess(any(), any(), anyVararg());

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/workspace/runtime?namespace=userok");

        assertEquals(response.getStatusCode(), 204);
        verify(workspaceService).startFromConfig(any(), any(), eq("userok"));
        verify(permissionsFilter).checkNamespaceAccess(any(), eq("userok"), anyVararg());
        verifyZeroInteractions(subject);
    }

    @Test
    public void shouldNotCheckPermissionsOnWorkspacesGetting() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/workspace");

        assertEquals(response.getStatusCode(), 200);
        verify(workspaceService).getWorkspaces(any(), anyInt(), anyString());
        verifyZeroInteractions(subject);
    }

    @Test
    public void shouldCheckPermissionsOnMachineCreating() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", "run")).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/workspace/{id}/machine");

        assertEquals(response.getStatusCode(), 204);
        verify(machineService).startMachine(eq("workspace123"), any());
        verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("run"));
    }

    @Test
    public void shouldCheckPermissionsOnMachineDestroying() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", RUN)).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .contentType("application/json")
                                         .when()
                                         .delete(SECURE_PATH + "/workspace/{id}/machine/machine123");

        assertEquals(response.getStatusCode(), 204);
        verify(machineService).stopMachine(eq("workspace123"), any());
        verify(subject).hasPermission(eq("workspace"), eq("workspace123"), Matchers.eq(RUN));
    }

    @Test
    public void shouldCheckPermissionsOnGettingMachineById() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", USE)).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/workspace/{id}/machine/machine123");

        assertEquals(response.getStatusCode(), 204);
        verify(machineService).getMachineById(eq("workspace123"), eq("machine123"));
        verify(subject).hasPermission(DOMAIN_ID, "workspace123", USE);
    }

    @Test
    public void shouldCheckPermissionsOnGettingMachines() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", USE)).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/workspace/{id}/machine");

        assertEquals(response.getStatusCode(), 200);
        verify(machineService).getMachines(eq("workspace123"));
        verify(subject).hasPermission(DOMAIN_ID, "workspace123", USE);
    }

    @Test
    public void shouldCheckPermissionsOnCommandExecuting() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", USE)).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/workspace/{id}/machine/machine123/command");

        assertEquals(response.getStatusCode(), 204);
        verify(machineService).executeCommandInMachine(eq("workspace123"), eq("machine123"), any(), anyString());
        verify(subject).hasPermission(DOMAIN_ID, "workspace123", USE);
    }

    @Test
    public void shouldCheckPermissionsOnProcessesGetting() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", USE)).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/workspace/{id}/machine/machine123/process");

        assertEquals(response.getStatusCode(), 200);
        verify(machineService).getProcesses(eq("workspace123"), eq("machine123"));
        verify(subject).hasPermission(DOMAIN_ID, "workspace123", USE);
    }

    @Test
    public void shouldCheckPermissionsOnProcessStopping() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", USE)).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .contentType("application/json")
                                         .when()
                                         .delete(SECURE_PATH + "/workspace/{id}/machine/machine123/process/123");

        assertEquals(response.getStatusCode(), 204);
        verify(machineService).stopProcess(eq("workspace123"), eq("machine123"), eq(123));
        verify(subject).hasPermission(DOMAIN_ID, "workspace123", USE);
    }

    @Test
    public void shouldCheckPermissionsOnProcessLogsGetting() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", USE)).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/workspace/{id}/machine/machine123/process/123/logs");

        assertEquals(response.getStatusCode(), 204);
        verify(machineService).getProcessLogs(eq("workspace123"), eq("machine123"), eq(123), any());
        verify(subject).hasPermission(DOMAIN_ID, "workspace123", USE);
    }

    @Test
    public void shouldCheckPermissionsOnWorkspaceStopping() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", "run")).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .contentType("application/json")
                                         .when()
                                         .delete(SECURE_PATH + "/workspace/{id}/runtime");

        assertEquals(response.getStatusCode(), 204);
        verify(workspaceService).stop(eq("workspace123"), any());
        verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("run"));
    }

    @Test
    public void shouldCheckPermissionsOnWorkspaceStarting() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", "run")).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/workspace/{id}/runtime");

        assertEquals(response.getStatusCode(), 204);
        verify(workspaceService).startById(eq("workspace123"), anyString(), any());
        verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("run"));
    }

    @Test
    public void shouldCheckPermissionsOnSnapshotStarting() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", "run")).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/workspace/{id}/snapshot");

        assertEquals(response.getStatusCode(), 204);
        verify(workspaceService).createSnapshot(eq("workspace123"));
        verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("run"));
    }

    @Test
    public void shouldCheckPermissionsOnSnapshotGetting() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", "read")).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/workspace/{id}/snapshot");

        assertEquals(response.getStatusCode(), 200);
        verify(workspaceService).getSnapshot(eq("workspace123"));
        verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("read"));
    }

    @Test
    public void shouldCheckPermissionsOnGetWorkspaceByKey() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", "read")).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("key", "workspace123")
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/workspace/{key}");

        assertEquals(response.getStatusCode(), 204);
        verify(workspaceService).getByKey(eq("workspace123"));
        verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("read"));
    }

    @Test
    public void shouldCheckPermissionsOnGetWorkspaceByUserNameAndWorkspaceName() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", "read")).thenReturn(true);
        User storedUser = mock(User.class);
        when(storedUser.getId()).thenReturn("user123");

        WorkspaceImpl workspace = mock(WorkspaceImpl.class);
        when(workspace.getId()).thenReturn("workspace123");
        when(workspaceManager.getWorkspace("myWorkspace", "userok")).thenReturn(workspace);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("key", "userok:myWorkspace")
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/workspace/{key}");

        assertEquals(response.getStatusCode(), 204);
        verify(workspaceService).getByKey(eq("userok:myWorkspace"));
        verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("read"));
    }

    @Test
    public void shouldCheckPermissionsOnProjectAdding() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", "configure")).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/workspace/{id}/project");

        assertEquals(response.getStatusCode(), 204);
        verify(workspaceService).addProject(eq("workspace123"), any());
        verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("configure"));
    }

    @Test
    public void shouldCheckPermissionsOnProjectRemoving() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", "configure")).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .when()
                                         .delete(SECURE_PATH + "/workspace/{id}/project/spring");

        assertEquals(response.getStatusCode(), 204);
        verify(workspaceService).deleteProject(eq("workspace123"), eq("spring"));
        verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("configure"));
    }

    @Test
    public void shouldCheckPermissionsOnProjectUpdating() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", "configure")).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .when()
                                         .put(SECURE_PATH + "/workspace/{id}/project/spring");

        assertEquals(response.getStatusCode(), 204);
        verify(workspaceService).updateProject(eq("workspace123"), eq("spring"), any());
        verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("configure"));
    }

    @Test
    public void shouldCheckPermissionsOnCommandAdding() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", "configure")).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .pathParam("id", "workspace123")
                                         .when()
                                         .post(SECURE_PATH + "/workspace/{id}/command");

        assertEquals(response.getStatusCode(), 204);
        verify(workspaceService).addCommand(eq("workspace123"), any());
        verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("configure"));
    }

    @Test
    public void shouldCheckPermissionsOnCommandRemoving() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", "configure")).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .when()
                                         .delete(SECURE_PATH + "/workspace/{id}/command/run-application");

        assertEquals(response.getStatusCode(), 204);
        verify(workspaceService).deleteCommand(eq("workspace123"), eq("run-application"));
        verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("configure"));
    }

    @Test
    public void shouldCheckPermissionsOnCommandUpdating() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", "configure")).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .when()
                                         .put(SECURE_PATH + "/workspace/{id}/command/run-application");

        assertEquals(response.getStatusCode(), 204);
        verify(workspaceService).updateCommand(eq("workspace123"), eq("run-application"), any());
        verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("configure"));
    }

    @Test
    public void shouldCheckPermissionsOnEnvironmentAdding() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", "configure")).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/workspace/{id}/environment");

        assertEquals(response.getStatusCode(), 204);
        verify(workspaceService).addEnvironment(eq("workspace123"), any(), anyString());
        verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("configure"));
    }

    @Test
    public void shouldCheckPermissionsOnEnvironmentRemoving() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", "configure")).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .when()
                                         .delete(SECURE_PATH + "/workspace/{id}/environment/ubuntu");

        assertEquals(response.getStatusCode(), 204);
        verify(workspaceService).deleteEnvironment(eq("workspace123"), eq("ubuntu"));
        verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("configure"));
    }

    @Test
    public void shouldCheckPermissionsOnEnvironmentUpdating() throws Exception {
        when(subject.hasPermission("workspace", "workspace123", "configure")).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .pathParam("id", "workspace123")
                                         .when()
                                         .put(SECURE_PATH + "/workspace/{id}/environment/ubuntu");

        assertEquals(response.getStatusCode(), 204);
        verify(workspaceService).updateEnvironment(eq("workspace123"), eq("ubuntu"), any());
        verify(subject).hasPermission(eq("workspace"), eq("workspace123"), eq("configure"));
    }

    @Test(expectedExceptions = ForbiddenException.class,
          expectedExceptionsMessageRegExp = "The user does not have permission to perform this operation")
    public void shouldThrowForbiddenExceptionWhenRequestedUnknownMethod() throws Exception {
        final GenericResourceMethod mock = mock(GenericResourceMethod.class);
        Method injectLinks = WorkspaceService.class.getMethod("getServiceDescriptor");
        when(mock.getMethod()).thenReturn(injectLinks);

        permissionsFilter.filter(mock, new Object[] {});
    }

    @Test(dataProvider = "coveredPaths")
    public void shouldThrowForbiddenExceptionWhenUserDoesNotHavePermissionsForPerformOperation(String path,
                                                                                               String method,
                                                                                               String action) throws Exception {
        when(subject.hasPermission(anyString(), anyString(), anyString())).thenReturn(false);
        doThrow(new ForbiddenException("")).when(permissionsFilter).checkNamespaceAccess(any(), any(), anyVararg());

        Response response = request(given().auth()
                                           .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                           .contentType("application/json")
                                           .when(),
                                    SECURE_PATH + path,
                                    method);

        assertEquals(response.getStatusCode(), 403);
        assertEquals(unwrapError(response), "The user does not have permission to " + action + " workspace with id 'workspace123'");

        verifyZeroInteractions(workspaceService);
        verifyZeroInteractions(machineService);
    }

    @Test(dataProvider = "coveredPaths")
    public void shouldNotCheckWorkspacePermissionsWhenWorkspaceBelongToHisPersonalAccount(String path,
                                                                                          String method,
                                                                                          String action) throws Exception {
        doNothing().when(permissionsFilter).checkNamespaceAccess(any(), any(), anyVararg());
        when(workspace.getNamespace()).thenReturn(USERNAME);

        Response response = request(given().auth()
                                           .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                           .contentType("application/json")
                                           .when(),
                                    SECURE_PATH + path,
                                    method);
        //Successful 2xx
        assertEquals(response.getStatusCode() / 100, 2);
        verify(subject, never()).hasPermission(any(), any(), any());
    }

    @Test
    public void shouldNotThrowExceptionWhenNamespaceIsNullOnNamespaceAccessChecking() throws Exception {
        doCallRealMethod().when(permissionsFilter).checkNamespaceAccess(any(), any());

        permissionsFilter.checkNamespaceAccess(subject, null);
    }

    @Test
    public void shouldNotThrowExceptionWhenNamespaceEqualsToPersonalAccountNameOfUserOnNamespaceAccessChecking() throws Exception {
        when(account.getName()).thenReturn(USERNAME);
        when(account.getType()).thenReturn(UserImpl.PERSONAL_ACCOUNT);
        doCallRealMethod().when(permissionsFilter).checkNamespaceAccess(any(), any());

        permissionsFilter.checkNamespaceAccess(subject, USERNAME);
    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void shouldThrowForbiddenExceptionWhenNamespaceIsNotNullAndDoesNotEqualToUserNameOnNamespaceAccessChecking() throws Exception {
        doCallRealMethod().when(permissionsFilter).checkNamespaceAccess(any(), any());

        permissionsFilter.checkNamespaceAccess(subject, "namespace");
    }

    @DataProvider(name = "coveredPaths")
    public Object[][] pathsProvider() {
        return new Object[][] {
                {"/workspace/workspace123", "get", READ},
                {"/workspace/workspace123", "put", CONFIGURE},
                {"/workspace/workspace123/runtime", "post", RUN},
                {"/workspace/workspace123/runtime", "delete", RUN},
                {"/workspace/workspace123/snapshot", "post", RUN},
                {"/workspace/workspace123/snapshot", "get", READ},
                {"/workspace/workspace123/command", "post", CONFIGURE},
                {"/workspace/workspace123/command/run-application", "put", CONFIGURE},
                {"/workspace/workspace123/command/run-application", "delete", CONFIGURE},
                {"/workspace/workspace123/environment", "post", CONFIGURE},
                {"/workspace/workspace123/environment/myEnvironment", "put", CONFIGURE},
                {"/workspace/workspace123/environment/myEnvironment", "delete", CONFIGURE},
                {"/workspace/workspace123/project", "post", CONFIGURE},
                {"/workspace/workspace123/project/spring", "put", CONFIGURE},
                {"/workspace/workspace123/project/spring", "delete", CONFIGURE},
                {"/workspace/ws123/machine", "post", RUN},
                {"/workspace/ws123/machine", "get", USE},
                {"/workspace/ws123/machine/mc123", "delete", RUN},
                {"/workspace/ws123/machine/mc123", "get", USE},
                {"/workspace/ws123/machine/mc123/process", "get", USE},
                {"/workspace/ws123/machine/mc123/process/123", "delete", USE},
                {"/workspace/ws123/machine/mc123/process/123/logs", "get", USE},
                {"/workspace/ws123/machine/mc123/command", "post", USE}
        };
    }

    private Response request(RequestSpecification request, String path, String method) {
        switch (method) {
            case "post":
                return request.post(path);
            case "get":
                return request.get(path);
            case "delete":
                return request.delete(path);
            case "put":
                return request.put(path);
        }
        throw new RuntimeException("Unsupported method");
    }

    private static String unwrapError(Response response) {
        return unwrapDto(response, ServiceError.class).getMessage();
    }

    private static <T> T unwrapDto(Response response, Class<T> dtoClass) {
        return DtoFactory.getInstance().createDtoFromJson(response.body().print(), dtoClass);
    }

    @Filter
    public static class EnvironmentFilter implements RequestFilter {
        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext.getCurrent().setSubject(subject);
        }
    }
}
