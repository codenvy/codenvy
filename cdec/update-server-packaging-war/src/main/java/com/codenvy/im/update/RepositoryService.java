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


import com.codenvy.im.artifacts.ArtifactNotFoundException;
import com.codenvy.im.artifacts.ArtifactProperties;
import com.codenvy.im.event.Event;
import com.codenvy.im.event.EventFactory;
import com.codenvy.im.event.EventLogger;
import com.codenvy.im.saas.SaasUserServiceProxy;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.IllegalVersionException;
import com.codenvy.im.utils.MailUtil;
import com.codenvy.im.utils.Version;
import com.google.common.collect.FluentIterable;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.JsonArrayImpl;
import org.eclipse.che.dto.server.JsonStringMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static com.codenvy.im.artifacts.ArtifactProperties.PUBLIC_PROPERTIES;
import static java.lang.String.format;


/**
 * Repository API.
 *
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
@Path("repository")
public class RepositoryService {

    public static final Pattern VALID_EMAIL_ADDRESS_RFC822     =
        Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    public static final String  CAN_NOT_ADD_TRIAL_SUBSCRIPTION =
        "You do not have a valid subscription to install Codenvy. Please contact sales@codenvy.com to add subscription.";

    static Logger LOG = LoggerFactory.getLogger(RepositoryService.class);  // with default access and is not final for testing propose

    private final String                  saasApiEndpoint;
    private final ArtifactStorage         artifactStorage;
    private final HttpTransport           httpTransport;
    private final UserManager             userManager;
    private final MailUtil                mailUtil;
    private final SaasUserServiceProxy    saasUserServiceProxy;
    private final EventLogger             eventLogger;

    @Inject
    public RepositoryService(@Named("saas.api.endpoint") String saasApiEndpoint,
                             UserManager userManager,
                             ArtifactStorage artifactStorage,
                             HttpTransport httpTransport,
                             MailUtil mailUtil,
                             SaasUserServiceProxy saasUserServiceProxy,
                             EventLogger eventLogger) {
        this.artifactStorage = artifactStorage;
        this.httpTransport = httpTransport;
        this.saasApiEndpoint = saasApiEndpoint;
        this.userManager = userManager;
        this.mailUtil = mailUtil;
        this.saasUserServiceProxy = saasUserServiceProxy;
        this.eventLogger = eventLogger;
    }

    /**
     * Retrieves the list of available updates.
     *
     * @param artifact
     *         artifact name
     * @param fromVersion
     *         to return the list of updates beginning from the given version, excluded
     */
    @GenerateLink(rel = "updates")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/updates/{artifact}")
    public Response getUpdates(@PathParam("artifact") final String artifact, @QueryParam("fromVersion") final String fromVersion) {
        try {
            Collection<Version> versions = artifactStorage.getVersions(artifact, fromVersion);

            List<String> l = FluentIterable.from(versions).transform(Version::toString).toList();

            return Response.status(Response.Status.OK).entity(new JsonArrayImpl<>(l)).build();
        } catch (IllegalVersionException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (ArtifactNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error. Can't retrieve the latest version of the '" + artifact + "'").build();
        }
    }

    /**
     * Retrieves properties of the latest version of the artifact.
     *
     * @param artifact
     *         the name of the artifact
     * @param label
     *         label of artifact version
     * @return Response
     */
    @GenerateLink(rel = "artifact properties")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/properties/{artifact}")
    public Response getLatestArtifactProperties(@PathParam("artifact") final String artifact,
                                                @QueryParam("label") final String label) {
        try {
            String version = artifactStorage.getLatestVersion(artifact, label);
            Map<String, String> properties = doGetArtifactProperties(artifact, version);

            return Response.status(Response.Status.OK).entity(new JsonStringMapImpl<>(properties)).build();
        } catch (ArtifactNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Unexpected error. Can't retrieve the latest version of the '" + artifact + "'. " + e.getMessage())
                           .build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error. Can't retrieve the latest version of the '" + artifact + "'").build();
        }
    }

    /**
     * Retrieves properties of the specific version of the artifact.
     *
     * @param artifact
     *         the name of the artifact
     * @param version
     *         the version of the artifact
     * @return Response
     */
    @GenerateLink(rel = "artifact properties")
    @GET
    @Path("/properties/{artifact}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getArtifactProperties(@PathParam("artifact") final String artifact,
                                          @PathParam("version") final String version) {
        try {
            Map<String, String> properties = doGetArtifactProperties(artifact, version);

            return Response.status(Response.Status.OK).entity(new JsonStringMapImpl<>(properties)).build();
        } catch (ArtifactNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(format("Unexpected error. Can't retrieve the info of the artifact %s:%s. %s",
                                          artifact, version, e.getMessage())).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(format("Unexpected error. Can't retrieve the info of the artifact %s:%s. %s",
                                          artifact, version, e.getMessage())).build();
        }
    }

    private Map<String, String> doGetArtifactProperties(final String artifact, String version) throws IOException {
        final Properties properties = artifactStorage.loadProperties(artifact, version);

        Map<String, String> m = new HashMap<>(PUBLIC_PROPERTIES.size());
        for (String prop : PUBLIC_PROPERTIES) {
            if (properties.containsKey(prop)) {
                m.put(prop, properties.getProperty(prop));
            }
        }
        return m;
    }

    /**
     * Downloads artifact of the specific version.
     *
     * @param artifact
     *         the name of the artifact
     * @param version
     *         the version of the artifact
     * @return Response
     */
    @GenerateLink(rel = "download artifact")
    @GET
    @Path("/download/{artifact}/{version}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed({"user", "system/admin"})
    public Response download(@PathParam("artifact") final String artifact,
                             @PathParam("version") final String version) {
        try {
            String userId = userManager.getCurrentUser().getUserId();
            return doDownloadArtifact(artifact, version, userId);
        } catch (ArtifactNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    "Unexpected error. Can't download the artifact " + artifact + ":" + version + ". " + e.getMessage()).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error. Can't download the artifact " + artifact + ":" + version + ". " + e.getMessage())
                           .build();
        }
    }

    /**
     * Downloads public artifact of the specific version.
     *
     * @param artifact
     *         the artifact name
     * @param version
     *         the version of the artifact
     * @return Response
     */
    @GenerateLink(rel = "download artifact")
    @GET
    @Path("/public/download/{artifact}/{version}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadPublicArtifact(@PathParam("artifact") String artifact,
                                           @PathParam("version") String version) {
        try {
            return doDownloadArtifact(artifact, version, null);
        } catch (ArtifactNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    "Unexpected error. Can't download the artifact " + artifact + ":" + version + ". " + e.getMessage()).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error. Can't download the artifact " + artifact + ":" + version + ". " + e.getMessage())
                           .build();
        }
    }

    /**
     * Downloads the latest version of the artifact.
     *
     * @param artifact
     *         the name of the artifact
     * @param label
     *         label of artifact version
     * @return Response
     */
    @GenerateLink(rel = "download artifact")
    @GET
    @Path("/public/download/{artifact}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadPublicArtifactLatestVersion(@PathParam("artifact") final String artifact,
                                                        @QueryParam("label") final String label) {
        try {
            String version = artifactStorage.getLatestVersion(artifact, label);
            return doDownloadArtifact(artifact, version, null);
        } catch (ArtifactNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Unexpected error. Can't download the latest version of artifact '" + artifact).build();
        }
    }

    private Response doDownloadArtifact(final String artifact,
                                        final String version,
                                        @Nullable final String userId) throws IOException {
        final java.nio.file.Path path = artifactStorage.getArtifact(artifact, version);
        final boolean publicURL = userId == null;

        if (!Files.exists(path)) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Unexpected error. Can't download the artifact '" + artifact +
                                   "' version " + version + ". Probably the repository doesn't contain one.").build();
        }

        if (publicURL &&
            (artifactStorage.isAuthenticationRequired(artifact, version))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Artifact '" + artifact + "' is not in public access").build();
        }

        final String fileName = artifactStorage.getFileName(artifact, version);

        if (!publicURL) {
            LOG.info(format("User '%s' is downloading %s", userId, fileName));
        }

        StreamingOutput stream = output -> {
            try (InputStream input = new FileInputStream(path.toFile())) {
                IOUtils.copyLarge(input, output);

                Event event = EventFactory.createImArtifactDownloadedEventWithTime(artifact.toLowerCase(),
                                                                                   version,
                                                                                   userId == null ? "" : userId);
                eventLogger.log(event);

            } catch (ClientAbortException e) {
                // do nothing
                LOG.info(format("User %s aborted downloading %s:%s", userId == null ? "Anonymous" : userId, artifact, version));
            } catch (Exception e) {
                LOG.info(format("User %s failed to download %s:%s", userId == null ? "Anonymous" : userId, artifact, version), e);
                throw new IOException(e.getMessage(), e);
            }
        };

        return Response.ok(stream)
                       .header("Content-Length", String.valueOf(Files.size(path)))
                       .header("Content-Disposition", "attachment; filename=" + fileName)
                       .build();
    }

    /**
     * Uploads artifact into the repository. If the same artifact exists then it will be replaced.
     * If {@value ArtifactProperties#AUTHENTICATION_REQUIRED_PROPERTY} isn't set then artifact will be treated as private, which requires
     * user to be authenticated to download it. If {@value ArtifactProperties#SUBSCRIPTION_PROPERTY}  is set then     * user has to have specific valid subscription to download artifact. If artifact is public then subscription won't be taken into account.
     *
     * @param artifact
     *         the name of the artifact
     * @param version
     *         the version of the artifact
     * @return Response
     */
    @GenerateLink(rel = "upload artifact")
    @POST
    @Path("/upload/{artifact}/{version}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({"system/admin"})
    public Response upload(@PathParam("artifact") String artifact,
                           @PathParam("version") String version,
                           @Context HttpServletRequest request,
                           @Context UriInfo uriInfo) {
        if (ServletFileUpload.isMultipartContent(request)) {
            DiskFileItemFactory diskFactory = new DiskFileItemFactory();
            diskFactory.setRepository(new File(System.getProperty("java.io.tmpdir")));

            ServletFileUpload upload = new ServletFileUpload(diskFactory);
            try {
                List<FileItem> items = upload.parseRequest(request);
                for (FileItem item : items) {
                    if (!item.isFormField()) {
                        String fileName = FilenameUtils.getName(item.getName());

                        Version v;
                        try {
                            v = Version.valueOf(version);
                        } catch (IllegalArgumentException e) {
                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                           .entity("The version format is invalid '" + version + "'").build();
                        }

                        Properties props = new Properties();
                        for (Map.Entry<String, List<String>> entry : uriInfo.getQueryParameters().entrySet()) {
                            if (PUBLIC_PROPERTIES.contains(entry.getKey())) {
                                props.put(entry.getKey(), entry.getValue().get(0));
                            }
                        }

                        try (InputStream in = item.getInputStream()) {
                            artifactStorage.upload(in, artifact, v.toString(), fileName, props);
                            return Response.status(Response.Status.OK).build();
                        } catch (IOException e) {
                            LOG.error(e.getMessage(), e);
                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                           .entity("Unexpected error occurred during uploading.").build();
                        } finally {
                            item.delete();
                        }
                    }
                }

                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Can n't upload files. The list is empty.").build();
            } catch (FileUploadException e) {
                LOG.error(e.getMessage(), e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unexpected error occurred during uploading.").build();
            }
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("The request must contain multipart content").build();
        }
    }

    /** Log event. */
    @GenerateLink(rel = "log event")
    @POST
    @Path("/event")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response logEvent(@Context HttpServletRequest requestContext,
                             Event event) {
        try {
            String userIp = requestContext.getRemoteAddr();
            event.putParameter(Event.USER_IP_PARAM, userIp);

            if (userManager.isAnonymous()) {
                event.putParameter(Event.USER_PARAM, "");
            } else {
                String userId = userManager.getCurrentUser().getUserId();
                event.putParameter(Event.USER_PARAM, userId == null ? "" : userId);
            }

            eventLogger.log(event);

            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unexpected error. " + e.getMessage()).build();
        }
    }

    protected void sendNotificationLetter(String accountId, Subject subject) {
        try {
            String userEmail = VALID_EMAIL_ADDRESS_RFC822.matcher(subject.getUserName()).matches()
                               ? subject.getUserName()
                               : saasUserServiceProxy.getUserEmail(subject.getToken());

            mailUtil.sendNotificationLetter(accountId, userEmail);
            LOG.info(format("Subscription for %s was provisioned and notification mail was sent", userEmail));
        } catch (IOException | MessagingException | ApiException e) {
            LOG.error("Error of sending email with subscription info to sales.", e);
        }
    }

}
