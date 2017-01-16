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
package com.codenvy.plugin.urlfactory;

import com.google.common.base.Strings;

import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import static com.codenvy.plugin.urlfactory.URLFetcher.MAXIMUM_READ_BYTES;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Testing {@link URLFetcher}
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class URLFetcherTest {

    /**
     * Instance to test.
     */
    @InjectMocks
    private URLFetcher URLFetcher;


    /**
     * Check that when url is null, NPE is thrown
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void checkNullURL() {
        URLFetcher.fetch((String) null);
    }

    /**
     * Check that when url exists the content is retrieved
     */
    @Test
    public void checkGetContent() {

        // test to download this class object
        URL urlJson =
                URLFetcherTest.class.getResource("/" + URLFetcherTest.class.getPackage().getName().replace('.', '/') + "/.codenvy.json");
        Assert.assertNotNull(urlJson);

        String content = URLFetcher.fetch(urlJson.toString());
        assertEquals(content, "Hello");
    }


    /**
     * Check when url is invalid
     */
    @Test
    public void checkUrlFileIsInvalid() {
        String result = URLFetcher.fetch("hello world");
        assertNull(result);
    }


    /**
     * Check that when url doesn't exist
     */
    @Test
    public void checkMissingContent() {

        // test to download this class object
        URL urlJson =
                URLFetcherTest.class.getResource("/" + URLFetcherTest.class.getPackage().getName().replace('.', '/') + "/.codenvy.json");
        Assert.assertNotNull(urlJson);

        // add extra path to make url not found
        String content = URLFetcher.fetch(urlJson.toString() + "-invalid");
        assertNull(content);
    }


    /**
     * Check when we reach custom limit
     */
    @Test
    public void checkPartialContent() {
        URL urlJson =
                URLFetcherTest.class.getResource("/" + URLFetcherTest.class.getPackage().getName().replace('.', '/') + "/.codenvy.json");
        Assert.assertNotNull(urlJson);

        String content = new OneByteURLFetcher().fetch(urlJson.toString());
        assertEquals(content, "Hello".substring(0, 1));
    }

    /**
     * Check when we reach custom limit
     */
    @Test
    public void checkDefaultPartialContent() throws IOException {
        URLConnection urlConnection = Mockito.mock(URLConnection.class);
        String originalContent = Strings.padEnd("", (int) MAXIMUM_READ_BYTES, 'a');
        String extraContent = originalContent + "----";
        when(urlConnection.getInputStream()).thenReturn( new ByteArrayInputStream(extraContent.getBytes(UTF_8)));
        String readcontent = URLFetcher.fetch(urlConnection);
        // check extra content has been removed as we keep only first values
        assertEquals(readcontent, originalContent);
    }

    /**
     * Limit to only one Byte.
     */
    class OneByteURLFetcher extends URLFetcher {

        /**
         * Override the limit
         */
        protected long getLimit() {
            return 1;
        }
    }

}
