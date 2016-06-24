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
package com.codenvy.im.managers;

import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

public class TestNodeConfig {
    private static final String              TEST_HOST     = "host";
    private static final int                 TEST_PORT     = 2222;
    private static final String              TEST_USER     = "user";
    private static final Path TEST_KEY_PATH = Paths.get("key");
    private static final NodeConfig.NodeType TEST_TYPE     = NodeConfig.NodeType.API;

    @Test
    public void testInstantiate() throws Exception {
        NodeConfig config = new NodeConfig(TEST_TYPE, TEST_HOST);
        assertEquals(config.getHost(), TEST_HOST);
        assertEquals(config.getType(), TEST_TYPE);

        config.setHost("another");
        assertEquals(config.getHost(), "another");

        config.setType(NodeConfig.NodeType.BUILDER);
        assertEquals(config.getType(), NodeConfig.NodeType.BUILDER);
    }

    @Test
    public void testPort() throws Exception {
        NodeConfig config = new NodeConfig(TEST_TYPE, TEST_HOST);
        assertEquals(config.getPort(), 22);

        config.setPort(TEST_PORT);
        assertEquals(config.getPort(), TEST_PORT);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Port number must be greater than zero")
    public void testIllegalPortArgument() throws Exception {
        NodeConfig config = new NodeConfig(TEST_TYPE, TEST_HOST);
        config.setPort(0);
    }

    @Test
    public void testUser() throws Exception {
        NodeConfig config = new NodeConfig(TEST_TYPE, TEST_HOST);
        assertNull(config.getUser());

        config.setUser(TEST_USER);
        assertEquals(config.getUser(), TEST_USER);
    }

    @Test
    public void testEqualsAndHashCode() {
        NodeConfig config1 = new NodeConfig(TEST_TYPE, TEST_HOST);
        config1.setUser(TEST_USER);
        config1.setPort(TEST_PORT);
        config1.setPrivateKeyFile(TEST_KEY_PATH);

        NodeConfig config2 = new NodeConfig(TEST_TYPE, TEST_HOST);
        config2.setUser(TEST_USER);
        config2.setPort(TEST_PORT);
        config2.setPrivateKeyFile(TEST_KEY_PATH);

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());

        config2.setType(NodeConfig.NodeType.BUILDER);
        assertNotEquals(config1, config2);
        assertNotEquals(config1.hashCode(), config2.hashCode());
        config2.setType(TEST_TYPE);

        config2.setHost("another1");
        assertNotEquals(config1, config2);
        assertNotEquals(config1.hashCode(), config2.hashCode());
        config2.setHost(TEST_HOST);

        config2.setUser("another2");
        assertNotEquals(config1, config2);
        assertNotEquals(config1.hashCode(), config2.hashCode());
        config2.setUser(TEST_USER);

        config2.setPort(123);
        assertNotEquals(config1, config2);
        assertNotEquals(config1.hashCode(), config2.hashCode());
        config1.setPort(TEST_PORT);

        config2.setPrivateKeyFile(Paths.get("another3"));
        assertNotEquals(config1, config2);
        assertNotEquals(config1.hashCode(), config2.hashCode());
        config2.setPrivateKeyFile(TEST_KEY_PATH);
    }

    @Test
    public void testPrivateKeyFileAbsolutePath() throws Exception {
        NodeConfig config = new NodeConfig(TEST_TYPE, TEST_HOST);
        assertEquals(config.getPrivateKeyFile().toString(), "~/.ssh/id_rsa");

        config.setPrivateKeyFile(TEST_KEY_PATH);
        assertEquals(config.getPrivateKeyFile(), TEST_KEY_PATH);
    }

    @Test
    public void testTypeEnum() throws Exception {
        assertEquals(Arrays.toString(NodeConfig.NodeType.values()), "[DATA, SITE, BUILDER, RUNNER, DATASOURCE, ANALYTICS, API, MACHINE_NODE]");
    }

    @Test
    public void testExtractConfigsFrom() throws Exception {
        Map<String, String> configProperties = new HashMap<String, String>() {{
            put("host_url", "dev.com");
            put("data_host_name", "data.dev.com");
            put("builder_host_name", "builder1.dev.com");
            put("runner_host_name", "runner1.dev.com");
            put("datasource_host_name", "datasource.dev.com");
            put("analytics_host_name", "analytics.dev.com");
            put("site_host_name", "site.dev.com");
            put("codeassistant_host_name", "codeassistant.dev.com");  // this is unknown node type 'codeassistant'
            put("puppet_master_host_name", "puppet-master.dev.com");
            put("api_host_name", "api.dev.com");
        }};
        Config config = new Config(configProperties);

        List<NodeConfig> result = NodeConfig.extractConfigsFrom(config);
        assertEquals(result.toString(), "[{'host':'data.dev.com', 'port':'22', 'privateKeyFile':'~/.ssh/id_rsa', 'type':'DATA'}, " +
                                        "{'host':'site.dev.com', 'port':'22', 'privateKeyFile':'~/.ssh/id_rsa', 'type':'SITE'}, " +
                                        "{'host':'builder1.dev.com', 'port':'22', 'privateKeyFile':'~/.ssh/id_rsa', 'type':'BUILDER'}, " +
                                        "{'host':'runner1.dev.com', 'port':'22', 'privateKeyFile':'~/.ssh/id_rsa', 'type':'RUNNER'}, " +
                                        "{'host':'datasource.dev.com', 'port':'22', 'privateKeyFile':'~/.ssh/id_rsa', 'type':'DATASOURCE'}, " +
                                        "{'host':'analytics.dev.com', 'port':'22', 'privateKeyFile':'~/.ssh/id_rsa', 'type':'ANALYTICS'}, " +
                                        "{'host':'api.dev.com', 'port':'22', 'privateKeyFile':'~/.ssh/id_rsa', 'type':'API'}]");
    }

    @Test
    public void testExtractConfigsWithEmptyNodeProperty() throws Exception {
        Map<String, String> configProperties = new HashMap<String, String>() {{
            put("host_url", "dev.com");
            put("data_host_name", "data.dev.com");
            put("api_host_name", "");   // should be ignored because of empty value
            put("builder_host_name", "builder1.dev.com");
        }};
        Config config = new Config(configProperties);

        List<NodeConfig> result = NodeConfig.extractConfigsFrom(config);
        assertEquals(result.toString(), "[" +
                                        "{'host':'data.dev.com', 'port':'22', 'privateKeyFile':'~/.ssh/id_rsa', 'type':'DATA'}, " +
                                        "{'host':'builder1.dev.com', 'port':'22', 'privateKeyFile':'~/.ssh/id_rsa', 'type':'BUILDER'}" +
                                        "]");
    }

}
