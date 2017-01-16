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
package com.codenvy.im.service;

import com.codenvy.im.saas.SaasUserCredentials;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import org.everrest.assured.JettyHttpServer;
import org.mockito.Mock;

import javax.ws.rs.core.Response;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;

/**
 * @author Dmytro Nochevnov
 */
public class BaseContractTest {
    public static final String OK_RESPONSE_BODY = "{\n"
                                                  + "    \n"
                                                  + "}";
    @Mock
    public SaasUserCredentials saasUserCredentials;

    enum HttpMethod {GET, POST, PUT, DELETE}

    protected void testContract(String path,
                                Map<String, String> queryParameters,
                                String requestBody,
                                ContentType consumeContentType,
                                ContentType produceContentType,
                                HttpMethod httpMethod,
                                String expectedResponseBody,
                                Response.Status expectedResponseStatus,
                                Runnable doBeforeTest,
                                Runnable doAssertion) {

        if (doBeforeTest != null) {
            doBeforeTest.run();
        }

        RequestSpecification requestSpec = getRequestSpecification();
        com.jayway.restassured.response.Response response;

        if (queryParameters != null) {
            requestSpec.queryParameters(queryParameters);
        }

        if (requestBody != null) {
            requestSpec.body(requestBody);
        }

        if (consumeContentType != null) {
            requestSpec.contentType(consumeContentType);
        }

        switch (httpMethod) {
            case GET :
                response = requestSpec.get(getSecurePath(path));
                break;

            case PUT :
                response = requestSpec.put(getSecurePath(path));
                break;

            case POST :
                response = requestSpec.post(getSecurePath(path));
                break;

            case DELETE :
                response = requestSpec.delete(getSecurePath(path));
                break;

            default:
                throw new RuntimeException("Unknown HTTP method");
        }

        assertResponse(response, produceContentType, expectedResponseBody, expectedResponseStatus);

        if (doAssertion != null) {
            doAssertion.run();
        }
    }

    private void assertResponse(com.jayway.restassured.response.Response response,
                                ContentType PRODUCE_CONTENT_TYPE,
                                String RESPONSE_BODY,
                                Response.Status RESPONSE_STATUS) {
        assertEquals(response.statusCode(), RESPONSE_STATUS.getStatusCode());

        if (PRODUCE_CONTENT_TYPE != null) {
            assertEquals(response.getContentType(), PRODUCE_CONTENT_TYPE.toString());
        }

        if (RESPONSE_BODY != null) {
            assertEquals(response.getBody().prettyPrint(), RESPONSE_BODY);
        }
    }

    private String getSecurePath(String path) {
        return format("%s/%s", JettyHttpServer.SECURE_PATH, path);
    }

    private RequestSpecification getRequestSpecification() {
        return given()
            .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD)
            .when();
    }

}
