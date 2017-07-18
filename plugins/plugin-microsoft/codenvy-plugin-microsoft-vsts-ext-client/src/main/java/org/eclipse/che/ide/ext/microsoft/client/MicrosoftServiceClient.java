/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
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
