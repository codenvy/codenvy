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
package com.codenvy.im.managers;

import com.codenvy.im.utils.OSUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.testng.annotations.AfterTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Dmytro Nochevnov
 */
public class TestConfig {
    public static final String INITIAL_OS_VERSION = OSUtils.VERSION;

    @AfterTest
    public void tearDown() {
        OSUtils.VERSION = INITIAL_OS_VERSION;
    }

    @Test
    public void testGetHostUrl() throws Exception {
        Config config = new Config(ImmutableMap.of(Config.HOST_URL, "host"));
        assertEquals(config.getHostUrl(), "host");

        config = new Config(ImmutableMap.of(Config.AIO_HOST_URL, "host"));
        assertEquals(config.getHostUrl(), "host");

        config = new Config(ImmutableMap.of(Config.HOST_URL, "host1", Config.AIO_HOST_URL, "host2"));
        assertEquals(config.getHostUrl(), "host1");
    }

    @Test
    public void testGetValueByVersion() throws Exception {
        Config config = new Config(Collections.<String, String>emptyMap());
        assertNotNull(config.getValue(Config.PUPPET_AGENT_PACKAGE));
        assertNotNull(config.getValue(Config.PUPPET_SERVER_PACKAGE));
        assertNotNull(config.getValue(Config.PUPPET_RESOURCE_URL));
    }

    @Test(dataProvider = "getValues")
    public void testGetValues(String propertyName, String propertyValue, String delimiter, String osVersion, List<String> expectedResult) {
        OSUtils.VERSION = osVersion;
        Config config = new Config(Collections.singletonMap(propertyName, propertyValue));
        List<String> result = config.getAllValuesWithoutSubstitution(propertyName, delimiter);
        assertEquals(result, expectedResult);
    }

    @DataProvider
    public Object[][] getValues() {
        return new Object[][]{
            {"property", null, ",", "7", null},
            {"property", "", ",", "7", new ArrayList<String>()},
            {"property", "value1", ",", "7", new ArrayList<>(ImmutableList.of("value1"))},
            {"property", "value1,value2", ",", "7", new ArrayList<>(ImmutableList.of("value1", "value2"))},
            {"property", "value1,value2,value3", ",", "7", new ArrayList<>(ImmutableList.of("value1", "value2", "value3"))},
            {Config.PUPPET_AGENT_PACKAGE, "", ",", "7", new ArrayList<>(ImmutableList.of("puppet-3.8.6-1.el7.noarch"))},

            {"property", "value1", "\n", "7", new ArrayList<>(ImmutableList.of("value1"))},
            {"property", "value1\n", "\n", "7", new ArrayList<>(ImmutableList.of("value1"))},
            {"property", "value1\nvalue2", "\n", "7", new ArrayList<>(ImmutableList.of("value1", "value2"))},
            {"property", "value1\nvalue2\nvalue3\n", "\n", "7", new ArrayList<>(ImmutableList.of("value1", "value2", "value3"))},
        };
    }

    @Test(dataProvider = "getEnclosedValue")
    public void testGetEnclosedValues(String propertyName, String osVersion, Config config, String expectedValue) {
        OSUtils.VERSION = osVersion;
        String result = config.getValue(propertyName);
        assertEquals(result, expectedValue);
    }

    @DataProvider
    public Object[][] getEnclosedValue() {
        Map<String, String> properties = ImmutableMap.of(
            "prop_1", "value_1.1,$prop_2,value_1.2",
            "prop_2", "$prop_3,$prop_4",                  // check on several variables enclosed into one value
            "prop_3", "$" + Config.PUPPET_AGENT_PACKAGE,  // Config.PUPPET_AGENT_PACKAGE depends on version
            "prop_4", "value_4"
        );

        Config config = new Config(properties);

        return new Object[][]{
            {"prop_1", null, config, String.format("value_1.1,$%s,value_4,value_1.2", Config.PUPPET_AGENT_PACKAGE)},  // enclosed variable remained as it because of osVersion = null
            {"prop_1", "7", config, "value_1.1,puppet-3.8.6-1.el7.noarch,value_4,value_1.2"}, // Config.PUPPET_AGENT_VERSION depends on version
            {"prop_2", "7", config, "puppet-3.8.6-1.el7.noarch,value_4"}                      // Config.PUPPET_AGENT_VERSION depends on version
            };
    }

    @Test
    public void testGetEnclosedCyclicValues() {
        Map<String, String> properties = ImmutableMap.of(
            "prop_1", "value_1.1,$prop_1,value_1.2",
            "prop_2", "value_2,$prop_3",
            "prop_3", "$prop_1"
        );

        Config config = new Config(properties);

        String result = config.getValue("prop_1");
        assertEquals(result, "value_1.1,$prop_1,value_1.2");
    }

    @Test
    public void testGetValueWithoutSubstitution() {
        Map<String, String> properties = ImmutableMap.of(
            "prop_1", "$prop_2",
            "prop_2", "value_2"
        );

        Config config = new Config(properties);

        String result = config.getValueWithoutSubstitution("prop_1");
        assertEquals(result, "$prop_2");
    }

    @Test
    public void shouldReturnPathToCodenvyConfigDir() {
        Map<String, String> propertiesOf4_3_0_RC1 = ImmutableMap.of(Config.VERSION, "4.3.0-RC1-SNAPSHOT");
        Config config = new Config(propertiesOf4_3_0_RC1);
        assertEquals(config.getPathToCodenvyConfigDir(), Paths.get("/home/codenvy/codenvy-data/conf"));

        Map<String, String> propertiesOf4_2_2 = ImmutableMap.of(Config.VERSION, "4.2.2");
        config = new Config(propertiesOf4_2_2);
        assertEquals(config.getPathToCodenvyConfigDir(), Paths.get("/home/codenvy/codenvy-data/cloud-ide-local-configuration"));
    }
}
