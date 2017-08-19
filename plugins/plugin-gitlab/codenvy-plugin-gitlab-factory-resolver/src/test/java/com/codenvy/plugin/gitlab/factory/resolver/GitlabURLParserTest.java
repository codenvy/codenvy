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
import static org.testng.Assert.assertTrue;

import org.mockito.InjectMocks;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Validate operations performed by the Github parser
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class GitlabURLParserTest {

  /** Instance of component that will be tested. */
  @InjectMocks private GitlabURLParserImpl gitlabUrlParser;

  /** Check invalid url (not a gitlab one) */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void invalidUrl() {
    gitlabUrlParser.parse("http://www.eclipse.org");
  }

  /** Check URLs are valid with regexp */
  @Test(dataProvider = "UrlsProvider")
  public void checkRegexp(String url) {
    assertTrue(gitlabUrlParser.isValid(url), "url " + url + " is invalid");
  }

  /** Compare parsing */
  @Test(dataProvider = "parsing")
  public void checkParsing(
      String url, String username, String repository, String branch, String subfolder) {
    GitlabUrl gitlabUrl = gitlabUrlParser.parse(url);

    assertEquals(gitlabUrl.getUsername(), username);
    assertEquals(gitlabUrl.getRepository(), repository);
    assertEquals(gitlabUrl.getBranch(), branch);
    assertEquals(gitlabUrl.getSubfolder(), subfolder);
  }

  @DataProvider(name = "UrlsProvider")
  public Object[][] urls() {
    return new Object[][] {
      {"https://gitlab.com/benoitf/simple-project"},
      {"https://gitlab.com/benoitf/simple-project/tree/master"},
      {"https://gitlab.com/benoitf/che/tree/master/"},
      {"https://gitlab.com/benoitf/che/tree/master/dashboard/"},
      {"https://gitlab.com/benoitf/che/tree/master/plugins/plugin-git/che-plugin-git-ext-git"},
      {"https://gitlab.com/benoitf/che/tree/master/plugins/plugin-git/che-plugin-git-ext-git/"}
    };
  }

  @DataProvider(name = "parsing")
  public Object[][] expectedParsing() {
    return new Object[][] {
      {"https://gitlab.com/eclipse/che", "eclipse", "che", "master", null},
      {"https://gitlab.com/eclipse/che/tree/4.2.x", "eclipse", "che", "4.2.x", null},
      {
        "https://gitlab.com/eclipse/che/tree/master/dashboard/",
        "eclipse",
        "che",
        "master",
        "dashboard/"
      },
      {
        "https://gitlab.com/eclipse/che/tree/master/plugins/plugin-git/che-plugin-git-ext-git",
        "eclipse",
        "che",
        "master",
        "plugins/plugin-git/che-plugin-git-ext-git"
      }
    };
  }
}
