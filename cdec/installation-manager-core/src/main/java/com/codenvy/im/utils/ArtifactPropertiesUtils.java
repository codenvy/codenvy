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

import com.codenvy.im.artifacts.ArtifactNotFoundException;
import com.codenvy.im.artifacts.ArtifactProperties;

import org.eclipse.che.commons.json.JsonParseException;

import java.io.IOException;
import java.util.Map;

import static com.codenvy.im.utils.Commons.asMap;
import static com.codenvy.im.utils.Commons.combinePaths;

/** @author Dmytro Nochevnov */
public class ArtifactPropertiesUtils {

    /** Utility class so no public constructor. */
    private ArtifactPropertiesUtils() {

    }

    /** Checks if Artifact property {@link com.codenvy.im.artifacts.ArtifactProperties#AUTHENTICATION_REQUIRED_PROPERTY} is set to true */
    public static boolean isAuthenticationRequired(String artifactName,
                                                   String version,
                                                   HttpTransport transport,
                                                   String updateEndpoint) throws IOException, JsonParseException {
        Map properties = getArtifactProperties(artifactName, version, transport, updateEndpoint);
        return Boolean.valueOf((String)properties.get(ArtifactProperties.AUTHENTICATION_REQUIRED_PROPERTY));
    }

    /** Returns {@link com.codenvy.im.artifacts.ArtifactProperties#SUBSCRIPTION_PROPERTY} property */
    public static Object getSubscription(String artifactName,
                                         String version,
                                         HttpTransport transport,
                                         String updateEndpoint) throws IOException, JsonParseException {
        Map properties = getArtifactProperties(artifactName, version, transport, updateEndpoint);
        return properties.get(ArtifactProperties.SUBSCRIPTION_PROPERTY);
    }

    private static Map getArtifactProperties(String artifactName,
                                             String version,
                                             HttpTransport transport,
                                             String updateEndpoint) throws IOException, JsonParseException {
        String versionInfoServiceHref = combinePaths(updateEndpoint, "repository/properties/" + artifactName + "/" + version);
        Map properties = asMap(transport.doGet(versionInfoServiceHref));
        if (properties == null) {
            throw new ArtifactNotFoundException(artifactName, version);
        }
        return properties;
    }

}
