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
package com.codenvy.onpremises.factory;


import com.codenvy.onpremises.factory.filter.UserAgent;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Vitaliy Guliy
 */
public class UserAgentTest {

    String FIREFOX_ON_UBUNTU_X64 = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:28.0) Gecko/20100101 Firefox/28.0";

    String CHROME_ON_UBUNTU_X64 = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.115 Safari/537.36";


    String INTERNET_EXPLORER_ON_WINDOWS = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0)";

    String CHROME_ON_WINDOWS = "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36";

    String SAFARI_ON_WINDOWS = "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/534.57.2 (KHTML, like Gecko) Version/5.1.7 Safari/534.57.2";

    String FIREFOX_ON_WINDOWS = "Mozilla/5.0 (Windows NT 5.1; rv:28.0) Gecko/20100101 Firefox/28.0";


    String MOBILE_BROWSER_ON_ANDROID = "Mozilla/5.0 (Linux; U; Android 2.3.3; ru-ru; Fly_IQ280 Build/GINGERBREAD) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";


    String SAFARI_ON_IPHONE = "Mozilla/5.0 (iPhone; CPU iPhone OS 7_1 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Version/7.0 Mobile/11D169 Safari/9537.53";


    @Test
    public void testFirefoxOnUbuntu() {
        UserAgent agent = new UserAgent(FIREFOX_ON_UBUNTU_X64);

        Assert.assertEquals(true, agent.hasProduct("Mozilla"));
        Assert.assertEquals(true, agent.hasProduct("Firefox"));
        Assert.assertEquals(false, agent.hasProduct("Chrome"));

        Assert.assertEquals(true, agent.hasComment("Ubuntu"));
        Assert.assertEquals(true, agent.hasComment("Linux x86_64"));
    }

    @Test
    public void testChromeOnUbuntu() {
        UserAgent agent = new UserAgent(CHROME_ON_UBUNTU_X64);

        Assert.assertEquals(true, agent.hasProduct("Mozilla"));
        Assert.assertEquals(true, agent.hasProduct("Chrome"));
        Assert.assertEquals(false, agent.hasProduct("Firefox"));

        Assert.assertEquals(true, agent.hasComment("Linux x86_64"));
    }


    @Test
    public void testInternetExplorerOnWindow() {
        UserAgent agent = new UserAgent(INTERNET_EXPLORER_ON_WINDOWS);

        Assert.assertEquals(true, agent.hasProduct("Mozilla"));
        Assert.assertEquals(true, agent.hasCommentPart("MSIE"));
        Assert.assertEquals(true, agent.hasCommentPart("Windows"));
        Assert.assertEquals(true, agent.hasCommentPart("Trident"));
    }

    @Test
    public void testMobileBrowserOnAndroid() {
        UserAgent agent = new UserAgent(MOBILE_BROWSER_ON_ANDROID);

        Assert.assertEquals(true, agent.hasProduct("Mozilla"));
        Assert.assertEquals(true, agent.hasProduct("Mobile"));

        Assert.assertEquals(true, agent.hasCommentPart("Linux"));
        Assert.assertEquals(true, agent.hasCommentPart("Android"));
        Assert.assertEquals(true, agent.hasCommentPart("Linux"));
    }

    @Test
    public void testUserAgentParseProductWithSplitter() {
        String product = "Chrome/";

        UserAgent agent = new UserAgent(product);

        Assert.assertTrue(agent.hasProduct("Chrome"));
    }

    @Test
    public void testUserAgentParseProductOnly() {
        String product = "Chrome";

        UserAgent agent = new UserAgent(product);

        Assert.assertTrue(agent.hasProduct("Chrome"));
    }

}
