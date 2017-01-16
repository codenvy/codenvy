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
package com.codenvy.im.managers.helper;

import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.NodeConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/** @author Dmytro Nochevnov */
public class TestNodeConfigHelperCodenvy3 {

    @Mock
    private Config mockConfig;

    private static final String              TEST_NODE_DNS  = "localhost";
    private static final NodeConfig.NodeType TEST_NODE_TYPE = NodeConfig.NodeType.RUNNER;
    private static final NodeConfig          TEST_NODE      = new NodeConfig(TEST_NODE_TYPE, TEST_NODE_DNS);

    private static final String ADDITIONAL_RUNNERS_PROPERTY_NAME = "additional_runners";

    private NodeConfigHelperCodenvy3Impl spyConfigHelper;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        spyConfigHelper = spy(new NodeConfigHelperCodenvy3Impl(mockConfig));
    }

    @Test
    public void testRecognizeNodeTypeByDns() {
        Config testConfig = new Config(ImmutableMap.of(Config.ADDITIONAL_RUNNERS, "http://runner1.codenvy:8080/runner/internal/runner"));
        NodeConfigHelperCodenvy3Impl testConfigUtil = new NodeConfigHelperCodenvy3Impl(testConfig);

        assertEquals(testConfigUtil.recognizeNodeTypeFromConfigByDns("runner1.codenvy"), NodeConfig.NodeType.RUNNER);
    }

    @Test
    public void testGetPropertyNameByDns() {
        assertEquals(spyConfigHelper.getPropertyNameByType(NodeConfig.NodeType.RUNNER),
                     ADDITIONAL_RUNNERS_PROPERTY_NAME);
        assertEquals(spyConfigHelper.getPropertyNameByType(NodeConfig.NodeType.BUILDER),
                     "additional_builders");
    }

    @Test(dataProvider = "GetValueWithNode")
    public void testGetValueWithNode(List<String> additionalNodes, String addingNodeDns, String expectedResult) {
        doReturn(additionalNodes).when(mockConfig).getAllValuesWithoutSubstitution(ADDITIONAL_RUNNERS_PROPERTY_NAME,
                                                                String.valueOf(NodeConfigHelperCodenvy3Impl.NODE_DELIMITER));
        NodeConfig testNode = new NodeConfig(NodeConfig.NodeType.RUNNER, addingNodeDns);

        String result = spyConfigHelper.getValueWithNode(testNode);
        assertEquals(result, expectedResult);
    }

    @DataProvider(name = "GetValueWithNode")
    public static Object[][] GetValueWithNode() {
        return new Object[][]{
            {new ArrayList(), "test", "http://test:8080/runner/internal/runner"},
            {new ArrayList<>(ImmutableList.of("test1")), "test2", "test1,http://test2:8080/runner/internal/runner"}
        };
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Node '" + TEST_NODE_DNS + "' has been already used")
    public void testGetValueWithNodeWhenNodeExists() {
        List<String> additionalNodes = new ArrayList<>();
        additionalNodes.add(String.format("http://%s:8080/runner/internal/runner", TEST_NODE_DNS));
        doReturn(additionalNodes).when(mockConfig).getAllValues(ADDITIONAL_RUNNERS_PROPERTY_NAME,
                                                                String.valueOf(NodeConfigHelperCodenvy3Impl.NODE_DELIMITER));

        spyConfigHelper.getValueWithNode(TEST_NODE);
    }

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = "Node list property '" + ADDITIONAL_RUNNERS_PROPERTY_NAME + "' isn't found in Codenvy config")
    public void testGetValueWithNodeWithoutAdditionalNodesProperty() {
        doReturn(null).when(mockConfig).getAllValues(ADDITIONAL_RUNNERS_PROPERTY_NAME,
                                                     String.valueOf(NodeConfigHelperCodenvy3Impl.NODE_DELIMITER));
        spyConfigHelper.getValueWithNode(TEST_NODE);
    }

    @Test(dataProvider = "GetValueWithoutNode")
    public void testGetValueWithoutNode(List<String> additionalNodes, String removingNodeDns, String expectedResult) {
        doReturn(additionalNodes).when(mockConfig).getAllValues(ADDITIONAL_RUNNERS_PROPERTY_NAME,
                                                                String.valueOf(NodeConfigHelperCodenvy3Impl.NODE_DELIMITER));
        NodeConfig testNode = new NodeConfig(NodeConfig.NodeType.RUNNER, removingNodeDns);

        String result = spyConfigHelper.getValueWithoutNode(testNode);
        assertEquals(result, expectedResult);
    }

    @DataProvider(name = "GetValueWithoutNode")
    public static Object[][] GetValueWithoutNode() {
        return new Object[][]{
                {new ArrayList<>(ImmutableList.of("http://test:8080/runner/internal/runner")), "test", ""},
                {new ArrayList<>(ImmutableList.of(
                                      "http://test1:8080/runner/internal/runner",
                                      "http://test2:8080/runner/internal/runner",
                                      "http://test3:8080/runner/internal/runner"
                                  )),
                                  "test1",
                                  "http://test2:8080/runner/internal/runner,http://test3:8080/runner/internal/runner"},
                {new ArrayList<>(ImmutableList.of(
                                      "http://test1:8080/runner/internal/runner",
                                      "http://test2:8080/runner/internal/runner",
                                      "http://test3:8080/runner/internal/runner"
                                  )),
                                  "test2",
                                  "http://test1:8080/runner/internal/runner,http://test3:8080/runner/internal/runner"},
                {new ArrayList<>(ImmutableList.of(
                                "http://test1:8080/runner/internal/runner",
                                "http://test2:8080/runner/internal/runner",
                                "http://test3:8080/runner/internal/runner"
                            )),
                             "test3",
                             "http://test1:8080/runner/internal/runner,http://test2:8080/runner/internal/runner"}
        };
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "There is no node '" + TEST_NODE_DNS + "' in the list of nodes")
    public void testGetValueWithoutNodeWhenNodeIsNotExists() {
        doReturn(new ArrayList<>()).when(mockConfig).getAllValues(ADDITIONAL_RUNNERS_PROPERTY_NAME,
                                                                  String.valueOf(NodeConfigHelperCodenvy3Impl.NODE_DELIMITER));

        spyConfigHelper.getValueWithoutNode(TEST_NODE);
    }

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = "Node list property '" + ADDITIONAL_RUNNERS_PROPERTY_NAME + "' isn't found in Codenvy config")
    public void testGetValueWithoutNodeWithoutAdditionalNodesProperty() {
        doReturn(null).when(mockConfig).getAllValues(ADDITIONAL_RUNNERS_PROPERTY_NAME,
                                                     String.valueOf(NodeConfigHelperCodenvy3Impl.NODE_DELIMITER));
        spyConfigHelper.getValueWithoutNode(TEST_NODE);
    }

    @Test(dataProvider = "GetAdditionalNodeUrl")
    public void testGetAdditionalNodeUrl(NodeConfig node, String expectedResult) {
        assertEquals(spyConfigHelper.getNodeUrl(node), expectedResult);
    }

    @DataProvider(name = "GetAdditionalNodeUrl")
    public static Object[][] GetAdditionalNodeUrl() {
        return new Object[][]{
            {new NodeConfig(NodeConfig.NodeType.RUNNER, TEST_NODE_DNS), "http://localhost:8080/runner/internal/runner"},
            {new NodeConfig(NodeConfig.NodeType.BUILDER, TEST_NODE_DNS), "http://localhost:8080/builder/internal/builder"},
            {new NodeConfig(NodeConfig.NodeType.SITE, TEST_NODE_DNS), "http://localhost:8080/site/internal/site"},
        };
    }

    @Test(dataProvider = "RecognizeNodeConfigFromDns")
    public void testRecognizeNodeConfigFromDns(String dns, String baseBuilderNodeDomain, String baseRunnerNodeDomain, NodeConfig expectedResult) {
        doReturn(baseBuilderNodeDomain).when(mockConfig).getValue(NodeConfig.NodeType.BUILDER.toString().toLowerCase() + Config.NODE_HOST_PROPERTY_SUFFIX);
        doReturn(baseRunnerNodeDomain).when(mockConfig).getValue(NodeConfig.NodeType.RUNNER.toString().toLowerCase() + Config.NODE_HOST_PROPERTY_SUFFIX);
        assertEquals(spyConfigHelper.recognizeNodeConfigFromDns(dns), expectedResult);
    }

    @DataProvider
    public static Object[][] RecognizeNodeConfigFromDns() {
        return new Object[][]{
            {"runner123.dev.com", "builder1.dev.com", "runner1.dev.com", new NodeConfig(NodeConfig.NodeType.RUNNER, "runner123.dev.com")},
            {"builder123.com", "builder1.com", "runner1.com", new NodeConfig(NodeConfig.NodeType.BUILDER, "builder123.com")}
        };
    }

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = "Host name of base node of type 'BUILDER' wasn't found.")
    public void testRecognizeNodeConfigFromDnsWhenBaseNodeDomainUnknown() {
        spyConfigHelper.recognizeNodeConfigFromDns("some");
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Illegal DNS name 'runner2.another.com' of node. Correct DNS name templates: \\['builder<number>.some.com', 'runner<number>.some.com'\\]")
    public void testRecognizeNodeConfigFromDnsWhenDnsDoesNotComplyBaseNodeDomain() {
        doReturn("builder1.some.com").when(mockConfig).getValue(
                NodeConfig.NodeType.BUILDER.toString().toLowerCase() + Config.NODE_HOST_PROPERTY_SUFFIX);
        doReturn("runner1.some.com").when(mockConfig).getValue(NodeConfig.NodeType.RUNNER.toString().toLowerCase() + Config.NODE_HOST_PROPERTY_SUFFIX);
        spyConfigHelper.recognizeNodeConfigFromDns("runner2.another.com");
    }

    @Test
    public void testExtractAdditionalNodesDns() {
        ArrayList additionalNodes = new ArrayList<>(ImmutableList.of(
            "http://test1.dev.com/runner/internal/runner",
            "http://test-2.dev.com:8080/runner/internal/runner",
            "https://test3.dev.com:8080/runner/internal/runner",
            "wrong_address"
        ));

        doReturn(additionalNodes).when(mockConfig).getAllValues(ADDITIONAL_RUNNERS_PROPERTY_NAME,
                                                                String.valueOf(NodeConfigHelperCodenvy3Impl.NODE_DELIMITER));

        Map<String, List<String>> result = spyConfigHelper.extractNodesDns(NodeConfig.NodeType.RUNNER);
        assertEquals(result.toString(), "{additional_runners=[test1.dev.com, test-2.dev.com, test3.dev.com]}");
    }

    @Test
    public void testExtractAdditionalNodesDnsWhenPropertiesIsAbsent() {
        doReturn(null).when(mockConfig).getAllValues(ADDITIONAL_RUNNERS_PROPERTY_NAME,
                                                     String.valueOf(NodeConfigHelperCodenvy3Impl.NODE_DELIMITER));

        Map<String, List<String>> result = spyConfigHelper.extractNodesDns(NodeConfig.NodeType.RUNNER);
        assertNull(result);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetNodeNumber() {
        spyConfigHelper.getNodeNumber();
    }
}
