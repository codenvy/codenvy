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
package com.codenvy.resource.api.usage;

import com.codenvy.resource.api.usage.ResourceUsageManager;
import com.codenvy.resource.api.usage.ResourceUsageService;
import com.codenvy.resource.shared.dto.ResourceDto;
import com.codenvy.resource.spi.impl.ResourceImpl;
import com.jayway.restassured.response.Response;

import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link ResourceUsageService}
 *
 * @author Sergii Leschenko
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class ResourceUsageServiceTest {
    private static final String RESOURCE_TYPE   = "test";
    private static final Long   RESOURCE_AMOUNT = 1000L;
    private static final String RESOURCE_UNIT   = "mb";

    @SuppressWarnings("unused") //is declared for deploying by everrest-assured
    private ApiExceptionMapper exceptionMapper;

    @Mock
    ResourceImpl resource;

    @Mock
    private ResourceUsageManager resourceUsageManager;

    @InjectMocks
    private ResourceUsageService service;

    @BeforeMethod
    public void setUp() throws Exception {
        when(resource.getType()).thenReturn(RESOURCE_TYPE);
        when(resource.getAmount()).thenReturn(RESOURCE_AMOUNT);
        when(resource.getUnit()).thenReturn(RESOURCE_UNIT);
    }

    @Test
    public void shouldReturnTotalResourcesForGivenAccount() throws Exception {
        doReturn(singletonList(resource)).when(resourceUsageManager).getTotalResources(any());

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/resource/account123");

        assertEquals(response.statusCode(), 200);
        verify(resourceUsageManager).getTotalResources(eq("account123"));
        final List<ResourceDto> resources = unwrapDtoList(response, ResourceDto.class);
        assertEquals(resources.size(), 1);
        final ResourceDto fetchedResource = resources.get(0);
        assertEquals(fetchedResource.getType(), RESOURCE_TYPE);
        assertEquals(new Long(fetchedResource.getAmount()), RESOURCE_AMOUNT);
        assertEquals(fetchedResource.getUnit(), RESOURCE_UNIT);
    }

    @Test
    public void shouldReturnUsedResourcesForGivenAccount() throws Exception {
        doReturn(singletonList(resource)).when(resourceUsageManager).getUsedResources(any());

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/resource/account123/used");

        assertEquals(response.statusCode(), 200);
        verify(resourceUsageManager).getUsedResources(eq("account123"));
        final List<ResourceDto> resources = unwrapDtoList(response, ResourceDto.class);
        assertEquals(resources.size(), 1);
        final ResourceDto fetchedResource = resources.get(0);
        assertEquals(fetchedResource.getType(), RESOURCE_TYPE);
        assertEquals(new Long(fetchedResource.getAmount()), RESOURCE_AMOUNT);
        assertEquals(fetchedResource.getUnit(), RESOURCE_UNIT);
    }

    @Test
    public void shouldReturnAvailableResourcesForGivenAccount() throws Exception {
        doReturn(singletonList(resource)).when(resourceUsageManager).getAvailableResources(any());

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/resource/account123/available");

        assertEquals(response.statusCode(), 200);
        verify(resourceUsageManager).getAvailableResources(eq("account123"));
        final List<ResourceDto> resources = unwrapDtoList(response, ResourceDto.class);
        assertEquals(resources.size(), 1);
        final ResourceDto fetchedResource = resources.get(0);
        assertEquals(fetchedResource.getType(), RESOURCE_TYPE);
        assertEquals(new Long(fetchedResource.getAmount()), RESOURCE_AMOUNT);
        assertEquals(fetchedResource.getUnit(), RESOURCE_UNIT);
    }

    private static <T> List<T> unwrapDtoList(Response response, Class<T> dtoClass) {
        return DtoFactory.getInstance().createListDtoFromJson(response.body().print(), dtoClass)
                         .stream()
                         .collect(toList());
    }
}
