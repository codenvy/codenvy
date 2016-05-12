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
package com.codenvy.im.service;

import com.codenvy.im.artifacts.Artifact;
import com.codenvy.im.artifacts.ArtifactFactory;
import com.codenvy.im.artifacts.ArtifactNotFoundException;
import com.codenvy.im.artifacts.ArtifactProperties;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.event.Event;
import com.codenvy.im.facade.IMCliFilteredFacade;
import com.codenvy.im.facade.InstallationManagerFacade;
import com.codenvy.im.managers.BackupConfig;
import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.DownloadAlreadyStartedException;
import com.codenvy.im.managers.DownloadNotStartedException;
import com.codenvy.im.managers.InstallOptions;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.managers.InstallationNotStartedException;
import com.codenvy.im.managers.NodeConfig;
import com.codenvy.im.managers.PropertiesNotFoundException;
import com.codenvy.im.managers.PropertyNotFoundException;
import com.codenvy.im.response.ArtifactInfo;
import com.codenvy.im.response.BackupInfo;
import com.codenvy.im.response.DownloadProgressResponse;
import com.codenvy.im.response.InstallArtifactInfo;
import com.codenvy.im.response.InstallArtifactStepInfo;
import com.codenvy.im.response.NodeInfo;
import com.codenvy.im.response.UpdateArtifactInfo;
import com.codenvy.im.saas.SaasUserCredentials;
import com.codenvy.im.utils.HttpException;
import com.codenvy.im.utils.IllegalVersionException;
import com.codenvy.im.utils.Version;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.api.auth.shared.dto.Credentials;
import org.eclipse.che.api.auth.shared.dto.Token;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.dto.server.JsonArrayImpl;
import org.eclipse.che.dto.server.JsonStringMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.codenvy.im.utils.Commons.createArtifactOrNull;
import static com.codenvy.im.utils.Commons.createVersionOrNull;
import static com.codenvy.im.utils.Commons.toJson;
import static java.lang.String.format;

/**
 * @author Dmytro Nochevnov
 *         We deny concurrent access to the services by marking this class as Singleton
 *         so as there are operations which couldn't execute simulteniously: addNode, removeNode, backup, restore, updateCodenvyProperty
 */
@Singleton
@Path("/")
@RolesAllowed({"system/admin"})
@Api(value = "/im", description = "Installation manager")
public class InstallationManagerService {

    private static final Logger LOG            = LoggerFactory.getLogger(InstallationManagerService.class);
    private static final String DOWNLOAD_TOKEN = UUID.randomUUID().toString();

    private static final String    SECURE_VALUE_MASK    = "*****";
    private static final Pattern[] PRIVATE_KEY_PATTERNS = new Pattern[]{Pattern.compile("password$"),
                                                                        Pattern.compile("_pass$"),
                                                                        Pattern.compile("secret$")};

    protected final InstallationManagerFacade delegate;
    protected final ConfigManager             configManager;
    protected final String                    backupDir;

    protected SaasUserCredentials saasUserCredentials;

    @Inject
    public InstallationManagerService(@Named("installation-manager.backup_dir") String backupDir,
                                      IMCliFilteredFacade delegate,
                                      ConfigManager configManager) {
        this.delegate = delegate;
        this.configManager = configManager;
        this.backupDir = backupDir;
    }

    /**
     * Starts downloading artifacts.
     */
    @POST
    @Path("downloads")
    @ApiOperation(value = "Starts downloading artifacts", response = DownloadToken.class)
    @ApiResponses(value = {@ApiResponse(code = 202, message = "OK"),
                           @ApiResponse(code = 400, message = "Illegal version format or artifact name"),
                           @ApiResponse(code = 409, message = "Downloading already in progress"),
                           @ApiResponse(code = 500, message = "Server error")})
    public Response startDownload(
            @QueryParam(value = "artifact") @ApiParam(value = "Artifact name", allowableValues = CDECArtifact.NAME) String artifactName,
            @QueryParam(value = "version") @ApiParam(value = "Version number") String versionNumber) {

        try {
            // DownloadManager has to support tokens
            DownloadToken downloadToken = new DownloadToken();
            downloadToken.setId(DOWNLOAD_TOKEN);

            Artifact artifact = createArtifactOrNull(artifactName);
            Version version = createVersionOrNull(versionNumber);

            delegate.startDownload(artifact, version);
            return Response.status(Response.Status.ACCEPTED).entity(downloadToken).build();
        } catch (ArtifactNotFoundException | IllegalVersionException e) {
            return handleException(e, Response.Status.BAD_REQUEST);
        } catch (DownloadAlreadyStartedException e) {
            return handleException(e, Response.Status.CONFLICT);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Interrupts downloading.
     */
    @DELETE
    @Path("downloads/{id}")
    @ApiOperation(value = "Interrupts downloading")
    @ApiResponses(value = {@ApiResponse(code = 204, message = "OK"),
                           @ApiResponse(code = 404, message = "Downloading not found"),
                           @ApiResponse(code = 409, message = "Downloading not in progress"),
                           @ApiResponse(code = 500, message = "Server error")})
    public Response stopDownload(@PathParam("id") @ApiParam(value = "Download Id") String downloadId) {
        try {
            delegate.stopDownload();
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (DownloadNotStartedException e) {
            return handleException(e, Response.Status.CONFLICT);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Gets download progress.
     */
    @GET
    @Path("downloads/{id}")
    @ApiOperation(value = "Gets download progress", response = DownloadProgressResponse.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
                           @ApiResponse(code = 404, message = "Downloading not found"),
                           @ApiResponse(code = 409, message = "Downloading not in progress"),
                           @ApiResponse(code = 500, message = "Server error")})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDownloadProgress(@PathParam("id") @ApiParam(value = "Download Id") String downloadId) {
        try {
            DownloadProgressResponse downloadProgress = delegate.getDownloadProgress();
            return Response.ok(downloadProgress).build();
        } catch (DownloadNotStartedException e) {
            return handleException(e, Response.Status.CONFLICT);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Deletes downloaded artifact.
     */
    @DELETE
    @Path("downloads/{artifact}/{version}")
    @ApiOperation(value = "Deletes downloaded artifact")
    @ApiResponses(value = {@ApiResponse(code = 204, message = "Successfully removed"),
                           @ApiResponse(code = 400, message = "Illegal version format or artifact name"),
                           @ApiResponse(code = 500, message = "Server error")})
    public Response deleteDownloadedArtifact(
            @PathParam("artifact") @ApiParam(value = "Artifact name") final String artifactName,
            @PathParam("version") @ApiParam(value = "Artifact version") final String artifactVersion) {

        try {
            Artifact artifact = createArtifact(artifactName);
            Version version = Version.valueOf(artifactVersion);

            delegate.deleteDownloadedArtifact(artifact, version);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Get the list of actual updates from Update Server.
     */
    @GET
    @Path("updates")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the list of actual updates from Update Server", response = UpdateArtifactInfo.class, responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
                           @ApiResponse(code = 500, message = "Server error")})
    public Response getUpdates() {
        try {
            Collection<UpdateArtifactInfo> updates = delegate.getAllUpdates(createArtifact(CDECArtifact.NAME));
            return Response.ok(updates).build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Get download ID being in progress.
     */
    @GET
    @Path("downloads")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get download ID being in progress")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Ok"),
                           @ApiResponse(code = 409, message = "Downloading not in progress")})
    public Response getDownloads() {
        try {
            String id = delegate.getDownloadIdInProgress();
            Map<String, String> ids = ImmutableMap.of("id", id);

            return Response.ok(new JsonStringMapImpl<>(ids)).build();
        } catch (DownloadNotStartedException e) {
            return handleException(e, Response.Status.CONFLICT);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Gets the list of downloaded and installed artifacts.
     */
    @GET
    @Path("artifacts")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the list of downloaded and installed artifacts", response = ArtifactInfo.class, responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
                           @ApiResponse(code = 500, message = "Server error")})
    public Response getArtifacts() {
        try {
            Collection<ArtifactInfo> artifacts = delegate.getArtifacts();
            return Response.ok(artifacts).build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Gets installed artifacts.
     */
    @GET
    @Path("installations")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets installed artifacts", response = InstallArtifactInfo.class, responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Ok"),
                           @ApiResponse(code = 500, message = "Server error")})
    public Response getInstalledVersions() {
        try {
            Collection<InstallArtifactInfo> installedVersions = delegate.getInstalledVersions();
            return Response.ok(installedVersions).build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Gets updates steps.
     */
    @GET
    @Path("update/info")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets updates steps", responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Ok"),
                           @ApiResponse(code = 500, message = "Server error")})
    public Response getUpdateInfo() {
        try {
            InstallType installType = configManager.detectInstallationType();
            List<String> infos = delegate.getUpdateInfo(createArtifact(CDECArtifact.NAME), installType);
            return Response.ok(infos).build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Updates Codenvy.
     */
    @POST
    @Path("update")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates Codenvy")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Successfully updated"),
                           @ApiResponse(code = 400, message = "Binaries to install not found or installation step is out of range"),
                           @ApiResponse(code = 500, message = "Server error")})
    public Response updateCodenvy(@QueryParam("step") @ApiParam(value = "installation step starting from 0") int installStep) {
        try {
            InstallType installType = configManager.detectInstallationType();
            Artifact artifact = createArtifact(CDECArtifact.NAME);
            Version version = delegate.getLatestInstallableVersion(artifact);
            if (version == null) {
                return handleException(new IllegalStateException("There is no appropriate version to install"),
                                       Response.Status.BAD_REQUEST);
            }
            Map<String, String> properties = configManager.prepareInstallProperties(null,
                                                                                    null,
                                                                                    installType,
                                                                                    artifact,
                                                                                    version,
                                                                                    false);
            final InstallOptions installOptions = new InstallOptions();
            installOptions.setInstallType(installType);
            installOptions.setConfigProperties(properties);

            List<String> infos = delegate.getUpdateInfo(artifact, installType);
            if (installStep < 0 || installStep >= infos.size()) {
                return handleException(new IllegalArgumentException(format("Installation step is out of range [0..%d]", infos.size() - 1)),
                                       Response.Status.BAD_REQUEST);
            }

            installOptions.setStep(installStep);
            String id = delegate.update(artifact, version, installOptions);

            Map<String, String> m = ImmutableMap.of("id", id);
            return Response.status(Response.Status.ACCEPTED).entity(new JsonStringMapImpl<>(m)).build();
        } catch (FileNotFoundException e) {
            return handleException(e, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Gets Codenvy updating status.
     */
    @GET
    @Path("update/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets Codenvy updating status", response = InstallArtifactStepInfo.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Successfully updated"),
                           @ApiResponse(code = 404, message = "Updating step not found"),
                           @ApiResponse(code = 500, message = "Server error")})
    public Response getUpdateStatus(@PathParam("id") @ApiParam(value = "updating step id") String stepId) {
        try {
            InstallArtifactStepInfo info = delegate.getUpdateStepInfo(stepId);
            return Response.ok(info).build();
        } catch (InstallationNotStartedException e) {
            return handleException(e, Response.Status.NOT_FOUND);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /** Gets Installation Manager configuration */
    @GET
    @Path("properties")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets Installation Manager Server configuration")
    public Response getInstallationManagerServerConfig() {
        Map<String, String> properties = delegate.getInstallationManagerProperties();
        return Response.ok(new JsonStringMapImpl<>(properties)).build();
    }

    /** Gets Codenvy nodes configuration */
    @GET
    @Path("nodes")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets Codenvy nodes configuration")
    public Response getNodesList() {
        try {
            Map<String, Object> properties = new HashMap<>();

            InstallType installType = configManager.detectInstallationType();
            Config config = configManager.loadInstalledCodenvyConfig(installType);

            // add host url
            String hostUrl = config.getHostUrl();
            if (hostUrl != null) {
                properties.put(Config.HOST_URL, hostUrl);
            }

            properties.putAll(delegate.getNodes());

            if (InstallType.SINGLE_SERVER.equals(installType)) {
                return Response.ok(toJson(properties)).build();
            }

            // filter node dns
            List<NodeConfig> nodes = NodeConfig.extractConfigsFrom(config);
            for (NodeConfig node : nodes) {
                String nodeHostPropertyName = node.getType().toString().toLowerCase() + Config.NODE_HOST_PROPERTY_SUFFIX;
                properties.put(nodeHostPropertyName, node.getHost());
            }

            return Response.ok(toJson(properties)).build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Adds Codenvy node in the multi-node environment.
     */
    @POST
    @Path("node")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Adds Codenvy node in the multi-node environment", response = NodeInfo.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Successfully added"),
                           @ApiResponse(code = 500, message = "Server error")})
    public Response addNode(@QueryParam(value = "dns") @ApiParam(required = true, value = "node DNS to add") String dns) {
        try {
            NodeInfo nodeInfo = delegate.addNode(dns);
            return Response.status(Response.Status.CREATED).entity(nodeInfo).build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Removes Codenvy node in the multi-node environment
     */
    @DELETE
    @Path("node")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Removes Codenvy node in the multi-node environment")
    @ApiResponses(value = {@ApiResponse(code = 204, message = "Successfully removed"),
                           @ApiResponse(code = 500, message = "Server error")})
    public Response removeNode(@QueryParam(value = "dns") @ApiParam(required = true, value = "node DNS to remove") String dns) {
        try {
            delegate.removeNode(dns);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Performs Codenvy backup.
     */
    @POST
    @Path("backup")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Performs Codenvy backup", response = BackupInfo.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Successfully created"),
                           @ApiResponse(code = 500, message = "Server error")})
    public Response backup(@DefaultValue(CDECArtifact.NAME)
                                            @QueryParam(value = "artifact")
                                            @ApiParam(allowableValues = CDECArtifact.NAME) String artifactName) {

        try {
            BackupConfig config = new BackupConfig();
            config.setArtifactName(artifactName);
            config.setBackupDirectory(backupDir);

            BackupInfo backupInfo = delegate.backup(config);
            return Response.status(Response.Status.CREATED).entity(backupInfo).build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Performs Codenvy restoring.
     */
    @POST
    @Path("restore")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Performs Codenvy restoring", response = BackupInfo.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Successfully restored"),
                           @ApiResponse(code = 500, message = "Server error")})
    public Response restore(@DefaultValue(CDECArtifact.NAME)
                                             @QueryParam(value = "artifact")
                                             @ApiParam(allowableValues = CDECArtifact.NAME) String artifactName,
                                             @QueryParam(value = "backupFile")
                                             @ApiParam(value = "path to backup file", required = true)
                                             String backupFile) throws IOException {

        try {
            BackupConfig config = new BackupConfig();
            config.setArtifactName(artifactName);
            config.setBackupFile(backupFile);

            BackupInfo backupInfo = delegate.restore(config);
            return Response.status(Response.Status.CREATED).entity(backupInfo).build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Authentication failed"),
            @ApiResponse(code = 500, message = "Unexpected error occurred")})
    @ApiOperation(value = "Login to Codenvy SaaS",
            notes = "After login is successful SaaS user credentials will be cached.")
    public Response loginToCodenvySaaS(Credentials credentials) {
        try {
            logoutFromCodenvySaaS();

            credentials = DtoFactory.cloneDto(credentials);

            Token token = delegate.loginToCodenvySaaS(credentials);
            SaasUserCredentials saasUserCredentials = new SaasUserCredentials(token.getValue());

            // cache SaaS user credentials into the state of service
            this.saasUserCredentials = saasUserCredentials;

            return Response.ok().build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @POST
    @Path("logout")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Unexpected error occurred")})
    @ApiOperation(value = "Logout from Codenvy SaaS")
    public Response logoutFromCodenvySaaS() {
        try {
            if (saasUserCredentials != null) {
                delegate.logoutFromCodenvySaaS(saasUserCredentials.getToken());
            }

            return Response.ok().build();
        } catch (Exception e) {
            return handleException(e);
        } finally {
            saasUserCredentials = null;
        }
    }

    @POST
    @Path("password")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Unexpected error occurred")})
    @ApiOperation(value = "Changes admin passwords",
                  notes = "currentPassword and newPassword parameters have to be passed in the request body")
    public Response changeAdminPassword(Map<String, String> passwords) {
        try {
            final String currentPwdKey = "currentPassword";
            final String newPwdKey = "newPassword";

            if (!passwords.containsKey(currentPwdKey) || !passwords.containsKey(newPwdKey)) {
                return handleException(new IllegalArgumentException("Current and new admin passwords have to be specified"),
                                       Response.Status.BAD_REQUEST);
            }

            delegate.changeAdminPassword(passwords.get(currentPwdKey).getBytes("UTF-8"),
                                         passwords.get(newPwdKey).getBytes("UTF-8"));
            return Response.ok().build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /** @return the properties of the specific artifact and version */
    @GET
    @Path("artifact/{artifact}/version/{version}/properties")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Artifact not found"),
            @ApiResponse(code = 500, message = "Unexpected error occurred")})
    @ApiOperation(value = "Gets list of the specific artifact and version properties")
    public Response getArtifactProperties(@PathParam("artifact") final String artifactName,
                                          @PathParam("version") final String artifactVersion) {
        try {
            Artifact artifact = createArtifact(artifactName);
            Version version;
            try {
                version = Version.valueOf(artifactVersion);
            } catch (IllegalArgumentException e) {
                throw new ArtifactNotFoundException(artifactName, artifactVersion);
            }

            Map<String, String> properties = artifact.getProperties(version);
            if (properties.containsKey(ArtifactProperties.BUILD_TIME_PROPERTY)) {
                String humanReadableDateTime = properties.get(ArtifactProperties.BUILD_TIME_PROPERTY);
                String valueInIso = convertToIsoDateTime(humanReadableDateTime);
                properties.put(ArtifactProperties.BUILD_TIME_PROPERTY, valueInIso);
            }

            return Response.ok(new JsonStringMapImpl<>(properties)).build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /** Gets list of properties from the storage */
    @GET
    @Path("storage/properties")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Unexpected error occurred")})
    @ApiOperation(value = "Gets list of properties from the storage")
    public Response getStorageProperties() {
        try {
            Map<String, String> properties = delegate.loadStorageProperties();
            return Response.ok(new JsonStringMapImpl<>(properties)).build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /** Inserts new properties into the storage and update existed */
    @POST
    @Path("storage/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Unexpected error occurred")})
    @ApiOperation(value = "Inserts new properties into the storage and update existed")
    public Response insertStorageProperties(Map<String, String> properties) {
        try {
            delegate.storeStorageProperties(properties);
            return Response.ok().build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /** Get property value from the storage */
    @GET
    @Path("storage/properties/{key}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Property not found"),
            @ApiResponse(code = 500, message = "Unexpected error occurred")})
    @ApiOperation(value = "Gets property value from the storage")
    public Response getStorageProperty(@PathParam("key") String key) {
        try {
            String value = delegate.loadStorageProperty(key);
            return Response.ok(value).build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PUT
    @Path("storage/properties/{key}")
    @Consumes("text/plain")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Property not found"),
            @ApiResponse(code = 500, message = "Unexpected error occurred")})
    @ApiOperation(value = "Updates property in the storage")
    public Response updateStorageProperty(@PathParam("key") String key, String value) {
        try {
            delegate.storeStorageProperty(key, value);
            return Response.ok().build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @DELETE
    @Path("storage/properties/{key}")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No Content"),
            @ApiResponse(code = 404, message = "Property not found"),
            @ApiResponse(code = 500, message = "Unexpected error occurred")})
    @ApiOperation(value = "Deletes property from the storage")
    public Response deleteStorageProperty(@PathParam("key") String key) {
        try {
            delegate.deleteStorageProperty(key);
            return Response.noContent().build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /** Gets Codenvy configuration properties. */
    @GET
    @Path("codenvy/properties")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Unexpected error occurred")})
    @ApiOperation(value = "Gets Codenvy configuration properties")
    public Response getCodenvyProperties() {
        try {
            Config config = configManager.loadInstalledCodenvyConfig();
            Map<String, String> properties = maskPrivateProperties(config.getProperties());
            return Response.ok(new JsonStringMapImpl<>(properties)).build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /** Gets specific Codenvy configuration property. */
    @GET
    @Path("codenvy/properties/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Property not found"),
            @ApiResponse(code = 500, message = "Unexpected error occurred")})
    @ApiOperation(value = "Gets specific Codenvy configuration property")
    public Response getCodenvyProperty(@PathParam("key") String key) {
        try {
            Config config = configManager.loadInstalledCodenvyConfig();
            Map<String, String> properties = maskPrivateProperties(config.getProperties());

            if (properties.containsKey(key)) {
                Map<String, String> m = ImmutableMap.of(key, properties.get(key));
                return Response.ok(new JsonStringMapImpl<>(m)).build();
            } else {
                throw PropertyNotFoundException.from(key);
            }
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /** Updates Codenvy configuration property */
    @PUT
    @Path("codenvy/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully updated"),
            @ApiResponse(code = 404, message = "Properties not found"),
            @ApiResponse(code = 500, message = "Unexpected error occurred")})
    @ApiOperation(value = "Updates property of configuration of Codenvy on-prem. It could take 5-7 minutes.")
    public Response updateCodenvyProperties(Map<String, String> properties) {
        try {
            delegate.updateArtifactConfig(createArtifact(CDECArtifact.NAME), properties);
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /** Log SaaS Codenvy Analytics event. */
    @POST
    @Path("event")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
        @ApiResponse(code = 202, message = "Successfully log"),
        @ApiResponse(code = 400, message = "Event doesn't comply requirements for parameters."),
        @ApiResponse(code = 500, message = "Unexpected error occurred")})
    @ApiOperation(value = "Log SaaS Codenvy Analytics event. Requirements for event parameters: "
                          + " maximum parameters number = " + Event.MAX_EXTENDED_PARAMS_NUMBER + ", "
                          + " maximum parameter name length = " + Event.MAX_PARAM_NAME_LENGTH + ", "
                          + " maximum parameter value length = " + Event.MAX_PARAM_VALUE_LENGTH)
    public Response logSaasAnalyticsEvent(Event event) {
        try {
            Event.validateNumberOfParametersTreatingAsExtended(event.getParameters());

            String saasUserToken = null;
            if (saasUserCredentials != null) {
                saasUserToken = saasUserCredentials.getToken();
            }

            delegate.logSaasAnalyticsEvent(event, saasUserToken);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (IllegalArgumentException e) {
            return handleException(e, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    private Response handleException(Exception e) {
        Response.Status status;

        if (e instanceof ArtifactNotFoundException || e instanceof PropertyNotFoundException) {
            status = Response.Status.NOT_FOUND;
        } else if (e instanceof HttpException) {
            status = Response.Status.fromStatusCode(((HttpException)e).getStatus());
        } else if (e instanceof AuthenticationException
                   || e instanceof IllegalVersionException) {
            status = Response.Status.BAD_REQUEST;
        } else if (e instanceof PropertiesNotFoundException) {
            return handlePropertiesNotFoundException((PropertiesNotFoundException) e);
        } else {
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }

        return handleException(e, status);
    }

    private Response handleException(Exception e, Response.Status status) {
        LOG.error(e.getMessage(), e);

        JsonStringMapImpl<String> msgBody = new JsonStringMapImpl<>(ImmutableMap.of("message", e.getMessage()));
        return createResponse(status, msgBody);
    }

    private Response handlePropertiesNotFoundException(PropertiesNotFoundException e) {
        LOG.error(e.getMessage(), e);

        Response.Status status = Response.Status.NOT_FOUND;
        JsonStringMapImpl msgBody = new JsonStringMapImpl<>(ImmutableMap.of("message", e.getMessage(),
                                                                            "properties", new JsonArrayImpl<>(e.getProperties())));
        return createResponse(status, msgBody);
    }

    private Response createResponse(Response.Status status, JsonStringMapImpl body) {
        return Response.status(status)
                       .entity(body)
                       .type(MediaType.APPLICATION_JSON_TYPE)
                       .build();
    }

    /**
     * Convert string of datetime of format "yyyy-MM-dd HH:mm:ss" into ISO 8601 format "yyyy-MM-dd'T'HH:mm:ss.S'Z'"
     */
    private String convertToIsoDateTime(String humanReadableDateTime) throws ParseException {
        DateFormat dfInitial = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date initialDateTime = dfInitial.parse(humanReadableDateTime);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");

        return df.format(initialDateTime);
    }

    protected Artifact createArtifact(String artifactName) throws ArtifactNotFoundException {
        return ArtifactFactory.createArtifact(artifactName);
    }

    /**
     * @return map where all private keys ("password", "secret" etc.) were been updated to have PASSWORD_MASK value
     */
    private Map<String, String> maskPrivateProperties(Map<String, String> properties) {
        Map<String, String> maskedProperties = new HashMap<>(properties);
        for (Map.Entry<String, String> property : maskedProperties.entrySet()) {
            String key = property.getKey();
            if (isPrivateProperty(key)) {
                property.setValue(SECURE_VALUE_MASK);
            }
        }

        return maskedProperties;
    }

    /** @return true if only key matches one of the patterns from the PRIVATE_KEY_PATTERNS */
    private boolean isPrivateProperty(String key) {
        for (Pattern pattern : PRIVATE_KEY_PATTERNS) {
            if (pattern.matcher(key).find()) {
                return true;
            }
        }

        return false;
    }


}
