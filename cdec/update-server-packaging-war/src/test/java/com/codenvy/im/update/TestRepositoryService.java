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
package com.codenvy.im.update;

import com.codenvy.im.artifacts.ArtifactProperties;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.artifacts.InstallManagerArtifact;
import com.codenvy.im.artifacts.VersionLabel;
import com.codenvy.im.event.Event;
import com.codenvy.im.event.EventFactory;
import com.codenvy.im.event.EventLogger;
import com.codenvy.im.saas.SaasUserServiceProxy;
import com.codenvy.im.utils.Commons;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.MailUtil;
import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.response.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.everrest.assured.EverrestJetty;
import org.everrest.assured.JettyHttpServer;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.codenvy.im.artifacts.ArtifactProperties.ARTIFACT_PROPERTY;
import static com.codenvy.im.artifacts.ArtifactProperties.AUTHENTICATION_REQUIRED_PROPERTY;
import static com.codenvy.im.artifacts.ArtifactProperties.BUILD_TIME_PROPERTY;
import static com.codenvy.im.artifacts.ArtifactProperties.FILE_NAME_PROPERTY;
import static com.codenvy.im.artifacts.ArtifactProperties.LABEL_PROPERTY;
import static com.codenvy.im.artifacts.ArtifactProperties.MD5_PROPERTY;
import static com.codenvy.im.artifacts.ArtifactProperties.SUBSCRIPTION_PROPERTY;
import static com.codenvy.im.artifacts.ArtifactProperties.VERSION_PROPERTY;
import static com.jayway.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatoliy Bazko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class TestRepositoryService extends BaseTest {

    public static final javax.ws.rs.core.Response OK_RESPONSE = javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.OK).build();
    public static final javax.ws.rs.core.Response NOT_FOUND_RESPONSE = javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
    public static final String TEST_USER_ID = "id";
    public static final String ANY_NAME     = "any_name";
    private ArtifactStorage   artifactStorage;
    private RepositoryService repositoryService;

    @Mock
    private HttpTransport mockHttpTransport;

    @Mock
    private UserManager mockUserManager;

    @Mock
    private MailUtil mockMailUtil;

    @Mock
    private EventLogger mockEventLogger;

    private final Properties authenticationRequiredProperties = new Properties() {{
        put(AUTHENTICATION_REQUIRED_PROPERTY, "true");
    }};
    private SaasUserServiceProxy    saasUserServiceProxy;

    @Override
    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        saasUserServiceProxy = new SaasUserServiceProxy("", mockHttpTransport);
        artifactStorage = new ArtifactStorage(DOWNLOAD_DIRECTORY.toString());
        repositoryService = new RepositoryService("",
                                                  mockUserManager,
                                                  artifactStorage,
                                                  mockHttpTransport,
                                                  mockMailUtil,
                                                  saasUserServiceProxy,
                                                  mockEventLogger);

        when(mockUserManager.getCurrentUser()).thenReturn(new SubjectImpl("name", TEST_USER_ID, "token", Collections.<String>emptyList(), false));
        super.setUp();
    }

    @Test
    public void testGetPropertiesOfLatestVersion() throws Exception {
        artifactStorage.upload(new ByteArrayInputStream("content".getBytes()), InstallManagerArtifact.NAME, "1.0.2", "tmp", new Properties());

        Response response = given().when().get("repository/properties/" + InstallManagerArtifact.NAME);
        assertEquals(response.statusCode(), OK_RESPONSE.getStatus());

        Map value = Commons.asMap(response.body().asString());

        assertNotNull(value);
        assertEquals(value.size(), 3);
        assertEquals(value.get(ARTIFACT_PROPERTY), InstallManagerArtifact.NAME);
        assertEquals(value.get(VERSION_PROPERTY), "1.0.2");
        assertNull(value.get(MD5_PROPERTY));
    }

    @Test
    public void testGetPropertiesOfLatestStableVersion() throws Exception {
        Properties stableVersionProps = new Properties();
        stableVersionProps.putAll(ImmutableMap.<Object, Object>of(
            ArtifactProperties.LABEL_PROPERTY, VersionLabel.STABLE.toString())
        );
        artifactStorage.upload(new ByteArrayInputStream("content".getBytes()), CDECArtifact.NAME, "1.0.0", "tmp", stableVersionProps);
        stableVersionProps.putAll(ImmutableMap.<Object, Object>of(ArtifactProperties.LABEL_PROPERTY, VersionLabel.STABLE.toString()));
        artifactStorage.upload(new ByteArrayInputStream("content".getBytes()), CDECArtifact.NAME, "1.0.1", "tmp", stableVersionProps);

        Properties unstableVersionProps = new Properties();
        unstableVersionProps.putAll(ImmutableMap.<Object, Object>of(
            ArtifactProperties.LABEL_PROPERTY, VersionLabel.UNSTABLE.toString())
        );
        artifactStorage.upload(new ByteArrayInputStream("content".getBytes()), CDECArtifact.NAME, "1.0.2", "tmp", unstableVersionProps);

        Properties emptyProps = new Properties();
        artifactStorage.upload(new ByteArrayInputStream("content".getBytes()), CDECArtifact.NAME, "1.0.3", "tmp", emptyProps);

        Response response = given().when().get(format("repository/properties/%s?label=%s", CDECArtifact.NAME, VersionLabel.STABLE.toString().toLowerCase()));
        assertEquals(response.statusCode(), OK_RESPONSE.getStatus());

        Map value = Commons.asMap(response.body().asString());

        assertNotNull(value);
        assertEquals(value.size(), 4);
        assertEquals(value.get(ARTIFACT_PROPERTY), CDECArtifact.NAME);
        assertEquals(value.get(VERSION_PROPERTY), "1.0.1");
        assertEquals(value.get(LABEL_PROPERTY), VersionLabel.STABLE.toString());
    }

    @Test
    public void testFailOfGettingLatestStableVersion() throws Exception {
        Properties unstableVersionProps = new Properties();
        unstableVersionProps.putAll(ImmutableMap.<Object, Object>of(ArtifactProperties.LABEL_PROPERTY, VersionLabel.UNSTABLE.toString()));
        artifactStorage.upload(new ByteArrayInputStream("content".getBytes()), InstallManagerArtifact.NAME, "1.0.2", "tmp", unstableVersionProps);

        Response response = given().when().get(format("repository/properties/%s?label=%s", InstallManagerArtifact.NAME, VersionLabel.STABLE.toString().toLowerCase()));
        assertEquals(response.statusCode(), NOT_FOUND_RESPONSE.getStatus());
    }

    @Test
    public void testGetArtifactProperties() throws Exception {
        Map testProperties = new HashMap<String, String>() {{
            put(AUTHENTICATION_REQUIRED_PROPERTY, "true");
            put(SUBSCRIPTION_PROPERTY, "OnPremises");
        }};

        Properties testPropertiesContainer = new Properties();
        testPropertiesContainer.putAll(testProperties);

        artifactStorage.upload(new ByteArrayInputStream("content".getBytes()), InstallManagerArtifact.NAME, "1.0.1", "tmp", testPropertiesContainer);

        Response response = given().when().get("repository/properties/" + InstallManagerArtifact.NAME + "/1.0.1");
        assertEquals(response.statusCode(), OK_RESPONSE.getStatus());

        Map value = Commons.asMap(response.body().asString());

        assertNotNull(value);
        assertEquals(value.size(), 4);
        assertEquals(value.get(ARTIFACT_PROPERTY), InstallManagerArtifact.NAME);
        assertEquals(value.get(VERSION_PROPERTY), "1.0.1");
        assertEquals(value.get(AUTHENTICATION_REQUIRED_PROPERTY), "true");
        assertNull(value.get(MD5_PROPERTY));
    }

    @Test
    public void testDownloadPublicArtifact() throws Exception {
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

        artifactStorage.upload(new ByteArrayInputStream("content".getBytes()), InstallManagerArtifact.NAME, "1.0.1", "tmp", new Properties());

        Response response = given().when().get("repository/public/download/" + InstallManagerArtifact.NAME + "/1.0.1");
        assertEquals(response.statusCode(), OK_RESPONSE.getStatus());
        assertEquals(IOUtils.toString(response.body().asInputStream()), "content");

        verify(mockEventLogger).log(eventCaptor.capture());

        Event loggedEvent = eventCaptor.getValue();
        assertEquals(loggedEvent.getType(), Event.Type.IM_ARTIFACT_DOWNLOADED);
        assertTrue(loggedEvent.getParameters().toString().matches("\\{ARTIFACT=installation-manager-cli, VERSION=1.0.1, USER=, TIME=\\d*}"),
                   "Actual value: " + loggedEvent.getParameters().toString());
    }

    @Test
    public void testDownloadPublicErrorWhenArtifactAbsent() throws Exception {
        Response response = given().when().get("repository/public/download/installation-manager/1.0.2");
        assertEquals(response.statusCode(), javax.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testDownloadPublicArtifactLatestVersion() throws Exception {
        artifactStorage.upload(new ByteArrayInputStream("content".getBytes()), InstallManagerArtifact.NAME, "1.0.1", "tmp", new Properties());

        Response response = given().when().get("repository/public/download/" + InstallManagerArtifact.NAME);
        assertEquals(response.statusCode(), OK_RESPONSE.getStatus());
        assertEquals(IOUtils.toString(response.body().asInputStream()), "content");
    }

    @Test
    public void testDownloadPrivateArtifact() throws Exception {
        artifactStorage.upload(new ByteArrayInputStream("content".getBytes()), "codenvy", "1.0.1", "tmp", authenticationRequiredProperties);

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .get(JettyHttpServer.SECURE_PATH + "/repository/download/codenvy/1.0.1");
        assertEquals(response.statusCode(), OK_RESPONSE.getStatus());
        assertEquals(IOUtils.toString(response.body().asInputStream()), "content");
    }

    @Test
    public void testDownloadArtifactWhenAuthenticationError() throws Exception {
        artifactStorage.upload(new ByteArrayInputStream("content".getBytes()), "codenvy", "1.0.1", "tmp", authenticationRequiredProperties);

        Response response = given().when().get("repository/public/download/codenvy/1.0.1");
        assertEquals(response.statusCode(), javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void testUploadDownloadSnapshotVersion() throws Exception {
        Path tmp = Paths.get("target/tmp-1.0.1.txt");
        Files.copy(new ByteArrayInputStream("content".getBytes()), tmp, StandardCopyOption.REPLACE_EXISTING);

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .multiPart(tmp.toFile()).post(JettyHttpServer.SECURE_PATH + "/repository/upload/codenvy/1.0.1-SNAPSHOT");
        assertEquals(response.statusCode(), OK_RESPONSE.getStatus());

        response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .get("/repository/public/download/codenvy");
        assertEquals(response.statusCode(), OK_RESPONSE.getStatus());
        assertEquals(IOUtils.toString(response.body().asInputStream()), "content");
    }

    @Test
    public void testUploadSnapshotVersion() throws Exception {
        Path tmp = Paths.get("target/tmp-1.0.1.txt");
        Files.copy(new ByteArrayInputStream("content".getBytes()), tmp, StandardCopyOption.REPLACE_EXISTING);

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .multiPart(tmp.toFile()).post(JettyHttpServer.SECURE_PATH + "/repository/upload/codenvy/1.0.1-SNAPSHOT");

        assertEquals(response.statusCode(), OK_RESPONSE.getStatus());

        Path artifact = Paths.get("target", "download", "codenvy", "1.0.1-SNAPSHOT", "tmp-1.0.1.txt");
        assertEquals(FileUtils.readFileToString(artifact.toFile()), "content");
        assertTrue(Files.exists(artifact));

        Path propertiesFile = Paths.get("target", "download", "codenvy", "1.0.1-SNAPSHOT", ArtifactStorage.PROPERTIES_FILE);
        assertTrue(Files.exists(propertiesFile));

        Properties properties = new Properties();
        properties.load(Files.newInputStream(propertiesFile));
        assertEquals(properties.size(), 3);
        assertEquals(properties.get(VERSION_PROPERTY), "1.0.1-SNAPSHOT");
        assertEquals(properties.get(FILE_NAME_PROPERTY), "tmp-1.0.1.txt");
        assertEquals(properties.get(ARTIFACT_PROPERTY), "codenvy");
    }


    @Test
    public void testUpload() throws Exception {
        Path tmp = Paths.get("target/tmp-1.0.1.txt");
        Files.copy(new ByteArrayInputStream("content".getBytes()), tmp, StandardCopyOption.REPLACE_EXISTING);

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .multiPart(tmp.toFile()).post(JettyHttpServer.SECURE_PATH + "/repository/upload/codenvy/1.0.1?revision=abcd&build-time=20140930");

        assertEquals(response.statusCode(), OK_RESPONSE.getStatus());

        Path artifact = Paths.get("target", "download", "codenvy", "1.0.1", "tmp-1.0.1.txt");
        assertEquals(FileUtils.readFileToString(artifact.toFile()), "content");
        assertTrue(Files.exists(artifact));

        Path propertiesFile = Paths.get("target", "download", "codenvy", "1.0.1", ArtifactStorage.PROPERTIES_FILE);
        assertTrue(Files.exists(propertiesFile));

        Properties properties = new Properties();
        properties.load(Files.newInputStream(propertiesFile));
        assertEquals(properties.size(), 4);
        assertEquals(properties.get(VERSION_PROPERTY), "1.0.1");
        assertEquals(properties.get(FILE_NAME_PROPERTY), "tmp-1.0.1.txt");
        assertEquals(properties.get(BUILD_TIME_PROPERTY), "20140930");
        assertEquals(properties.get(ARTIFACT_PROPERTY), "codenvy");
    }

    @Test
    public void testUploadErrorIfVersionHasBadFormat() throws Exception {
        Path tmp = Paths.get("target/tmp");
        Files.copy(new ByteArrayInputStream("content".getBytes()), tmp, StandardCopyOption.REPLACE_EXISTING);

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .multiPart(tmp.toFile()).post(JettyHttpServer.SECURE_PATH + "/repository/upload/codenvy-1.01.1/1.01.1");

        assertEquals(response.statusCode(), javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testUploadErrorIfNoStream() throws Exception {
        Files.copy(new ByteArrayInputStream("content".getBytes()), Paths.get("target/tmp"), StandardCopyOption.REPLACE_EXISTING);

        Response response = given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD).when()
                .post(JettyHttpServer.SECURE_PATH + "/repository/upload/codenvy-1.01.1/1.01.1");

        assertEquals(response.statusCode(), javax.ws.rs.core.Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    }

    @Test
    public void testSendNotificationLetterGetEmailFromName() throws Exception {
        repositoryService
                .sendNotificationLetter("accountId", new SubjectImpl("name@codenvy.com", "id", "token", Collections.<String>emptyList(), false));
        verify(mockMailUtil).sendNotificationLetter("accountId", "name@codenvy.com");
    }

    @Test
    public void testSendNotificationLetterGetEmailFromRequest() throws Exception {
        doReturn("{\"email\": \"userEmail\"}").when(mockHttpTransport).doGet(endsWith("/user"), eq("token"));
        repositoryService
                .sendNotificationLetter("accountId", new SubjectImpl("name", "id", "token", Collections.<String>emptyList(), false));
        verify(mockMailUtil).sendNotificationLetter("accountId", "userEmail");
    }

    @Test
    public void testUpdates() throws Exception {
        artifactStorage.upload(new ByteArrayInputStream("content".getBytes()), "codenvy", "1.0.1", "tmp", new Properties());
        artifactStorage.upload(new ByteArrayInputStream("content".getBytes()), "codenvy", "1.0.2", "tmp", new Properties());

        Response response = given().when().get("/repository/updates/codenvy?fromVersion=1.0.1");

        assertEquals(response.statusCode(), OK_RESPONSE.getStatus());

        List l = response.getBody().as(List.class);
        assertEquals(l.size(), 1);
        assertEquals(l.get(0), "1.0.2");
    }

    @Test
    public void shouldLogEventOfAnonymousUser() throws UnsupportedEncodingException {
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

        Map<String,String> testEventParameters = ImmutableMap.of(
            "PARAM1", "param1-value",
            "PARAM2", "param2-value"
        );
        Event.Type testEvenType = Event.Type.IM_ARTIFACT_DOWNLOADED;
        Event testEvent = EventFactory.createWithTime(testEvenType, testEventParameters);

        HttpServletRequest requestContext = mock(HttpServletRequest.class);
        String testUserIp = "10.20.30.40";
        doReturn(testUserIp).when(requestContext).getRemoteAddr();

        UserManager spyUserManager = spy(new UserManager());
        when(spyUserManager.getCurrentUser()).thenReturn(new SubjectImpl(UserManager.ANONYMOUS_USER_NAME, TEST_USER_ID, "token", Collections.<String>emptyList(), false));

        repositoryService = new RepositoryService("",
                                                  spyUserManager,
                                                  artifactStorage,
                                                  mockHttpTransport,
                                                  mockMailUtil,
                                                  saasUserServiceProxy,
                                                  mockEventLogger);

        javax.ws.rs.core.Response response = repositoryService.logEvent(requestContext, testEvent);
        assertEquals(response.getStatus(), OK_RESPONSE.getStatus());

        verify(mockEventLogger).log(eventCaptor.capture());

        Event loggedEvent = eventCaptor.getValue();
        assertEquals(loggedEvent.getType(), testEvenType);
        assertTrue(loggedEvent.getParameters().toString().matches("\\{PARAM1=param1-value, PARAM2=param2-value, TIME=\\d*, USER-IP=" + testUserIp + ", USER=}"),
                   "Actual value: " + loggedEvent.getParameters().toString());

    }

    @Test
    public void shouldLogEventOfAuthorizedUser() throws UnsupportedEncodingException {
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

        Map<String,String> testEventParameters = ImmutableMap.of(
            "PARAM1", "param1-value",
            "PARAM2", "param2-value"
        );
        Event.Type testEvenType = Event.Type.IM_ARTIFACT_DOWNLOADED;
        Event testEvent = EventFactory.createWithTime(testEvenType, testEventParameters);

        HttpServletRequest requestContext = mock(HttpServletRequest.class);
        String testUserIp = "10.20.30.40";
        doReturn(testUserIp).when(requestContext).getRemoteAddr();

        UserManager spyUserManager = spy(new UserManager());
        when(spyUserManager.getCurrentUser()).thenReturn(new SubjectImpl(ANY_NAME, TEST_USER_ID, "token", Collections.<String>emptyList(), false));

        repositoryService = new RepositoryService("",
                                                  spyUserManager,
                                                  artifactStorage,
                                                  mockHttpTransport,
                                                  mockMailUtil,
                                                  saasUserServiceProxy,
                                                  mockEventLogger);

        javax.ws.rs.core.Response response = repositoryService.logEvent(requestContext, testEvent);
        assertEquals(response.getStatus(), OK_RESPONSE.getStatus());

        verify(mockEventLogger).log(eventCaptor.capture());

        Event loggedEvent = eventCaptor.getValue();
        assertEquals(loggedEvent.getType(), testEvenType);
        assertTrue(loggedEvent.getParameters().toString().matches("\\{PARAM1=param1-value, PARAM2=param2-value, TIME=\\d*, USER-IP=" + testUserIp + ", USER=" + TEST_USER_ID + "}"),
                   "Actual value: " + loggedEvent.getParameters().toString());

    }

}
