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
package com.codenvy.ldap.sync;

import com.jayway.restassured.response.Response;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

/**
 * Tests {@link LdapSynchronizerService} and {@link LdapSynchronizerPermissionsFilter}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class LdapSynchronizerServiceTest {

    @SuppressWarnings("unused")
    private static LdapSynchronizerPermissionsFilter PERMISSIONS_FILTER = new LdapSynchronizerPermissionsFilter();
    @SuppressWarnings("unused")
    private static EnvironmentFilter                 ENVIRONMENT_FILTER = new EnvironmentFilter();
    @SuppressWarnings("unused")
    private static ApiExceptionMapper                EX_MAPPER          = new ApiExceptionMapper();

    @Mock
    private static SubjectImpl SUBJECT;

    @Mock
    private LdapSynchronizer synchronizer;

    @InjectMocks
    private LdapSynchronizerService syncService;

    @Test
    public void shouldCallSync() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .when()
               .post(SECURE_PATH + "/ldap/sync");

        verify(synchronizer).syncAllAsynchronously();
    }

    @Test
    public void shouldReturn409WhenSyncCouldNotBePerformedDueToOngoingOne() throws Exception {
        doThrow(new SyncException("test")).when(synchronizer).syncAllAsynchronously();

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .post(SECURE_PATH + "/ldap/sync");

        assertEquals(response.getStatusCode(), 409);
        verify(synchronizer).syncAllAsynchronously();
    }

    @Test
    public void shouldRejectSynchronizationIfUserPermissionsCheckFails() throws Exception {
        doThrow(new ForbiddenException("test")).when(SUBJECT).checkPermission(anyString(), anyString(), anyString());

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .post(SECURE_PATH + "/ldap/sync");

        assertEquals(response.getStatusCode(), 403);
        verify(synchronizer, never()).syncAllAsynchronously();
    }

    @Filter
    public static class EnvironmentFilter implements RequestFilter {
        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext.getCurrent().setSubject(SUBJECT);
        }
    }
}
