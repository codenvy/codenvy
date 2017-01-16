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
package com.codenvy.im.response;

import com.codenvy.im.artifacts.VersionLabel;

import org.testng.annotations.Test;

import static com.codenvy.im.utils.Commons.fromJson;
import static com.codenvy.im.utils.Commons.toJson;
import static org.testng.Assert.assertEquals;

/**
 * @author Alexander Reshetnyak
 */
public class VersionArtifactInfoTest {

    @Test
    public void test() throws Exception {
        VersionArtifactInfo info = new VersionArtifactInfo();
        info.setArtifact("codenvy");
        info.setVersion("1.0.1");
        info.setLabel(VersionLabel.STABLE);
        info.setStatus("little bit text");

        AvailableVersionInfo availableVersionInfo = new AvailableVersionInfo();
        availableVersionInfo.setStable("1.0.2");
        availableVersionInfo.setUnstable("1.0.3");
        info.setAvailableVersion(availableVersionInfo);

        String json = toJson(info);
        assertEquals(json, "{\n" +
                           "  \"artifact\" : \"codenvy\",\n" +
                           "  \"version\" : \"1.0.1\",\n" +
                           "  \"label\" : \"STABLE\",\n" +
                           "  \"availableVersion\" : {\n" +
                           "    \"stable\" : \"1.0.2\",\n" +
                           "    \"unstable\" : \"1.0.3\"\n" +
                           "  },\n" +
                           "  \"status\" : \"little bit text\"\n" +
                           "}");
        assertEquals(fromJson(json, VersionArtifactInfo.class), info);
    }
}
