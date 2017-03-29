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
package org.eclipse.che.ide.ext.bitbucket.server.rest;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.ssh.shared.dto.GenerateSshPairRequest;
import org.eclipse.che.api.ssh.shared.model.SshPair;
import org.eclipse.che.ide.ext.bitbucket.server.BitbucketConnection;
import org.eclipse.che.ide.ext.bitbucket.server.BitbucketException;
import org.eclipse.che.ide.ext.bitbucket.server.BitbucketKeyUploader;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositoryFork;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketUser;
import org.eclipse.che.plugin.ssh.key.SshServiceClient;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * REST service for Bitbucket.
 *
 * @author Kevin Pollet
 * @author Igor Vinokur
 */
@Path("/bitbucket")
public class BitbucketService extends Service {

    private final BitbucketConnection  bitbucketConnection;
    private final BitbucketKeyUploader bitbucketKeyUploader;
    private final SshServiceClient     sshServiceClient;

    @Inject
    public BitbucketService(BitbucketConnection bitbucketConnection,
                            BitbucketKeyUploader bitbucketKeyUploader,
                            SshServiceClient sshServiceClient) {
        this.bitbucketConnection = bitbucketConnection;
        this.bitbucketKeyUploader = bitbucketKeyUploader;
        this.sshServiceClient = sshServiceClient;
    }

    @GET
    @Path("user")
    @Produces(APPLICATION_JSON)
    public BitbucketUser getUser() throws IOException, BitbucketException, ServerException {
        return bitbucketConnection.getUser();
    }

    @GET
    @Path("repositories/{owner}/{repositorySlug}")
    @Produces(APPLICATION_JSON)
    public BitbucketRepository getRepository(@PathParam("owner") String owner,
                                             @PathParam("repositorySlug") String repositorySlug) throws IOException,
                                                                                                        BitbucketException,
                                                                                                        ServerException {
        return bitbucketConnection.getRepository(owner, repositorySlug);
    }

    @GET
    @Path("repositories/{owner}/{repositorySlug}/forks")
    @Produces(APPLICATION_JSON)
    public List<BitbucketRepository> getRepositoryForks(@PathParam("owner") String owner,
                                                        @PathParam("repositorySlug") String repositorySlug) throws IOException,
                                                                                                                   BitbucketException,
                                                                                                                   ServerException {
        return bitbucketConnection.getRepositoryForks(owner, repositorySlug);
    }

    @POST
    @Path("repositories/{owner}/{repositorySlug}/fork")
    @Produces(APPLICATION_JSON)
    public BitbucketRepositoryFork forkRepository(@PathParam("owner") String owner,
                                                  @PathParam("repositorySlug") String repositorySlug,
                                                  @QueryParam("forkName") String forkName,
                                                  @QueryParam("isForkPrivate") @DefaultValue("false") final boolean isForkPrivate)
            throws IOException, BitbucketException, ServerException, BadRequestException {

        if (isNullOrEmpty(forkName)) {
            throw new BadRequestException("Fork name is required");
        }
        return bitbucketConnection.forkRepository(owner, repositorySlug, forkName, isForkPrivate);
    }

    @GET
    @Path("repositories/{owner}/{repositorySlug}/pullrequests")
    @Produces(APPLICATION_JSON)
    public List<BitbucketPullRequest> getRepositoryPullRequests(@PathParam("owner") String owner,
                                                                @PathParam("repositorySlug") String repositorySlug)
            throws IOException, BitbucketException, ServerException {

        return bitbucketConnection.getRepositoryPullRequests(owner, repositorySlug);
    }

    @POST
    @Path("repositories/{owner}/{repositorySlug}/pullrequests")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public BitbucketPullRequest openPullRequest(@PathParam("owner") String owner,
                                                @PathParam("repositorySlug") String repositorySlug,
                                                BitbucketPullRequest pullRequest) throws IOException,
                                                                                         BitbucketException,
                                                                                         ServerException {
        return bitbucketConnection.openPullRequest(owner, repositorySlug, pullRequest);
    }

    @PUT
    @Path("repositories/{owner}/{repositorySlug}/pullrequests")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public BitbucketPullRequest updatePullRequest(@PathParam("owner") String owner,
                                                  @PathParam("repositorySlug") String repositorySlug,
                                                  BitbucketPullRequest pullRequest) throws IOException,
                                                                                           BitbucketException,
                                                                                           ServerException {
        return bitbucketConnection.updatePullRequest(owner, repositorySlug, pullRequest);
    }

    @POST
    @Path("ssh-keys")
    public void generateAndUploadSSHKey() throws ServerException, UnauthorizedException {
        final String host = "bitbucket.org";
        SshPair sshPair = null;
        try {
            sshPair = sshServiceClient.getPair("vcs", host);
        } catch (NotFoundException ignored) {
        }

        if (sshPair != null) {
            if (sshPair.getPublicKey() == null) {
                try {
                    sshServiceClient.removePair("git", host);
                } catch (NotFoundException ignored) {
                }

                sshPair = sshServiceClient.generatePair(newDto(GenerateSshPairRequest.class).withService("git")
                                                                                            .withName(host));
            }
        } else {
            sshPair = sshServiceClient.generatePair(newDto(GenerateSshPairRequest.class).withService("git")
                                                                                        .withName(host));
        }

        // update public key
        try {
            bitbucketKeyUploader.uploadKey(sshPair.getPublicKey());
        } catch (final IOException e) {
            throw new GitException(e);
        }
    }
}
