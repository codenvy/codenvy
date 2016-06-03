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

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.MachineService;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.resource.GenericMethodResource;
import org.everrest.core.uri.UriPattern;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.Path;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static com.codenvy.api.workspace.server.WorkspaceDomain.DOMAIN_ID;
import static com.codenvy.api.workspace.server.WorkspaceDomain.RUN;
import static com.codenvy.api.workspace.server.WorkspaceDomain.USE;
import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link MachinePermissionsFilter}.
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class MachinePermissionsFilterTest {
    @SuppressWarnings("unused")
    private static final EnvironmentFilter FILTER = new EnvironmentFilter();

    @Mock
    MachineManager machineManager;

    @Mock
    MachineImpl machine;

    @Mock
    SnapshotImpl snapshot;

    @SuppressWarnings("unused")
    @InjectMocks
    MachinePermissionsFilter permissionsFilter;

    @Mock
    private static Subject subject;

    @Mock
    MachineService service;

    @BeforeMethod
    public void setUp() throws Exception {
        when(machine.getId()).thenReturn("machine123");
        when(machine.getWorkspaceId()).thenReturn("workspace123");

        when(machineManager.getMachine(eq("machine123"))).thenReturn(machine);

        when(snapshot.getId()).thenReturn("snapshot123");
        when(snapshot.getWorkspaceId()).thenReturn("workspace123");
        when(machineManager.getSnapshot(eq("snapshot123"))).thenReturn(snapshot);

        MachineImpl targetMachine = mock(MachineImpl.class);
        when(targetMachine.getId()).thenReturn("machine321");
        when(targetMachine.getWorkspaceId()).thenReturn("workspace321");

        when(machineManager.getMachine(eq("machine321"))).thenReturn(targetMachine);
    }

    @Test
    public void shouldCheckPermissionsOnGettingMachineById() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/machine/machine123");

        assertEquals(response.getStatusCode(), 204);
        verify(service).getMachineById(eq("machine123"));
        verify(subject).checkPermission(DOMAIN_ID, "workspace123", USE);
    }

    @Test
    public void shouldCheckPermissionsOnGettingMachines() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/machine/?workspace=workspace123");

        assertEquals(response.getStatusCode(), 200);
        verify(service).getMachines(eq("workspace123"));
        verify(subject).checkPermission(DOMAIN_ID, "workspace123", USE);
    }

    @Test
    public void shouldCheckPermissionsOnGettingSnapshots() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/machine/snapshot?workspace=workspace123");

        assertEquals(response.getStatusCode(), 200);
        verify(service).getSnapshots(eq("workspace123"));
        verify(subject).checkPermission(DOMAIN_ID, "workspace123", USE);
    }

    @Test
    public void shouldCheckPermissionsOnSnapshotSaving() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/machine/machine123/snapshot");

        assertEquals(response.getStatusCode(), 204);
        verify(service).saveSnapshot(eq("machine123"), any());
        verify(subject).checkPermission(DOMAIN_ID, "workspace123", RUN);
    }

    @Test
    public void shouldCheckPermissionsOnSnapshotRemoving() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .delete(SECURE_PATH + "/machine/snapshot/snapshot123");

        assertEquals(response.getStatusCode(), 204);
        verify(service).removeSnapshot(eq("snapshot123"));
        verify(subject).checkPermission(DOMAIN_ID, "workspace123", RUN);
    }

    @Test
    public void shouldCheckPermissionsOnCommandExecuting() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/machine/machine123/command");

        assertEquals(response.getStatusCode(), 204);
        verify(service).executeCommandInMachine(eq("machine123"), any(), anyString());
        verify(subject).checkPermission(DOMAIN_ID, "workspace123", USE);
    }

    @Test
    public void shouldCheckPermissionsOnProcessesGetting() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/machine/machine123/process");

        assertEquals(response.getStatusCode(), 200);
        verify(service).getProcesses(eq("machine123"));
        verify(subject).checkPermission(DOMAIN_ID, "workspace123", USE);
    }

    @Test
    public void shouldCheckPermissionsOnProcessStopping() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .delete(SECURE_PATH + "/machine/machine123/process/123");

        assertEquals(response.getStatusCode(), 204);
        verify(service).stopProcess(eq("machine123"), eq(123));
        verify(subject).checkPermission(DOMAIN_ID, "workspace123", USE);
    }

    @Test
    public void shouldCheckPermissionsOnMachineLogsGetting() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/machine/machine123/logs");

        assertEquals(response.getStatusCode(), 204);
        verify(service).getMachineLogs(eq("machine123"), any());
        verify(subject).checkPermission(DOMAIN_ID, "workspace123", USE);
    }

    @Test
    public void shouldCheckPermissionsOnProcessLogsGetting() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/machine/machine123/process/123/logs");

        assertEquals(response.getStatusCode(), 204);
        verify(service).getProcessLogs(eq("machine123"), eq(123), any());
        verify(subject).checkPermission(DOMAIN_ID, "workspace123", USE);
    }

    @Test
    public void shouldCheckPermissionsOnFileContentGetting() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/machine/machine123/filepath/text.txt");

        assertEquals(response.getStatusCode(), 204);
        verify(service).getFileContent(eq("machine123"), eq("text.txt"), anyInt(), anyInt());
        verify(subject).checkPermission(DOMAIN_ID, "workspace123", USE);
    }

    @Test
    public void shouldCheckPermissionsOnFilesCopying() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/machine/copy?sourceMachineId=machine123&targetMachineId=machine321");

        assertEquals(response.getStatusCode(), 204);
        verify(service).copyFilesBetweenMachines(eq("machine123"), eq("machine321"), anyString(), anyString(), anyBoolean());
        verify(subject).checkPermission(DOMAIN_ID, "workspace123", USE);
        verify(subject).checkPermission(DOMAIN_ID, "workspace321", USE);
    }

    @Test
    public void shouldSkipMachineTokenMethod() throws Exception {
        String pathValue = permissionsFilter.getClass().getAnnotation(Path.class).value();
        UriPattern pattern = new UriPattern(pathValue);
        assertTrue(pattern.match("/machine/anything/any_value", new ArrayList<>()));
        assertFalse(pattern.match("/machine/token/any_value", new ArrayList<>()));
    }

    @Test(expectedExceptions = ForbiddenException.class,
          expectedExceptionsMessageRegExp = "The user does not have permission to perform this operation")
    public void shouldThrowForbiddenExceptionWhenRequestedUnknownMethod() throws Exception {
        final GenericMethodResource mock = mock(GenericMethodResource.class);
        Method injectLinks = MachineService.class.getMethod("getServiceDescriptor");
        when(mock.getMethod()).thenReturn(injectLinks);

        permissionsFilter.filter(mock, new Object[] {});
    }

    @Test(dataProvider = "coveredPaths")
    public void shouldThrowForbiddenExceptionWhenUserDoesNotHavePermissionsForPerformOperation(String path,
                                                                                               String method,
                                                                                               String action) throws Exception {

        when(subject.hasPermission(anyString(), anyString(), anyString())).thenReturn(false);
        doThrow(new ForbiddenException("The user does not have permission to " + action + " workspace with id 'workspace123'"))
                .when(subject).checkPermission(anyString(), anyString(), anyString());

        Response response = request(given().auth()
                                           .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                           .contentType("application/json")
                                           .when(),
                                    SECURE_PATH + path,
                                    method);

        assertEquals(response.getStatusCode(), 403);
        assertEquals(unwrapError(response), "The user does not have permission to " + action + " workspace with id 'workspace123'");

        verifyZeroInteractions(service);
    }

    @DataProvider(name = "coveredPaths")
    public Object[][] pathsProvider() {
        return new Object[][] {
                {"/machine/machine123", "get", USE},
                {"/machine?workspace=workspace123", "get", USE},
                {"/machine/snapshot?workspace=workspace123", "get", USE},
                {"/machine/machine123/snapshot", "post", RUN},
                {"/machine/snapshot/snapshot123", "delete", USE},
                {"/machine/machine123/command", "post", USE},
                {"/machine/machine123/process", "get", USE},
                {"/machine/machine123/process/123", "delete", USE},
                {"/machine/machine123/logs", "get", USE},
                {"/machine/machine123/process/1/logs", "get", USE},
                {"/machine/machine123/filepath/test.txt", "get", USE},
                {"/machine/copy?sourceMachineId=machine123&targetMachineId=machine321", "post", USE}
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
