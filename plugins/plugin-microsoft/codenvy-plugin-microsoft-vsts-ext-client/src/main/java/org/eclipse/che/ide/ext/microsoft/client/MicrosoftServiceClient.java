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
package org.eclipse.che.ide.ext.microsoft.client;

import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.ext.microsoft.shared.dto.MicrosoftPullRequest;
import org.eclipse.che.ide.ext.microsoft.shared.dto.MicrosoftRepository;
import org.eclipse.che.ide.ext.microsoft.shared.dto.MicrosoftUserProfile;
import org.eclipse.che.ide.ext.microsoft.shared.dto.NewMicrosoftPullRequest;

import java.util.List;

/**
 * @author Mihail Kuznyetsov
 * @author Anton Korneta
 */
@Singleton
public interface MicrosoftServiceClient {

    Promise<MicrosoftRepository> getRepository(String account,
                                               String collection,
                                               String project,
                                               String repository);

    Promise<List<MicrosoftPullRequest>> getPullRequests(String account,
                                                        String collection,
                                                        String project,
                                                        String repository);

    Promise<MicrosoftPullRequest> createPullRequest(String account,
                                                    String collection,
                                                    String project,
                                                    String repository,
                                                    NewMicrosoftPullRequest pullRequest);

    Promise<MicrosoftPullRequest> updatePullRequest(String account,
                                                    String collection,
                                                    String project,
                                                    String repository,
                                                    String pullRequestId,
                                                    MicrosoftPullRequest pullRequest);

    Promise<MicrosoftUserProfile> getUserProfile();
}
