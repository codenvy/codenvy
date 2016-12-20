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
package org.eclipse.che.ide.ext.bitbucket.server;

import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketLink;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest.BitbucketPullRequestBranch;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest.BitbucketPullRequestLocation;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketServerPullRequest;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketServerPullRequest.BitbucketServerPullRequestRef;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketServerRepository;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketServerUser;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketServerProject;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketUser;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketUser.BitbucketUserLinks;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Adapter for BitbucketServer DTOs.
 *
 * @author Igor Vinokur
 */
public class BitbucketServerDTOConverter {

    static BitbucketUser convertToBitbucketUser(BitbucketServerUser bitbucketServerUser) {

        BitbucketLink bitbucketLink = newDto(BitbucketLink.class).withHref(bitbucketServerUser.getLinks().getSelf().get(0).getHref())
                                                                 .withName(bitbucketServerUser.getLinks().getSelf().get(0).getName());

        BitbucketUserLinks bitbucketUserLinks = newDto(BitbucketUserLinks.class).withSelf(bitbucketLink);

        return newDto(BitbucketUser.class).withUsername(bitbucketServerUser.getName())
                                          .withDisplayName(bitbucketServerUser.getDisplayName())
                                          .withUuid(bitbucketServerUser.getId()).withLinks(bitbucketUserLinks);
    }

    static BitbucketRepository convertToBitbucketRepository(BitbucketServerRepository bitbucketServerRepository) {

        BitbucketServerUser owner = bitbucketServerRepository.getProject().getOwner();
        newDto(BitbucketRepository.class).withName(bitbucketServerRepository.getName())
                                         .withParent(bitbucketServerRepository.getOrigin() == null ? null :
                                                     convertToBitbucketRepository(bitbucketServerRepository.getOrigin()))
                                         .withOwner(owner == null ? null : convertToBitbucketUser(owner));

        return bitbucketServerRepository;
    }

    static BitbucketPullRequest convertToBitbucketPullRequest(BitbucketServerPullRequest pullRequest) {
        BitbucketPullRequestBranch branch = newDto(BitbucketPullRequestBranch.class).withName(pullRequest.getFromRef().getDisplayId());
        BitbucketPullRequestLocation location = newDto(BitbucketPullRequestLocation.class).withBranch(branch);

        return newDto(BitbucketPullRequest.class).withDescription(pullRequest.getDescription())
                                                 .withTitle(pullRequest.getTitle())
                                                 .withAuthor(convertToBitbucketUser(pullRequest.getAuthor().getUser()))
                                                 .withId(pullRequest.getId()).withSource(location)
                                                 .withState(BitbucketPullRequest.State.valueOf(pullRequest.getState().toString()));
    }

    static BitbucketServerPullRequest convertToBitbucketServerPullRequest(BitbucketPullRequest pullRequest) {

        String[] source = pullRequest.getSource().getRepository().getFullName().split("/");
        String[] destination = pullRequest.getDestination().getRepository().getFullName().split("/");

        BitbucketServerRepository fromRepository =
                newDto(BitbucketServerRepository.class).withSlug(source[1])
                                                       .withProject(newDto(BitbucketServerProject.class).withKey("~" + source[0]));
        BitbucketServerRepository toRepository =
                newDto(BitbucketServerRepository.class).withSlug(destination[1])
                                                       .withProject(newDto(BitbucketServerProject.class).withKey("~" + destination[0]));

        BitbucketServerPullRequestRef pullRequestFromRef =
                newDto(BitbucketServerPullRequestRef.class).withId("refs/heads/" + pullRequest.getSource().getBranch().getName())
                                                           .withRepository(fromRepository);

        BitbucketServerPullRequestRef pullRequestToRef =
                newDto(BitbucketServerPullRequestRef.class).withId("refs/heads/" + pullRequest.getDestination().getBranch().getName())
                                                           .withRepository(toRepository);

        return newDto(BitbucketServerPullRequest.class).withId(pullRequest.getId())
                                                       .withTitle(pullRequest.getTitle())
                                                       .withDescription(pullRequest.getDescription())
                                                       .withFromRef(pullRequestFromRef)
                                                       .withToRef(pullRequestToRef);
    }
}
