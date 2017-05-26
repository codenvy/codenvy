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
package com.codenvy.service.system;

import com.codenvy.api.permission.server.SystemDomain;
import com.google.common.collect.Sets;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.system.server.SystemService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.jayway.restassured.RestAssured.given;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.fail;

/**
 * Tests {@link SystemServicePermissionsFilter}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class SystemServicePermissionsFilterTest {

    private static final Set<String> TEST_HANDLED_METHODS = new HashSet<>(asList("stop",
                                                                                 "getState",
                                                                                 "getSystemRamLimitStatus"));


    @SuppressWarnings("unused")
    private static final SystemServicePermissionsFilter serviceFilter = new SystemServicePermissionsFilter();
    @SuppressWarnings("unused")
    private static final EnvironmentFilter              envFilter     = new EnvironmentFilter();

    @Mock
    private static Subject subject;

    @Mock
    private HostedSystemService systemService;

    @Test
    public void allPublicMethodsAreFiltered() {
        Set<String> existingMethods = getDeclaredPublicMethods(HostedSystemService.class);
        existingMethods.addAll(getDeclaredPublicMethods(SystemService.class));

        if (!existingMethods.equals(TEST_HANDLED_METHODS)) {
            Set<String> existingMinusExpected = Sets.difference(existingMethods, TEST_HANDLED_METHODS);
            Set<String> expectedMinusExisting = Sets.difference(TEST_HANDLED_METHODS, existingMethods);
            fail(format("The set of public methods tested by by the filter was changed.\n" +
                        "Methods present in service but not declared in test: '%s'\n" +
                        "Methods present in test but missing from service: '%s'",
                        existingMinusExpected,
                        expectedMinusExisting));
        }
    }

    @Test
    public void allowsStopForUserWithManageSystemPermission() throws Exception {
        permitSubject(SystemDomain.MANAGE_SYSTEM_ACTION);

        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .when()
               .post(SECURE_PATH + "/system/stop")
               .then()
               .statusCode(204);

        verify(systemService).stop();
    }

    @Test
    public void rejectsStopForUserWithoutManageSystemPermission() throws Exception {
        permitSubject("nothing");

        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .when()
               .post(SECURE_PATH + "/system/stop")
               .then()
               .statusCode(403);

        verify(systemService, never()).stop();
    }

    @Test
    public void allowsGetStateForUserWithManageSystemPermission() throws Exception {
        permitSubject(SystemDomain.MANAGE_SYSTEM_ACTION);

        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .when()
               .get(SECURE_PATH + "/system/state");

        verify(systemService).getState();
    }

    @Test
    public void rejectsGetStateForUserWithoutManageSystemPermission() throws Exception {
        permitSubject("nothing");

        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .when()
               .get(SECURE_PATH + "/system/state")
               .then()
               .statusCode(403);

        verify(systemService, never()).getState();
    }

    @Test
    public void allowsToGetSystemRamForAnyone() throws Exception {
        permitSubject("nothing");

        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .when()
               .get(SECURE_PATH + "/system/ram/limit");

        verify(systemService).getSystemRamLimitStatus();
    }

    private static void permitSubject(String... allowedActions) throws ForbiddenException {
        doAnswer(inv -> {
            if (!new HashSet<>(Arrays.asList(allowedActions)).contains(inv.getArguments()[2].toString())) {
                throw new ForbiddenException("Not allowed!");
            }
            return null;
        }).when(subject).checkPermission(anyObject(), anyObject(), anyObject());
    }

    private static Set<String> getDeclaredPublicMethods(Class<?> c) {
        return Arrays.stream(c.getDeclaredMethods())
                     .filter(m -> Modifier.isPublic(c.getModifiers()))
                     .map(Method::getName)
                     .collect(Collectors.toSet());
    }

    @Filter
    public static class EnvironmentFilter implements RequestFilter {
        @Override
        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext.getCurrent().setSubject(subject);
        }
    }
}
