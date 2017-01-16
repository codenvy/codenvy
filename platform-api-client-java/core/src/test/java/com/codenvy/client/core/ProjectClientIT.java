/*******************************************************************************
 * Copyright (c) [2012] - [2017] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.client.core;

import com.codenvy.client.core.model.DefaultProjectBuilder;
import com.codenvy.client.model.Project;
import com.codenvy.client.model.ProjectReference;
import com.codenvy.client.model.Workspace;
import com.codenvy.client.model.project.BuildersDescription;
import com.codenvy.client.model.project.ImportResponse;
import com.codenvy.client.model.project.RunnersDescription;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * {@linkplain com.codenvy.client.core.DefaultProjectClient ProjectService} tests.
 *
 * @author Kevin Pollet
 * @author Stéphane Daviet
 */
@Ignore
public class ProjectClientIT extends AbstractIT {
    private static Workspace        workspace;
    private static ProjectReference projectReferencePrj1;

    @BeforeClass
    public static void initialize() throws IOException {
        workspace = codenvy.workspace()
                           .withName(SDK_WORKSPACE_NAME)
                           .execute();

        assertNotNull(workspace);


        // init the workspace VFS mapping
        Path path = new File(System.getProperty("java.io.tmpdir")).toPath();
        Path factoryPath = Files.createTempDirectory(path, "factory");
        factoryPath.toFile().deleteOnExit();
        factoryPath.toFile().mkdirs();
        codenvy.vfs().directoryMapping(workspace.id(), factoryPath.toFile().getPath()).execute();

        projectReferencePrj1 = new DefaultProjectBuilder().withType("blank")
                                                          .withName("prj1")
                                                          .withDescription("description")
                                                          .withWorkspaceId(workspace.id())
                                                          .withWorkspaceName(workspace.name())
                                                          .build();


        codenvy.project()
               .create(projectReferencePrj1)
               .execute();

        assertNotNull(projectReferencePrj1);


        final URI uri = UriBuilder.fromUri(REST_API_URL).path("api/project").build();
        final WebTarget webTarget = ClientBuilder.newClient().target(uri);
        webTarget.path(projectReferencePrj1.workspaceId())
                 .path("folder")
                 .path(projectReferencePrj1.name())
                 .path("src")
                 .request()
                 .post(null);

        webTarget.path(projectReferencePrj1.workspaceId())
                 .path("file")
                 .path(projectReferencePrj1.name())
                 .queryParam("name", "src/file.txt")
                 .request()
                 .post(Entity.text(ProjectClientIT.class.getResourceAsStream("/file.txt")));

        webTarget.path(projectReferencePrj1.workspaceId())
                 .path("file")
                 .path(projectReferencePrj1.name())
                 .queryParam("name", "src/file2.txt")
                 .request()
                 .post(Entity.text(ProjectClientIT.class.getResourceAsStream("/file.txt")));
    }

    @Test(expected = NullPointerException.class)
    public void testGetWorkspaceProjectsWithNullWorkspaceId() {
        codenvy.project()
               .getWorkspaceProjects(null)
               .execute();
    }

    @Test
    public void testGetWorkspaceProjects() {
        final List<ProjectReference> projectReferences = codenvy.project()
                                              .getWorkspaceProjects(workspace.id())
                                              .execute();

        assertNotNull(projectReferences);
        assertFalse(projectReferences.isEmpty());
        assertTrue(projectReferences.size() > 1);

        ProjectReference projectReference = null;
        for (ProjectReference tmpProjectReference : projectReferences) {
            if ("prj1".equals(tmpProjectReference.name())) {
                projectReference = tmpProjectReference;
                break;
            }
        }
        assertNotNull(projectReference);
    }

    @Test
    public void testGetWorkspaceProject() {
        final Project project = codenvy.project()
                                                                .getProject(workspace.id(), "prj1")
                                                                .execute();
        assertNotNull(project);
        assertEquals("prj1", project.name());
    }

    @Test
    public void testGetWorkspaceProjectPermissions() {
        final Project project = codenvy.project()
                                       .getProject(workspace.id(), "prj1")
                                       .execute();
        assertNotNull(project);
        List<String> permissions = project.permissions();
        // Check we've the permissions
        assertNotNull(permissions);
        Collection<String> expected = Arrays.asList("read", "write", "update_acl", "build", "run");

        assertTrue(permissions.containsAll(expected));
    }

    @Test
    public void testGetWorkspaceProjectAttributes() {
        final Project project = codenvy.project()
                                       .getProject(workspace.id(), "prj1")
                                       .execute();
        assertNotNull(project);
        Map<String, List<String>> attributes = project.attributes();
        // Check we've the attributes
        assertNotNull(attributes);

        assertTrue(attributes.isEmpty());


    }

    @Test
    public void testGetProjectRunner() {
        final Project project = codenvy.project()
                                       .getProject(workspace.id(), "prj1")
                                       .execute();
        assertNotNull(project);
        RunnersDescription runnersDescription = project.runners();
        assertNotNull(runnersDescription);
        assertNull(runnersDescription.defaultRunner());
        assertTrue(runnersDescription.configurations().isEmpty());
    }


    @Test
    public void testGetProjectBuilder() {
        final Project project = codenvy.project()
                                       .getProject(workspace.id(), "prj1")
                                       .execute();
        assertNotNull(project);
        BuildersDescription buildersDescription = project.builders();
        assertNotNull(buildersDescription);
        assertNull(buildersDescription.defaultBuilder());
    }


    @Test(expected = NullPointerException.class)
    public void testCreateWithNullProject() {
        codenvy.project()
               .create(null)
               .execute();
    }

    @Test(expected = NullPointerException.class)
    public void testExportResourcesWithNullProject() {
        codenvy.project()
               .exportResources(null, null)
               .execute();
    }

    @Test
    public void testExportResources() {
        final ZipInputStream zipInputStream = codenvy.project()
                                                     .exportResources(projectReferencePrj1, null)
                                                     .execute();

        assertNotNull(zipInputStream);
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteResourcesWithNullProject() {
        codenvy.project()
               .deleteResources(null, null)
               .execute();
    }


    @Test(expected = NullPointerException.class)
    public void testImportArchiveWithNullWorkspace() {
        codenvy.project()
               .importArchive(null,
                              projectReferencePrj1,
                              ProjectClientIT.class.getResourceAsStream("/archiveToImport.zip"))
               .execute();
    }

    @Test(expected = NullPointerException.class)
    public void testImportArchiveWithNullProject() {
        codenvy.project()
               .importArchive(workspace.id(),
                              null,
                              ProjectClientIT.class.getResourceAsStream("/archiveToImport.zip"))
               .execute();
    }

    @Test(expected = NullPointerException.class)
    public void testImportArchiveWithNullInputStream() {
        codenvy.project()
               .importArchive(workspace.id(),
                              projectReferencePrj1,
                              null)
               .execute();
    }

    @Test
    public void testImportArchive() throws IOException {
        codenvy.project()
               .importArchive(workspace.id(),
                              projectReferencePrj1,
                              ProjectClientIT.class.getResourceAsStream("/archiveToImport.zip"))
               .execute();

        assertTrue(codenvy.project().hasFile(projectReferencePrj1, "/fileToImport.txt").execute());
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateFileWithNullProject() {
        codenvy.project()
               .updateFile(null, "dummyPath", new ByteArrayInputStream(new byte[0]))
               .execute();
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateFileWithNullFilePath() {
        codenvy.project()
               .updateFile(projectReferencePrj1, null, new ByteArrayInputStream(new byte[0]))
               .execute();
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateFileWithNullInputStream() {
        codenvy.project()
               .updateFile(projectReferencePrj1, "dummyPath", null)
               .execute();
    }

    @Test(expected = NullPointerException.class)
    public void testGetFileWithNullProject() {
        codenvy.project()
               .getFile(null, "dummyPath")
               .execute();
    }

    @Test(expected = NullPointerException.class)
    public void testGetFileWithNullFilePath() {
        codenvy.project()
               .getFile(projectReferencePrj1, null)
               .execute();
    }

    @Test
    public void testUpdateAndGetFile() throws IOException {
        codenvy.project()
               .updateFile(projectReferencePrj1, "src/file.txt", new ByteArrayInputStream("content2".getBytes()))
               .execute();

        final InputStream stream = codenvy.project()
                                          .getFile(projectReferencePrj1, "src/file.txt")
                                          .execute();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        assertEquals("content2", reader.readLine());
    }

    @Test(expected = NullPointerException.class)
    public void testHasFolderWithNullProject() {
        codenvy.project()
               .hasFolder(null, "dummyPath")
               .execute();
    }

    @Test(expected = NullPointerException.class)
    public void testHasFileWithNullProject() {
        codenvy.project()
               .hasFile(null, "dummyPath")
               .execute();
    }


    @Test(expected = NullPointerException.class)
    public void testHasFolderWithNullResourcePath() {
        codenvy.project()
               .hasFolder(projectReferencePrj1, null)
               .execute();
    }

    @Test(expected = NullPointerException.class)
    public void testHasFileWithNullResourcePath() {
        codenvy.project()
               .hasFile(projectReferencePrj1, null)
               .execute();
    }

    @Test
    public void testHasFile() {
        final boolean result = codenvy.project()
                                      .hasFile(projectReferencePrj1, "src/file.txt")
                                      .execute();

        assertTrue(result);
    }

    @Test
    public void testHasFileNotExist() {
        final boolean result = codenvy.project()
                                      .hasFile(projectReferencePrj1, "src/filedoesnotexit.txt")
                                      .execute();

        assertFalse(result);
    }

    @Test
    public void testHasFolder() {
        final boolean result = codenvy.project()
                                      .hasFolder(projectReferencePrj1, "src")
                                      .execute();

        assertTrue(result);
    }

    @Test
    public void testHasFolderNotExist() {
        final boolean result = codenvy.project()
                                      .hasFolder(projectReferencePrj1, "doesnotexist")
                                      .execute();

        assertFalse(result);
    }

    @Test
    public void testDeleteAndIsResources() {
        codenvy.project()
               .deleteResources(projectReferencePrj1, "src/file2.txt")
               .execute();

        final boolean exists = codenvy.project()
                                      .hasFile(projectReferencePrj1, "src/file2.txt")
                                      .execute();

        assertFalse(exists);
    }


    @Test
    public void testImportAndUpdateFromFactory() throws URISyntaxException, IOException, ExecutionException, InterruptedException {

        String factoryContent = "{\n" +
                                "    \"v\" : \"2.0\",\n" +
                                "    \"project\" : {\n" +
                                "        \"description\" : \"jsp sample app\",\n" +
                                "        \"type\" : \"blank\"\n" +
                                "    },\n" +
                                "\"source\" : { \"project\" : { \"location\" : \"$LOCATION$\",\n" +
                                "          \"type\" : \"zip\"\n" +
                                "        }\n" +
                                "    }\n" +
                                "}\n";


        HttpServer httpServer = HttpServer.create(new InetSocketAddress(5000), 0);
        httpServer.setExecutor(null);
        HttpHandler httpHandler = new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                URI uri = httpExchange.getRequestURI();

                // needs to send the file
                if ("/dummy/file1.zip".equals(uri.getPath())) {
                    URL archiveURL = ProjectClientIT.class.getResource("/archiveToImport.zip");
                    byte[] buf = new byte[1024];

                    try (InputStream is = archiveURL.openStream(); ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
                        int n;
                        while ((n = is.read(buf, 0, 1024)) > -1) {
                            baos.write(buf, 0, n);
                        }
                        httpExchange.sendResponseHeaders(200, baos.toByteArray().length);
                        httpExchange.getResponseBody().write(baos.toByteArray());
                    }
                }
            }
        };
        httpServer.createContext("/dummy", httpHandler);
        httpServer.start();

        factoryContent = factoryContent.replace("$LOCATION$", "http://localhost:5000/dummy/file1.zip");

        // Dump the content
        Path factoryPath = Files.createTempFile(new File(System.getProperty("java.io.tmpdir")).toPath(), "factory", ".json");

            // dump
            Files.write(factoryPath, factoryContent.getBytes(Charset.defaultCharset()));


        ImportResponse importResponse = codenvy.project().importProject(workspace.id(), "my-jsp-sample", factoryPath).execute();

        Project project = importResponse.project();


        assertNotNull(project);
        assertEquals("my-jsp-sample", project.name());

        // ok we will update project descriptor
        Project updatedProject = codenvy.project().updateProject(project, factoryPath).execute();
        assertNotNull(updatedProject);
        assertEquals(updatedProject.name(), "my-jsp-sample");
        // description should have been updated
        assertEquals(updatedProject.description(), "jsp sample app");

    }

}
