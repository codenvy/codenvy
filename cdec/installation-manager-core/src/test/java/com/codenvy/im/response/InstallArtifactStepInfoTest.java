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
public class InstallArtifactStepInfoTest {

    @Test
    public void test() throws Exception {
        InstallArtifactStepInfo info = new InstallArtifactStepInfo();
        info.setArtifact("codenvy");
        info.setVersion("1.0.1");
        info.setLabel(VersionLabel.STABLE);
        info.setStep(1);
        info.setStatus(InstallArtifactInfo.Status.IN_PROGRESS);
        info.setMessage("ok");

        String json = toJson(info);
        assertEquals(json, "{\n" +
                           "  \"artifact\" : \"codenvy\",\n" +
                           "  \"version\" : \"1.0.1\",\n" +
                           "  \"label\" : \"STABLE\",\n" +
                           "  \"message\" : \"ok\",\n" +
                           "  \"step\" : 1,\n" +
                           "  \"status\" : \"IN_PROGRESS\"\n" +
                           "}");
        assertEquals(fromJson(json, InstallArtifactStepInfo.class), info);

    }
}