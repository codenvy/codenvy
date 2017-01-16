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
package com.codenvy.plugin.github.factory.resolver;

import com.codenvy.plugin.github.factory.resolver.GithubUrl;
import com.codenvy.plugin.github.factory.resolver.GithubUrlParser;

import org.mockito.InjectMocks;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Validate operations performed by the Github parser
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class GithubUrlParserTest {

    /**
     * Instance of component that will be tested.
     */
    @InjectMocks
    private GithubUrlParser githubUrlParser;

    /**
     * Check invalid url (not a github one)
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void invalidUrl() {
        githubUrlParser.parse("http://www.eclipse.org");
    }

    /**
     * Check URLs are valid with regexp
     */
    @Test(dataProvider = "UrlsProvider")
    public void checkRegexp(String url) {
        assertTrue(githubUrlParser.isValid(url), "url " + url + " is invalid");
    }

    /**
     * Compare parsing
     */
    @Test(dataProvider = "parsing")
    public void checkParsing(String url, String username, String repository, String branch, String subfolder) {
        GithubUrl githubUrl = githubUrlParser.parse(url);

        assertEquals(githubUrl.username(), username);
        assertEquals(githubUrl.repository(), repository);
        assertEquals(githubUrl.branch(), branch);
        assertEquals(githubUrl.subfolder(), subfolder);
    }

    @DataProvider(name = "UrlsProvider")
    public Object[][] urls() {
        return new Object[][]{
                {"https://github.com/eclipse/che"},
                {"https://github.com/eclipse/che/tree/4.2.x"},
                {"https://github.com/eclipse/che/tree/master/"},
                {"https://github.com/eclipse/che/tree/master/dashboard/"},
                {"https://github.com/eclipse/che/tree/master/plugins/plugin-git/che-plugin-git-ext-git"},
                {"https://github.com/eclipse/che/tree/master/plugins/plugin-git/che-plugin-git-ext-git/"}
        };
    }

    @DataProvider(name = "parsing")
    public Object[][] expectedParsing() {
        return new Object[][]{
                {"https://github.com/eclipse/che", "eclipse", "che", "master", null},
                {"https://github.com/eclipse/che/tree/4.2.x", "eclipse", "che", "4.2.x", null},
                {"https://github.com/eclipse/che/tree/master/dashboard/", "eclipse", "che", "master", "dashboard/"},
                {"https://github.com/eclipse/che/tree/master/plugins/plugin-git/che-plugin-git-ext-git", "eclipse", "che", "master",
                 "plugins/plugin-git/che-plugin-git-ext-git"}
        };
    }


}
