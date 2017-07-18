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
package com.codenvy.api.invite;

import com.codenvy.auth.sso.server.EmailValidator;
import com.codenvy.shared.invite.dto.InviteDto;
import com.jayway.restassured.response.Response;

import org.eclipse.che.api.core.Page;
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
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link InviteService}.
 *
 * @author Sergii Leschenko
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class InviteServiceTest {
    private static final String EMAIL_TO_INVITE = "userok@test.com";

    @SuppressWarnings("unused") //is declared for deploying by everrest-assured
    private ApiExceptionMapper mapper;

    @SuppressWarnings("unused") //is declared for deploying by everrest-assured
    private CheJsonProvider jsonProvider = new CheJsonProvider(new HashSet<>());

    @Mock
    private InviteManager inviteManager;

    @Mock
    private EmailValidator emailValidator;

    @InjectMocks
    private InviteService service;

    @Test
    public void shouldStoreInvite() throws Exception {
        final InviteDto toStore = createInvite();

        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .contentType("application/json")
               .body(toStore)
               .when()
               .expect().statusCode(204)
               .post(SECURE_PATH + "/invite");

        verify(inviteManager).store(eq(toStore));
        verify(emailValidator).validateUserMail(toStore.getEmail());
    }

    @Test
    public void shouldRespond400OnStoringNullInvite() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .contentType("application/json")
               .when()
               .expect().statusCode(400)
               .post(SECURE_PATH + "/invite");
    }

    @Test
    public void shouldRespond400OnStoringInviteWithNullDomainId() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .contentType("application/json")
               .body(createInvite().withDomainId(null))
               .when()
               .expect().statusCode(400)
               .post(SECURE_PATH + "/invite");
    }

    @Test
    public void shouldRespond400OnStoringInviteWithNullInstanceId() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .contentType("application/json")
               .body(createInvite().withInstanceId(null))
               .when()
               .expect().statusCode(400)
               .post(SECURE_PATH + "/invite");
    }

    @Test
    public void shouldRespond400OnStoringInviteWithNullEmail() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .contentType("application/json")
               .body(createInvite().withEmail(null))
               .when()
               .expect().statusCode(400)
               .post(SECURE_PATH + "/invite");
    }

    @Test
    public void shouldRespond400OnStoringInviteWithEmptyActionsList() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .contentType("application/json")
               .body(createInvite().withActions(emptyList()))
               .when()
               .expect().statusCode(400)
               .post(SECURE_PATH + "/invite");
    }

    @Test
    public void shouldReturnInvitesByInstance() throws Exception {
        InviteDto invite = createInvite();
        doReturn(new Page<>(Collections.singletonList(invite), 2, 1, 3))
                .when(inviteManager).getInvites(anyString(), anyString(), anyLong(), anyInt());

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .contentType("application/json")
                                   .when()
                                   .expect().statusCode(200)
                                   .get(SECURE_PATH + "/invite/organization?instance=org123&skipCount=2&maxItems=1");

        List<InviteDto> result = unwrapDtoList(response, InviteDto.class);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0), invite);
        verify(inviteManager).getInvites("organization", "org123", 2, 1);
    }

    @Test
    public void shouldRespond400OnFetchingInvitesWithoutNullInstance() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .when()
               .expect().statusCode(400)
               .get(SECURE_PATH + "/invite/organization");
    }

    @Test
    public void shouldRespond400OnFetchingInvitesWithNegativeSkipCount() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .when()
               .expect().statusCode(400)
               .get(SECURE_PATH + "/invite/organization?instance=org123&skipCount=-1");
    }

    @Test
    public void shouldRespond400OnFetchingInvitesWithNegativeMaxItems() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .when()
               .expect().statusCode(400)
               .get(SECURE_PATH + "/invite/organization?instance=org123&maxItems=-1");
    }

    @Test
    public void shouldRemoveInvite() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .when()
               .expect().statusCode(204)
               .delete(SECURE_PATH + "/invite/organization?instance=org123&email=user@test.com");

        verify(inviteManager).remove("organization", "org123", "user@test.com");
    }

    @Test
    public void shouldRespond400OnRemovingInvitesWithNullInstance() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .when()
               .expect().statusCode(400)
               .delete(SECURE_PATH + "/invite/organization?email=user@test.com");
    }

    @Test
    public void shouldRespond400OnRemovingInvitesWithNullEmail() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .when()
               .expect().statusCode(400)
               .delete(SECURE_PATH + "/invite/organization?instance=org123");
    }

    private static <T> List<T> unwrapDtoList(Response response, Class<T> dtoClass) {
        return DtoFactory.getInstance().createListDtoFromJson(response.body().print(), dtoClass)
                         .stream()
                         .collect(toList());
    }

    private InviteDto createInvite() {
        return DtoFactory.newDto(InviteDto.class)
                         .withEmail(EMAIL_TO_INVITE)
                         .withDomainId("test")
                         .withInstanceId("test123")
                         .withActions(asList("read", "update"));
    }
}
