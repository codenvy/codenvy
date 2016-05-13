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
package com.codenvy.api.user.server;

import com.codenvy.api.user.server.dao.AdminUserDao;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.shared.dto.UserDescriptor;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.subject.Subject;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.impl.uri.UriBuilderImpl;
import org.everrest.core.tools.DependencySupplierImpl;
import org.everrest.core.tools.ResourceLauncher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link AdminUserService}
 *
 * @author Anatoliy Bazko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class AdminUserServiceTest {

    private final String BASE_URI     = "http://localhost/service";
    private final String SERVICE_PATH = BASE_URI + "/admin/user";

    @Mock
    AdminUserDao       userDao;
    @Mock
    UriInfo            uriInfo;
    @Mock
    EnvironmentContext environmentContext;
    @Mock
    SecurityContext    securityContext;

    AdminUserService userService;

    ResourceLauncher launcher;

    @BeforeMethod
    public void setUp() throws Exception {
        ResourceBinderImpl resources = new ResourceBinderImpl();
        DependencySupplierImpl dependencies = new DependencySupplierImpl();
        dependencies.addComponent(AdminUserDao.class, userDao);

        userService = new AdminUserService(userDao);
        final Field uriField = userService.getClass()
                                          .getSuperclass()
                                          .getDeclaredField("uriInfo");
        uriField.setAccessible(true);
        uriField.set(userService, uriInfo);

        resources.addResource(userService, null);

        EverrestProcessor processor = new EverrestProcessor(resources,
                                                            new ApplicationProviderBinder(),
                                                            dependencies,
                                                            new EverrestConfiguration(),
                                                            null);
        launcher = new ResourceLauncher(processor);
        ProviderBinder providerBinder = ProviderBinder.getInstance();
        providerBinder.addExceptionMapper(ApiExceptionMapper.class);
        ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, providerBinder));
        //set up user
        final User user = createUser();
        when(environmentContext.get(SecurityContext.class)).thenReturn(securityContext);

        when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());
        when(uriInfo.getRequestUri()).thenReturn(URI.create(SERVICE_PATH));

        org.eclipse.che.commons.env.EnvironmentContext.getCurrent().setSubject(new Subject() {

            @Override
            public String getUserName() {
                return user.getEmail();
            }

            @Override
            public boolean isMemberOf(String s) {
                return false;
            }

            @Override
            public boolean hasPermission(String domain, String instance, String action) {
                return false;
            }

            @Override
            public void checkPermission(String domain, String instance, String action) throws ForbiddenException {
            }

            @Override
            public String getToken() {
                return null;
            }

            @Override
            public String getUserId() {
                return user.getId();
            }

            @Override
            public boolean isTemporary() {
                return false;
            }
        });
    }

    private User createUser() throws NotFoundException, ServerException {
        final User testUser = new User().withId("test_id").withEmail("test@email");
        when(userDao.getAll(anyInt(), anyInt())).thenReturn(new Page<>(singletonList(testUser), 0, 1, 1));
        return testUser;
    }

    @Test
    public void shouldReturnAllUsers() throws Exception {
        final ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "?maxItems=3&skipCount=4", null);

        assertEquals(response.getStatus(), OK.getStatusCode());
        verify(userDao).getAll(3, 4);

        @SuppressWarnings("unchecked")
        List<UserDescriptor> users = (List<UserDescriptor>)response.getEntity();
        assertEquals(users.size(), 1);
    }

    @Test
    public void shouldThrowServerErrorIfDaoThrowException() throws Exception {
        when(userDao.getAll(anyInt(), anyInt())).thenThrow(new ServerException("some error"));
        final ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH, null);

        assertEquals(response.getStatus(), INTERNAL_SERVER_ERROR.getStatusCode());
    }

    private ContainerResponse makeRequest(String method, String path, Object entity) throws Exception {
        Map<String, List<String>> headers = null;
        byte[] data = null;
        if (entity != null) {
            headers = new HashMap<>();
            headers.put(HttpHeaders.CONTENT_TYPE, singletonList(MediaType.APPLICATION_JSON));
            data = JsonHelper.toJson(entity).getBytes();
        }
        return launcher.service(method, path, BASE_URI, headers, data, null, environmentContext);
    }
}
