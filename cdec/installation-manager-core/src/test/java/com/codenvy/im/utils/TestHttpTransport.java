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
package com.codenvy.im.utils;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.io.IOUtils;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.annotations.OPTIONS;
import org.eclipse.che.dto.server.JsonStringMapImpl;
import org.everrest.assured.EverrestJetty;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class TestHttpTransport {

    private static final String TEXT = "to be or not to be";

    private TestService   testService;
    private HttpTransport httpTransport;
    private HttpTransport spyHttpTransport;

    @Mock
    private HttpURLConnection httpURLConnection;

    @BeforeMethod
    public void setUp() throws Exception {
        testService = new TestService();
        httpTransport = new HttpTransport();
        spyHttpTransport = spy(new HttpTransport());

        doReturn(httpURLConnection).when(spyHttpTransport).getConnectionWithProxy(TEXT);
        doReturn(httpURLConnection).when(spyHttpTransport).getConnectionWithoutProxy(TEXT);
    }

    @Test
    public void testDoGet(ITestContext context) throws Exception {
        Object port = context.getAttribute(EverrestJetty.JETTY_PORT);
        Map value = Commons.asMap(httpTransport.doGet("http://localhost:" + port + "/rest/test/get"));

        assertNotNull(value);
        assertEquals(value.size(), 1);
        assertEquals(value.get("key"), "value");
    }

    @Test
    public void testDoGetTextPlain(ITestContext context) throws Exception {
        Object port = context.getAttribute(EverrestJetty.JETTY_PORT);
        String value = httpTransport.doGetTextPlain("http://localhost:" + port + "/rest/test/get-text-plain");

        assertNotNull(value);
        assertEquals(value, "value");
    }

    @Test
    public void testDoPost(ITestContext context) throws Exception {
        Object port = context.getAttribute(EverrestJetty.JETTY_PORT);
        Object body = new JsonStringMapImpl<>(ImmutableMap.of("a", "b"));
        Map value = Commons.asMap(httpTransport.doPost("http://localhost:" + port + "/rest/test/post", body, "token"));

        assertNotNull(value);
        assertEquals(value.size(), 1);
        assertEquals(value.get("a"), "b");
    }

    @Test
    public void testDoPostWithoutToken(ITestContext context) throws Exception {
        Object port = context.getAttribute(EverrestJetty.JETTY_PORT);
        Object body = new JsonStringMapImpl<>(ImmutableMap.of("a", "b"));
        Map value = Commons.asMap(httpTransport.doPost("http://localhost:" + port + "/rest/test/post", body));

        assertNotNull(value);
        assertEquals(value.size(), 1);
        assertEquals(value.get("a"), "b");
    }

    @Test
    public void testDoPostEmptyResponse(ITestContext context) throws Exception {
        Object port = context.getAttribute(EverrestJetty.JETTY_PORT);
        Object body = new JsonStringMapImpl<>(ImmutableMap.of("a", "b"));
        String response = httpTransport.doPost("http://localhost:" + port + "/rest/test/post-no-content", body, "token");
        assertTrue(response.isEmpty());
    }

    @Test
    public void testDownload(ITestContext context) throws Exception {
        java.nio.file.Path destDir = Paths.get("target", TestHttpTransport.class.getSimpleName(), "download");
        java.nio.file.Path destFile = destDir.resolve("temp");

        Object port = context.getAttribute(EverrestJetty.JETTY_PORT);
        httpTransport.download("http://localhost:" + port + "/rest/test/download", destDir, MediaType.APPLICATION_OCTET_STREAM);

        assertTrue(Files.exists(destFile));
        try (InputStream in = Files.newInputStream(destFile)) {
            assertEquals(IOUtils.toString(in), "content");
        }
    }

    @Test(expectedExceptions = HttpException.class, expectedExceptionsMessageRegExp = "[{]message:Not Found[}]")
    public void testFailDownload(ITestContext context) throws Exception {
        java.nio.file.Path destDir = Paths.get("target", "download");
        Object port = context.getAttribute(EverrestJetty.JETTY_PORT);

        httpTransport.download("http://localhost:" + port + "/rest/test/throwException", destDir, MediaType.APPLICATION_OCTET_STREAM);
    }

    @Test
    public void testDoOption(ITestContext context) throws Exception {
        Object port = context.getAttribute(EverrestJetty.JETTY_PORT);
        Map value = Commons.asMap(httpTransport.doOption("http://localhost:" + port + "/rest/test", null));

        assertNotNull(value);
        assertEquals(value.size(), 1);
        assertEquals(value.get("key"), "value");
    }

    @Test
    public void testDoDelete(ITestContext context) throws Exception {
        Object port = context.getAttribute(EverrestJetty.JETTY_PORT);
        httpTransport.doDelete("http://localhost:" + port + "/rest/test/delete", null);
    }


    @Test(expectedExceptions = HttpException.class, expectedExceptionsMessageRegExp = "Can't establish connection with http://1.1.1.1")
    public void testRequestFailedUnknownHost(ITestContext context) throws Exception {
        Object port = context.getAttribute(EverrestJetty.JETTY_PORT);
        httpTransport.doOption("http://1.1.1.1:" + port + "/rest/test", null);
    }

    @Test(expectedExceptions = HttpException.class)
    public void testRequestFailedWrongPath(ITestContext context) throws Exception {
        Object port = context.getAttribute(EverrestJetty.JETTY_PORT);
        httpTransport.doGet("http://localhost:" + port + "/rest/test/unknown");
    }

    @Test(dataProvider = "testConsideringAuthenticatedProxyData")
    public void testConsideringAuthenticatedHttpProxy(String username, String password) throws IOException {
        String httpPath = "http://localhost";

        if (username != null) {
            System.setProperty("http.proxyUser", username);
        }

        if (password != null) {
            System.setProperty("http.proxyPassword", password);
        }

        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        doReturn(new URL(httpPath)).when(mockConnection).getURL();
        doReturn(200).when(mockConnection).getResponseCode();

        doReturn(mockConnection).when(spyHttpTransport).openConnection(httpPath, null, false);

        spyHttpTransport.doGet(httpPath);
    }

    @DataProvider
    public Object[][] testConsideringAuthenticatedProxyData() {
        return new Object[][]{
            {null, null},
            {"", null},
            {null, ""},
            {null, "pass"},
            {"test", null},
            {"test", ""},
            {"test", "pass"}
        };
    }

    @Test
    public void testConsideringAuthenticatedHttpsProxy() throws IOException {
        String httpsPath = "https://localhost";

        System.setProperty("https.proxyUser", "user1");
        System.setProperty("https.proxyPassword", "passw1");

        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        doReturn(new URL(httpsPath)).when(mockConnection).getURL();
        doReturn(200).when(mockConnection).getResponseCode();

        doReturn(mockConnection).when(spyHttpTransport).openConnection(httpsPath, null, false);

        spyHttpTransport.doGet(httpsPath);
    }

    @Test
    public void shouldReturnConnectionWithProxy() throws IOException {
        spyHttpTransport.openConnection(TEXT, TEXT, false);

        verify(spyHttpTransport).getConnectionWithProxy(TEXT);
        verify(httpURLConnection).addRequestProperty(anyString(), anyString());
    }

    @Test
    public void shouldReturnConnectionWithoutProxy() throws IOException {
        spyHttpTransport.openConnection(TEXT, TEXT, true);

        verify(spyHttpTransport).getConnectionWithoutProxy(TEXT);
        verify(httpURLConnection).addRequestProperty(anyString(), anyString());
    }

    @AfterMethod
    public void tearDown() {
        System.clearProperty("http.proxyUser");
        System.clearProperty("http.proxyPassword");

        System.clearProperty("https.proxyUser");
        System.clearProperty("https.proxyPassword");
    }

    @Path("test")
    public class TestService {

        @DELETE
        @Path("delete")
        public Response delete() {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        @OPTIONS
        @Produces(MediaType.APPLICATION_JSON)
        public Response option() {
            Map<String, String> value = ImmutableMap.of("key", "value");
            return Response.status(Response.Status.OK).entity(new JsonStringMapImpl<>(value)).build();
        }

        @POST
        @Produces(MediaType.APPLICATION_JSON)
        @Consumes(MediaType.APPLICATION_JSON)
        @Path("post")
        public Response post(Map<String, String> body) {
            return Response.status(Response.Status.OK).entity(new JsonStringMapImpl<>(body)).build();
        }

        @POST
        @Produces(MediaType.APPLICATION_JSON)
        @Consumes(MediaType.APPLICATION_JSON)
        @Path("post-no-content")
        public Response postNoContent(Map<String, String> body) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Path("get")
        public Response get() {
            Map<String, String> value = ImmutableMap.of("key", "value");
            return Response.status(Response.Status.OK).entity(new JsonStringMapImpl<>(value)).build();
        }

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        @Path("get-text-plain")
        public Response getTextPlain() {
            String value = "value";
            return Response.status(Response.Status.OK).entity(value).build();
        }

        @GET
        @Path("download")
        @Produces(MediaType.APPLICATION_OCTET_STREAM)
        public Response download() throws IOException {
            java.nio.file.Path file = Paths.get("target", TestHttpTransport.class.getSimpleName(), "temp");
            Files.copy(new ByteArrayInputStream("content".getBytes()), file, StandardCopyOption.REPLACE_EXISTING);
            return Response.ok(file.toFile(), MediaType.APPLICATION_OCTET_STREAM)
                           .header("Content-Length", String.valueOf(Files.size(file)))
                           .header("Content-Disposition", "attachment; filename=" + file.getFileName().toString())
                           .build();
        }

        @GET
        @Path("throwException")
        @Produces(MediaType.APPLICATION_OCTET_STREAM)
        public Response doThrowException() throws ServerException {
            throw new ServerException("{message:Not Found}");
        }
    }
}
