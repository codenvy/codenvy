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
package com.codenvy.im.artifacts;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/** @author Dmytro Nochevnov */
public class TestArtifactProperties {

    @Test
    public void testArtifactProperties() {
        assertEquals(ArtifactProperties.VERSION_PROPERTY, "version");
        assertEquals(ArtifactProperties.PREVIOUS_VERSION_PROPERTY, "previous-version");
        assertEquals(ArtifactProperties.ARTIFACT_PROPERTY , "artifact");
        assertEquals(ArtifactProperties.BUILD_TIME_PROPERTY , "build-time");
        assertEquals(ArtifactProperties.FILE_NAME_PROPERTY, "file");
        assertEquals(ArtifactProperties.AUTHENTICATION_REQUIRED_PROPERTY, "authentication-required");
        assertEquals(ArtifactProperties.SUBSCRIPTION_PROPERTY , "subscription");
        assertEquals(ArtifactProperties.DESCRIPTION_PROPERTY, "description");
        assertEquals(ArtifactProperties.MD5_PROPERTY, "md5");
        assertEquals(ArtifactProperties.SIZE_PROPERTY , "size");
    }

    @Test
    public void testPublicProperties() {
        assertEquals(ArtifactProperties.PUBLIC_PROPERTIES.toString(),
                     "[version, label, previous-version, artifact, build-time, authentication-required, file, md5, size, description]");
    }
}
