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
package com.codenvy.resource.api.license;

import com.codenvy.resource.shared.dto.AccountLicenseDto;
import com.codenvy.resource.shared.dto.ProvidedResourcesDto;
import com.codenvy.resource.shared.dto.ResourceDto;
import com.codenvy.resource.spi.impl.AccountLicenseImpl;
import com.jayway.restassured.response.Response;

import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.CheJsonProvider;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link AccountLicenseService}
 *
 * @author Sergii Leschenko
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class AccountLicenseServiceTest {
    @SuppressWarnings("unused") //is declared for deploying by everrest-assured
    private ApiExceptionMapper mapper;

    @SuppressWarnings("unused") //is declared for deploying by everrest-assured
    private CheJsonProvider jsonProvider = new CheJsonProvider(new HashSet<>());

    @Mock
    private AccountLicenseManager accountLicenseManager;

    @InjectMocks
    private AccountLicenseService service;

    @Test
    public void shouldGetLicense() throws Exception {
        //given
        final ResourceDto testResource = DtoFactory.newDto(ResourceDto.class)
                                                   .withType("test")
                                                   .withAmount(1234)
                                                   .withUnit("mb");

        final AccountLicenseDto toFetch = DtoFactory.newDto(AccountLicenseDto.class)
                                                    .withAccountId("account123")
                                                    .withResourcesDetails(singletonList(DtoFactory.newDto(ProvidedResourcesDto.class)
                                                                                           .withId("resource123")
                                                                                           .withProviderId("provider")
                                                                                           .withOwner("account123")
                                                                                           .withStartTime(123L)
                                                                                           .withEndTime(321L)
                                                                                           .withResources(singletonList(testResource))))
                                                    .withTotalResources(singletonList(testResource));

        //when
        when(accountLicenseManager.getByAccount(eq("account123"))).thenReturn(new AccountLicenseImpl(toFetch));

        //then
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .expect()
                                         .statusCode(200)
                                         .get(SECURE_PATH + "/license/account/account123");

        final AccountLicenseDto fetchedLicense = DtoFactory.getInstance().createDtoFromJson(response.body().print(), AccountLicenseDto.class);
        assertEquals(fetchedLicense, toFetch);
        verify(accountLicenseManager).getByAccount("account123");
    }
}
