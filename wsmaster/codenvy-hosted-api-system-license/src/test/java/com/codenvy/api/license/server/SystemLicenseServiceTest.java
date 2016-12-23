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
package com.codenvy.api.license.server;

import com.codenvy.api.license.shared.dto.IssueDto;
import com.codenvy.api.license.shared.dto.LegalityDto;
import com.codenvy.api.license.shared.model.Issue;
import com.codenvy.api.license.SystemLicense;
import com.codenvy.api.license.SystemLicenseFactory;
import com.codenvy.api.license.exception.InvalidSystemLicenseException;
import com.codenvy.api.license.exception.SystemLicenseException;
import com.codenvy.api.license.SystemLicenseFeature;
import com.codenvy.api.license.exception.SystemLicenseNotFoundException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.response.Response;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.assured.JettyHttpServer;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 * @author Alexander Andrienko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class SystemLicenseServiceTest {

    public final static IssueDto issue = newDto(IssueDto.class).withStatus(Issue.Status.USER_LICENSE_HAS_REACHED_ITS_LIMIT)
                                           .withMessage("error message");

    @SuppressWarnings("unused")
    protected static ApiExceptionMapper MAPPER = new ApiExceptionMapper();
    @Mock
    private SystemLicenseManager                 licenseManager;
    @Mock
    private SystemLicense                        mockSystemLicense;
    @Mock
    private SystemLicenseFactory                 mockLicenseFactory;

    @InjectMocks
    SystemLicenseService systemLicenseService;

    @Test
    public void testGetLicenseShouldReturnOk() throws Exception {
        doReturn(mockSystemLicense).when(licenseManager).load();
        doReturn("license").when(mockSystemLicense).getLicenseText();

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .get(JettyHttpServer.SECURE_PATH + "/license/system");

        assertEquals(response.statusCode(), OK.getStatusCode());
        assertEquals(response.asString(), "license");
    }

    @Test
    public void testGetLicenseShouldReturnNotFoundWhenFacadeThrowLicenseNotFoundException() throws Exception {
        doThrow(new SystemLicenseNotFoundException("error")).when(licenseManager).load();

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .get(JettyHttpServer.SECURE_PATH + "/license/system");

        assertEquals(response.statusCode(), NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetLicenseShouldReturnServerErrorWhenFacadeThrowLicenseException() throws Exception {
        doThrow(new SystemLicenseException("error")).when(licenseManager).load();

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .get(JettyHttpServer.SECURE_PATH + "/license/system");

        assertEquals(response.statusCode(), INTERNAL_SERVER_ERROR.getStatusCode());
    }


    @Test
    public void testDeleteLicenseShouldReturnOk() throws Exception {
        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .delete(JettyHttpServer.SECURE_PATH + "/license/system");

        assertEquals(response.statusCode(), NO_CONTENT.getStatusCode());
    }

    @Test
    public void testDeleteLicensShouldNotFindLicenseToDelete() throws Exception {
        doThrow(new SystemLicenseNotFoundException("error")).when(licenseManager).delete();

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .delete(JettyHttpServer.SECURE_PATH + "/license/system");

        assertEquals(response.statusCode(), NOT_FOUND.getStatusCode());
    }

    @Test
    public void testDeleteLicenseShouldReturnServerErrorWhenFacadeThrowLicenseException() throws Exception {
        doThrow(new SystemLicenseException("error")).when(licenseManager).delete();

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .delete(JettyHttpServer.SECURE_PATH + "/license/system");

        assertEquals(response.statusCode(), INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testPostLicenseShouldReturnCreated() throws Exception {
        doReturn(mockSystemLicense).when(mockLicenseFactory).create(anyString());

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when().body("license")
                .post(JettyHttpServer.SECURE_PATH + "/license/system");

        assertEquals(response.statusCode(), CREATED.getStatusCode());
        verify(licenseManager).store(anyString());
    }

    @Test
    public void testPostLicenseShouldReturnServerErrorWhenFacadeThrowLicenseException() throws Exception {
        doThrow(new SystemLicenseException("error")).when(licenseManager).store(anyString());

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when().body("license")
                .post(JettyHttpServer.SECURE_PATH + "/license/system");

        assertEquals(response.statusCode(), INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testGetLicensePropertiesShouldReturnOk() throws Exception {
        doReturn(mockSystemLicense).when(licenseManager).load();
        doReturn(ImmutableMap.of(SystemLicenseFeature.TYPE, "type",
                                 SystemLicenseFeature.EXPIRATION, "2015/10/10",
                                 SystemLicenseFeature.USERS, "15")).when(mockSystemLicense).getFeatures();
        doReturn(Boolean.FALSE).when(mockSystemLicense).isExpiredCompletely();

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .get(JettyHttpServer.SECURE_PATH + "/license/system/properties");

        assertEquals(response.statusCode(), OK.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> m = response.as(Map.class);

        assertEquals(m.size(), 4);
        assertEquals(m.get(SystemLicenseFeature.TYPE.toString()), "type");
        assertEquals(m.get(SystemLicenseFeature.EXPIRATION.toString()), "2015/10/10");
        assertEquals(m.get(SystemLicenseFeature.USERS.toString()), "15");
        assertEquals(m.get(SystemLicenseService.CODENVY_LICENSE_PROPERTY_IS_EXPIRED), "false");
    }

    @Test
    public void testGetLicensePropertiesShouldReturnNotFoundWhenLicenseNotFound() throws Exception {
        doThrow(new SystemLicenseNotFoundException("error")).when(licenseManager).load();

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .get(JettyHttpServer.SECURE_PATH + "/license/system/properties");

        assertEquals(response.statusCode(), NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetLicensePropertiesShouldReturnConflictWhenLicenseInvalid() throws Exception {
        doThrow(new InvalidSystemLicenseException("error")).when(licenseManager).load();

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .get(JettyHttpServer.SECURE_PATH + "/license/system/properties");

        assertEquals(response.statusCode(), CONFLICT.getStatusCode());
    }

    @Test
    public void testisSystemUsageLegal() throws IOException, ServerException, ConflictException {
        doReturn(true).when(licenseManager).isSystemUsageLegal();
        doReturn(ImmutableList.of()).when(licenseManager).getLicenseIssues();

        Response response = given().when().get("/license/system/legality");

        assertEquals(response.statusCode(), OK.getStatusCode());
        assertEquals(getLegalityDtoFromJson(response),
                     newDto(LegalityDto.class).withIsLegal(true));
    }

    @Test
    public void testIsCodenvyUsageNotLegal() throws IOException, ServerException, ConflictException {
        doReturn(false).when(licenseManager).isSystemUsageLegal();
        doReturn(ImmutableList.of(issue)).when(licenseManager).getLicenseIssues();

        Response response = given().when().get("/license/system/legality");

        assertEquals(response.statusCode(), OK.getStatusCode());
        assertEquals(getLegalityDtoFromJson(response),
                     newDto(LegalityDto.class).withIsLegal(false)
                                              .withIssues(ImmutableList.of(issue)));
    }

    @Test
    public void testIsCodenvyUsageIOException() throws IOException, ServerException {
        doThrow(IOException.class).when(licenseManager).isSystemUsageLegal();

        Response response = given().when().get("/license/system/legality");
        assertEquals(response.statusCode(), INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testIsCodenvyActualNodesUsageLegal() throws IOException, ServerException {
        doReturn(true).when(licenseManager).isSystemNodesUsageLegal(null);
        Response response = given().when().get("/license/system/legality/node");

        assertEquals(response.statusCode(), OK.getStatusCode());
        assertEquals(getLegalityDtoFromJson(response),
                     newDto(LegalityDto.class).withIsLegal(true));
    }

    @Test
    public void testIsCodenvyGivenNodesUsageLegal() throws IOException, ServerException {
        doReturn(true).when(licenseManager).isSystemNodesUsageLegal(2);
        Response response = given().when().get("/license/system/legality/node?nodeNumber=2");

        assertEquals(response.statusCode(), OK.getStatusCode());
        assertEquals(getLegalityDtoFromJson(response),
                     newDto(LegalityDto.class).withIsLegal(true));
    }

    @Test
    public void testIsCodenvyNodesUsageNotLegal() throws IOException, ServerException {
        doReturn(false).when(licenseManager).isSystemNodesUsageLegal(2);
        Response response = given().when().get("/license/system/legality/node?nodeNumber=2");

        assertEquals(response.statusCode(), OK.getStatusCode());
        assertEquals(getLegalityDtoFromJson(response),
                     newDto(LegalityDto.class).withIsLegal(false));
    }

    @Test
    public void testIsCodenvyNodesUsageIOException() throws IOException, ServerException {
        doThrow(IOException.class).when(licenseManager).isSystemNodesUsageLegal(null);

        Response response = given().when().get("/license/system/legality/node");
        assertEquals(response.statusCode(), INTERNAL_SERVER_ERROR.getStatusCode());
    }

    private LegalityDto getLegalityDtoFromJson(Response response) {
        return DtoFactory.getInstance().createDtoFromJson(response.asString(), LegalityDto.class);
    }

}
