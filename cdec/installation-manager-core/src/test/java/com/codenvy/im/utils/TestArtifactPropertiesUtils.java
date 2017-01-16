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
package com.codenvy.im.utils;

import com.codenvy.im.artifacts.InstallManagerArtifact;

import org.eclipse.che.commons.json.JsonParseException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/** @author Dmytro Nochevnov */
public class TestArtifactPropertiesUtils {
    @Mock private HttpTransport mockTransport;
    
    @BeforeMethod
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testAuthenticationRequiredProperty() throws IOException, JsonParseException {
        when(mockTransport.doGet("/repository/properties/" + InstallManagerArtifact.NAME + "/1.0.1"))
        .thenReturn("{"
                    + "\"artifact\":\"" + InstallManagerArtifact.NAME + "\","
                    + "\"version\":\"2.0.4\","
                    + "\"authentication-required\":\"true\","
                    + "\"subscription\":\"OnPremises\""
                    + "}");
    
        assertEquals(ArtifactPropertiesUtils.isAuthenticationRequired(InstallManagerArtifact.NAME, "1.0.1", mockTransport, ""), true);
    }
    
    @Test
    public void testSubsctiptionProperty() throws IOException, JsonParseException {
        when(mockTransport.doGet("/repository/properties/" + InstallManagerArtifact.NAME + "/1.0.1"))
        .thenReturn("{"
                    + "\"artifact\":\"" + InstallManagerArtifact.NAME + "\","
                    + "\"version\":\"2.0.4\","
                    + "\"authentication-required\":\"true\","
                    + "\"subscription\":\"OnPremises\""
                    + "}");

        assertEquals(ArtifactPropertiesUtils.getSubscription(InstallManagerArtifact.NAME, "1.0.1", mockTransport, ""), "OnPremises");
    }    
}
