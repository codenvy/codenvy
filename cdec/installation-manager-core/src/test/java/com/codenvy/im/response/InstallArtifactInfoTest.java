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
 * @author Anatoliy Bazko
 */
public class InstallArtifactInfoTest {

    @Test
    public void test() throws Exception {
        InstallArtifactInfo info = InstallArtifactInfo.createInstance("codenvy",
                                                                      "1.0.1",
                                                                      VersionLabel.UNSTABLE,
                                                                      InstallArtifactInfo.Status.SUCCESS);

        String json = toJson(info);
        assertEquals(json, "{\n" +
                           "  \"artifact\" : \"codenvy\",\n" +
                           "  \"version\" : \"1.0.1\",\n" +
                           "  \"label\" : \"UNSTABLE\",\n" +
                           "  \"status\" : \"SUCCESS\"\n" +
                           "}");
        assertEquals(fromJson(json, InstallArtifactInfo.class), info);
    }
}
