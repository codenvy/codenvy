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
package com.codenvy.plugin.gitlab.factory.resolver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.mockito.InjectMocks;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test of {@Link GitlabUrl} Note: The parser is also testing the object
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class GitlabUrlTest {

  /** Parser used to create the url. */
  @InjectMocks private GitlabURLParserImpl gitlabUrlParser;

  /** Instance of the url created */
  private GitlabUrl gitlabUrl;

  /** Setup objects/ */
  @BeforeClass
  protected void init() {
    this.gitlabUrl = this.gitlabUrlParser.parse("https://gitlab.com/eclipse/che");
    assertNotNull(this.gitlabUrl);
  }

  /** Check when there is .codenvy.dockerfile in the repository */
  @Test
  public void checkDockerfileLocation() {
    assertEquals(
        gitlabUrl.dockerFileLocation(),
        "https://gitlab.com/eclipse/che/raw/master/.factory.dockerfile");
  }

  /** Check when there is .codenvy.json file in the repository */
  @Test
  public void checkCodenvyFactoryJsonFileLocation() {
    assertEquals(
        gitlabUrl.factoryJsonFileLocation(),
        "https://gitlab.com/eclipse/che/raw/master/.factory.json");
  }

  /** Check the original repository */
  @Test
  public void checkRepositoryLocation() {
    assertEquals(gitlabUrl.repositoryLocation(), "https://gitlab.com/eclipse/che.git");
  }
}
