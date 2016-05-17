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
package org.eclipse.che.ide.ext.bitbucket.server.rest;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.ssh.server.SshServiceClient;
import org.eclipse.che.api.ssh.shared.dto.GenerateSshPairRequest;
import org.eclipse.che.api.ssh.shared.model.SshPair;
import org.eclipse.che.ide.ext.bitbucket.server.Bitbucket;
import org.eclipse.che.ide.ext.bitbucket.server.BitbucketException;
import org.eclipse.che.ide.ext.bitbucket.server.BitbucketKeyUploader;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositoryFork;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketUser;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * REST service for Bitbucket.
 *
 * @author Kevin Pollet
 */
@Path("/bitbucket")
public class BitbucketService {
    private final Bitbucket            bitbucket;
    private final BitbucketKeyUploader bitbucketKeyUploader;
    private final SshServiceClient     sshServiceClient;

    @Inject
    public BitbucketService(@NotNull final Bitbucket bitbucket,
                            @NotNull final BitbucketKeyUploader bitbucketKeyUploader,
                            @NotNull final SshServiceClient sshServiceClient) {
        this.bitbucket = bitbucket;
        this.bitbucketKeyUploader = bitbucketKeyUploader;
        this.sshServiceClient = sshServiceClient;
    }

    /**
     * @see org.eclipse.che.ide.ext.bitbucket.server.Bitbucket#getUser()
     */
    @GET
    @Path("user")
    @Produces(APPLICATION_JSON)
    public BitbucketUser getUser() throws IOException, BitbucketException, ServerException {
        return bitbucket.getUser();
    }

    /**
     * @see org.eclipse.che.ide.ext.bitbucket.server.Bitbucket#getRepository(String, String)
     */
    @GET
    @Path("repositories/{owner}/{repositorySlug}")
    @Produces(APPLICATION_JSON)
    public BitbucketRepository getRepository(@PathParam("owner") final String owner,
                                             @PathParam("repositorySlug") final String repositorySlug)
            throws IOException, BitbucketException, ServerException {

        return bitbucket.getRepository(owner, repositorySlug);
    }

    /**
     * @see org.eclipse.che.ide.ext.bitbucket.server.Bitbucket#getRepositoryForks(String, String)
     */
    @GET
    @Path("repositories/{owner}/{repositorySlug}/forks")
    @Produces(APPLICATION_JSON)
    public List<BitbucketRepository> getRepositoryForks(@PathParam("owner") final String owner,
                                                        @PathParam("repositorySlug") final String repositorySlug)
            throws IOException, BitbucketException, ServerException {

        return bitbucket.getRepositoryForks(owner, repositorySlug);
    }

    /**
     * @see org.eclipse.che.ide.ext.bitbucket.server.Bitbucket#forkRepository(String, String, String, boolean)
     */
    @POST
    @Path("repositories/{owner}/{repositorySlug}/fork")
    @Produces(APPLICATION_JSON)
    public BitbucketRepositoryFork forkRepository(@PathParam("owner") final String owner,
                                                  @PathParam("repositorySlug") final String repositorySlug,
                                                  @QueryParam("forkName") final String forkName,
                                                  @QueryParam("isForkPrivate") @DefaultValue("false") final boolean isForkPrivate)
            throws IOException, BitbucketException, ServerException {

        return bitbucket.forkRepository(owner, repositorySlug, forkName, isForkPrivate);
    }

    /**
     * @see org.eclipse.che.ide.ext.bitbucket.server.Bitbucket#getRepositoryPullRequests(String, String)
     */
    @GET
    @Path("repositories/{owner}/{repositorySlug}/pullrequests")
    @Produces(APPLICATION_JSON)
    public List<BitbucketPullRequest> getRepositoryPullRequests(@PathParam("owner") final String owner,
                                                                @PathParam("repositorySlug") final String repositorySlug)
            throws IOException, BitbucketException, ServerException {

        return bitbucket.getRepositoryPullRequests(owner, repositorySlug);
    }

    /**
     * @see org.eclipse.che.ide.ext.bitbucket.server.Bitbucket#openPullRequest(String, String, org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest)
     */
    @POST
    @Path("repositories/{owner}/{repositorySlug}/pullrequests")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public BitbucketPullRequest openPullRequest(@PathParam("owner") final String owner,
                                                @PathParam("repositorySlug") final String repositorySlug,
                                                BitbucketPullRequest pullRequest)
            throws IOException, BitbucketException, ServerException {

        return bitbucket.openPullRequest(owner, repositorySlug, pullRequest);
    }

    @POST
    @Path("ssh-keys")
    public void generateAndUploadSSHKey() throws ServerException, UnauthorizedException {
        final String host = "bitbucket.org";
        SshPair sshPair = null;
        try {
            sshPair = sshServiceClient.getPair("git", host);
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
