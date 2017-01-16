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
public class UpdateArtifactInfoTest {


    @Test
    public void test() throws Exception {
        UpdateArtifactInfo info = new UpdateArtifactInfo();
        info.setLabel(VersionLabel.STABLE);
        info.setStatus(UpdateArtifactInfo.Status.AVAILABLE_TO_DOWNLOAD);
        info.setArtifact("codenvy");
        info.setVersion("1.0.1");

        String json = toJson(info);

        assertEquals(json, "{\n" +
                           "  \"artifact\" : \"codenvy\",\n" +
                           "  \"version\" : \"1.0.1\",\n" +
                           "  \"label\" : \"STABLE\",\n" +
                           "  \"status\" : \"AVAILABLE_TO_DOWNLOAD\"\n" +
                           "}");

        assertEquals(info, fromJson(json, UpdateArtifactInfo.class));
    }
}
