/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.plugin.pullrequest.client;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.bitbucket.client.BitbucketClientService;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Mihail Kuznyetsov */
@Listeners(MockitoTestNGListener.class)
public class BitbucketHostingServiceTest {

  @Mock private AppContext appContext;

  @Mock private DtoFactory dtoFactory;
  @Mock private BitbucketClientService bitbucketClientService;
  @Mock private BitBucketTemplates bitBucketTemplates;
  @Mock private BitBucketServerTemplates bitBucketServerTemplates;

  private BitbucketHostingService bitbucketHostingService;

  @BeforeMethod
  public void setUp() {
    Promise promise = mock(Promise.class);
    when(bitbucketClientService.getBitbucketEndpoint()).thenReturn(promise);
    bitbucketHostingService =
        new BitbucketHostingService(
            appContext,
            dtoFactory,
            bitbucketClientService,
            bitBucketTemplates,
            bitBucketServerTemplates,
            "");
  }

  @DataProvider(name = "repoNames")
  public static Object[][] repoNames() {
    return new Object[][] {
      {"https://bitbucket.org/testuser/testrepo", "testrepo"},
      {"https://bitbucket.org/testuser/testrepo.git", "testrepo"},
      {"git@bitbucket.org:testuser/ssh-repo", "ssh-repo"},
      {"git@bitbucket.org:testuser/ssh-repo.git", "ssh-repo"}
    };
  }

  @DataProvider(name = "repoOwners")
  public static Object[][] repoOwners() {
    return new Object[][] {
      {"https://bitbucket.org/testuser/testrepo", "testuser"},
      {"https://bitbucket.org/testuser/testrepo.git", "testuser"},
      {"git@bitbucket.org:testuser/ssh-repo", "testuser"},
      {"git@bitbucket.org:testuser/ssh-repo.git", "testuser"}
    };
  }

  @Test(dataProvider = "repoNames")
  public void shouldGetRepositoryNameFromUrl(String remoteUrl, String name) {
    Assert.assertEquals(bitbucketHostingService.getRepositoryNameFromUrl(remoteUrl), name);
  }

  @Test(dataProvider = "repoOwners")
  public void shouldGetRepositoryOwnerFromUrl(String remoteUrl, String owner) {
    Assert.assertEquals(bitbucketHostingService.getRepositoryOwnerFromUrl(remoteUrl), owner);
  }
}
