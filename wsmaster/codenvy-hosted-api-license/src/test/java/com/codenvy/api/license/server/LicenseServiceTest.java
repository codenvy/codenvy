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

import com.codenvy.api.license.CodenvyLicense;
import com.codenvy.api.license.CodenvyLicenseFactory;
import com.codenvy.api.license.InvalidLicenseException;
import com.codenvy.api.license.LicenseException;
import com.codenvy.api.license.LicenseFeature;
import com.codenvy.api.license.LicenseNotFoundException;
import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.response.Response;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
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
public class LicenseServiceTest {

    @SuppressWarnings("unused")
    protected static ApiExceptionMapper MAPPER = new ApiExceptionMapper();
    @Mock
    private CodenvyLicenseManager licenseManager;
    @Mock
    private CodenvyLicense        mockCodenvyLicense;
    @Mock
    private CodenvyLicenseFactory mockLicenseFactory;

    @InjectMocks
    LicenseService licenseService;

    @Test
    public void testGetLicenseShouldReturnOk() throws Exception {
        doReturn(mockCodenvyLicense).when(licenseManager).load();
        doReturn("license").when(mockCodenvyLicense).getLicenseText();

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .get(JettyHttpServer.SECURE_PATH + "/license/system");

        assertEquals(response.statusCode(), OK.getStatusCode());
        assertEquals(response.asString(), "license");
    }

    @Test
    public void testGetLicenseShouldReturnNotFoundWhenFacadeThrowLicenseNotFoundException() throws Exception {
        doThrow(new LicenseNotFoundException("error")).when(licenseManager).load();

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .get(JettyHttpServer.SECURE_PATH + "/license/system");

        assertEquals(response.statusCode(), NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetLicenseShouldReturnServerErrorWhenFacadeThrowLicenseException() throws Exception {
        doThrow(new LicenseException("error")).when(licenseManager).load();

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
    public void testDeleteLicensShouldNotFindLicenseToDelete() {
        doThrow(new LicenseNotFoundException("error")).when(licenseManager).delete();

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .delete(JettyHttpServer.SECURE_PATH + "/license/system");

        assertEquals(response.statusCode(), NOT_FOUND.getStatusCode());
    }

    @Test
    public void testDeleteLicenseShouldReturnServerErrorWhenFacadeThrowLicenseException() throws Exception {
        doThrow(new LicenseException("error")).when(licenseManager).delete();

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .delete(JettyHttpServer.SECURE_PATH + "/license/system");

        assertEquals(response.statusCode(), INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testPostLicenseShouldReturnCreated() throws Exception {
        doReturn(mockCodenvyLicense).when(mockLicenseFactory).create(anyString());

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when().body("license")
                .post(JettyHttpServer.SECURE_PATH + "/license/system");

        assertEquals(response.statusCode(), CREATED.getStatusCode());
        verify(licenseManager).store(anyString());
    }

    @Test
    public void testPostLicenseShouldReturnServerErrorWhenFacadeThrowLicenseException() throws Exception {
        doThrow(new LicenseException("error")).when(licenseManager).store(anyString());

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when().body("license")
                .post(JettyHttpServer.SECURE_PATH + "/license/system");

        assertEquals(response.statusCode(), INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testGetLicensePropertiesShouldReturnOk() throws Exception {
        doReturn(mockCodenvyLicense).when(licenseManager).load();
        doReturn(ImmutableMap.of(LicenseFeature.TYPE, "type",
                                 LicenseFeature.EXPIRATION, "2015/10/10",
                                 LicenseFeature.USERS, "15")).when(mockCodenvyLicense).getFeatures();
        doReturn(Boolean.FALSE).when(mockCodenvyLicense).isExpired();

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .get(JettyHttpServer.SECURE_PATH + "/license/system/properties");

        assertEquals(response.statusCode(), OK.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> m = response.as(Map.class);

        assertEquals(m.size(), 4);
        assertEquals(m.get(LicenseFeature.TYPE.toString()), "type");
        assertEquals(m.get(LicenseFeature.EXPIRATION.toString()), "2015/10/10");
        assertEquals(m.get(LicenseFeature.USERS.toString()), "15");
        assertEquals(m.get(LicenseService.CODENVY_LICENSE_PROPERTY_IS_EXPIRED), "false");
    }

    @Test
    public void testGetLicensePropertiesShouldReturnNotFoundWhenLicenseNotFound() throws Exception {
        doThrow(new LicenseNotFoundException("error")).when(licenseManager).load();

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .get(JettyHttpServer.SECURE_PATH + "/license/system/properties");

        assertEquals(response.statusCode(), NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetLicensePropertiesShouldReturnConflictWhenLicenseInvalid() throws Exception {
        doThrow(new InvalidLicenseException("error")).when(licenseManager).load();

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .get(JettyHttpServer.SECURE_PATH + "/license/system/properties");

        assertEquals(response.statusCode(), CONFLICT.getStatusCode());
    }

    @Test
    public void testIsCodenvyUsageLegal() throws IOException, ServerException {
        doReturn(true).when(licenseManager).isCodenvyUsageLegal();
        Response response = given().when().get("/license/system/legality");

        assertEquals(response.statusCode(), OK.getStatusCode());
        assertEquals(response.asString(), "{\"value\":\"true\"}");
    }

    @Test
    public void testIsCodenvyUsageNotLegal() throws IOException, ServerException {
        doReturn(false).when(licenseManager).isCodenvyUsageLegal();

        Response response = given().when().get("/license/system/legality");

        assertEquals(response.statusCode(), OK.getStatusCode());
        assertEquals(response.asString(), "{\"value\":\"false\"}");
    }

    @Test
    public void testIsCodenvyUsageIOException() throws IOException, ServerException {
        doThrow(IOException.class).when(licenseManager).isCodenvyUsageLegal();

        Response response = given().when().get("/license/system/legality");
        assertEquals(response.statusCode(), INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testIsCodenvyActualNodesUsageLegal() throws IOException, ServerException {
        doReturn(true).when(licenseManager).isCodenvyNodesUsageLegal(null);
        Response response = given().when().get("/license/system/legality/node");

        assertEquals(response.statusCode(), OK.getStatusCode());
        assertEquals(response.asString(), "{\"value\":\"true\"}");
    }

    @Test
    public void testIsCodenvyGivenNodesUsageLegal() throws IOException, ServerException {
        doReturn(true).when(licenseManager).isCodenvyNodesUsageLegal(2);
        Response response = given().when().get("/license/system/legality/node?nodeNumber=2");

        assertEquals(response.statusCode(), OK.getStatusCode());
        assertEquals(response.asString(), "{\"value\":\"true\"}");
    }

    @Test
    public void testIsCodenvyNodesUsageNotLegal() throws IOException, ServerException {
        doReturn(false).when(licenseManager).isCodenvyNodesUsageLegal(2);
        Response response = given().when().get("/license/system/legality/node?nodeNumber=2");

        assertEquals(response.statusCode(), OK.getStatusCode());
        assertEquals(response.asString(), "{\"value\":\"false\"}");
    }

    @Test
    public void testIsCodenvyNodesUsageIOException() throws IOException, ServerException {
        doThrow(IOException.class).when(licenseManager).isCodenvyNodesUsageLegal(null);

        Response response = given().when().get("/license/system/legality/node");
        assertEquals(response.statusCode(), INTERNAL_SERVER_ERROR.getStatusCode());
    }

}
