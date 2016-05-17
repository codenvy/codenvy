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
package org.eclipse.che.ide.ext.microsoft.server.rest;

import com.google.inject.Singleton;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.ide.ext.microsoft.server.MicrosoftVstsRestClient;
import org.eclipse.che.ide.ext.microsoft.server.URLTemplates;
import org.eclipse.che.ide.ext.microsoft.shared.dto.MicrosoftPullRequest;
import org.eclipse.che.ide.ext.microsoft.shared.dto.NewMicrosoftPullRequest;
import org.eclipse.che.ide.ext.microsoft.shared.dto.MicrosoftRepository;
import org.eclipse.che.ide.ext.microsoft.shared.dto.MicrosoftUserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * REST service for MicrosoftVstsRestClient VSTS.
 *
 * @author Mihail Kuznyetsov
 */
@Path("/microsoft")
@Singleton
public class MicrosoftVstsService {

    final MicrosoftVstsRestClient microsoftVstsRestClient;
    final URLTemplates            templates;

    private static final Logger LOG = LoggerFactory.getLogger(MicrosoftVstsService.class);

    @Inject
    public MicrosoftVstsService(MicrosoftVstsRestClient microsoftVstsRestClient, URLTemplates templates) {
        this.microsoftVstsRestClient = microsoftVstsRestClient;
        this.templates = templates;
    }

    @GET
    @Path("/profile")
    @Produces(APPLICATION_JSON)
    public MicrosoftUserProfile getUserProfile() throws ApiException {
        try {
            return microsoftVstsRestClient.getUserProfile();
        } catch (IOException e) {
            LOG.error("Getting user info fail", e);
            throw new ServerException(e.getMessage());
        }

    }

    @GET
    @Path("/repository/{account}/{collection}/{project}/{repository}")
    @Produces(APPLICATION_JSON)
    public MicrosoftRepository getRepository(@PathParam("account") String account,
                                             @PathParam("collection") String collection,
                                             @PathParam("project") String project,
                                             @PathParam("repository") String repository) throws IOException,
                                                                                                ServerException,
                                                                                                UnauthorizedException {
        return microsoftVstsRestClient.getRepository(account, collection, project, repository);
    }


    @GET
    @Path("/pullrequests/{account}/{collection}/{project}/{repository}")
    @Produces(APPLICATION_JSON)
    public List<MicrosoftPullRequest> getPullRequests(@PathParam("account") String account,
                                                      @PathParam("collection") String collection,
                                                      @PathParam("project") String project,
                                                      @PathParam("repository") String repository) throws IOException,
                                                                                                         ServerException,
                                                                                                         UnauthorizedException {
        String repositoryId = microsoftVstsRestClient.getRepository(account, collection, project, repository).getId();
        return microsoftVstsRestClient.getPullRequests(account, collection, project, repository, repositoryId);
    }

    @POST
    @Path("/pullrequests/{account}/{collection}/{repository}")
    @Consumes(APPLICATION_JSON)
    public MicrosoftPullRequest createPullRequest(@PathParam("account") String account,
                                                  @PathParam("collection") String collection,
                                                  @PathParam("repository") String repository,
                                                  NewMicrosoftPullRequest input) throws IOException,
                                                                                        ServerException,
                                                                                        UnauthorizedException {
        return microsoftVstsRestClient.createPullRequest(account, collection, repository, input);
    }

    @POST
    @Path("/pullrequests/{account}/{collection}/{project}/{repository}")
    @Consumes(APPLICATION_JSON)
    public MicrosoftPullRequest createPullRequest(@PathParam("account") String account,
                                                  @PathParam("collection") String collection,
                                                  @PathParam("project") String project,
                                                  @PathParam("repository") String repository,
                                                  NewMicrosoftPullRequest input) throws IOException,
                                                                                        ServerException,
                                                                                        UnauthorizedException {
        String repositoryId = microsoftVstsRestClient.getRepository(account, collection, project, repository).getId();
        return microsoftVstsRestClient.createPullRequest(account, collection, repositoryId, input);
    }

    @PUT
    @Path("/pullrequests/{account}/{collection}/{project}/{repository}/{pullRequest}")
    @Consumes(APPLICATION_JSON)
    public MicrosoftPullRequest updatePullRequest(@PathParam("account") String account,
                                                  @PathParam("collection") String collection,
                                                  @PathParam("project") String project,
                                                  @PathParam("repository") String repository,
                                                  @PathParam("pullRequest") String pullRequestId,
                                                  MicrosoftPullRequest pullRequest) throws IOException,
                                                                                           ServerException,
                                                                                           UnauthorizedException {
        final String repositoryId = microsoftVstsRestClient.getRepository(account, collection, project, repository).getId();
        return microsoftVstsRestClient.updatePullRequests(account, collection, repositoryId, pullRequestId, pullRequest);
    }
}
