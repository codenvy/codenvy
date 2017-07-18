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
package com.codenvy.plugin.jenkins.webhooks;

import org.eclipse.che.inject.ConfigurationProperties;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link JenkinsConnector}.
 *
 * @author Igor Vinokur
 */
public class JenkinsConnectorTest {

    private JenkinsConnector jenkinsConnector;

    @BeforeMethod
    public void setup() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("env.CODENVY_JENKINS_CONNECTOR_CONNECTORID_FACTORY_ID", "factoryId");
        properties.put("env.CODENVY_JENKINS_CONNECTOR_CONNECTORID_URL", "http://user:password@jenkins.url");
        properties.put("env.CODENVY_JENKINS_CONNECTOR_CONNECTORID_JOB_NAME", "jobName");
        ConfigurationProperties configurationProperties = mock(ConfigurationProperties.class);
        when(configurationProperties.getProperties(eq("env.CODENVY_JENKINS_CONNECTOR_.+"))).thenReturn(properties);
        jenkinsConnector = new JenkinsConnector("http://jenkins.url", "jobName", configurationProperties);
    }

    @Test
    public void shouldUpdateConnectorWithUrlWithCredentialsFromProperties() throws Exception {
        //when
        JenkinsConnector jenkinsConnector = this.jenkinsConnector.updateUrlWithCredentials();

        //then
        assertEquals(jenkinsConnector.url, "http://user:password@jenkins.url");
    }

    @Test
    public void shouldGetRelatedFactoryIdFromProperties() throws Exception {
        //when
        String factoryId = jenkinsConnector.getFactoryId();

        //then
        assertEquals(factoryId, "factoryId");
    }
}
