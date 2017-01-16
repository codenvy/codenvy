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

import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.artifacts.InstallManagerArtifact;
import com.codenvy.im.artifacts.VersionLabel;

import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import static com.codenvy.im.utils.Commons.fromJson;
import static com.codenvy.im.utils.Commons.toJson;
import static org.testng.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 */
public class ArtifactInfoTest {
    @Test
    public void test() throws Exception {
        ArtifactInfo info = new ArtifactInfo();
        info.setArtifact("codenvy");
        info.setVersion("1.0.1");
        info.setLabel(VersionLabel.STABLE);
        info.setStatus(ArtifactInfo.Status.INSTALLED);

        String json = toJson(info);
        assertEquals(json, "{\n" +
                           "  \"artifact\" : \"codenvy\",\n" +
                           "  \"version\" : \"1.0.1\",\n" +
                           "  \"label\" : \"STABLE\",\n" +
                           "  \"status\" : \"INSTALLED\"\n" +
                           "}");
        assertEquals(fromJson(json, ArtifactInfo.class), info);
    }

    @Test
    public void testSort() throws Exception {
        ArtifactInfo info1 = new ArtifactInfo();
        info1.setArtifact(CDECArtifact.NAME);
        info1.setVersion("1.0.1");

        ArtifactInfo info2 = new ArtifactInfo();
        info2.setArtifact(CDECArtifact.NAME);
        info2.setVersion("1.0.2");

        ArtifactInfo info3 = new ArtifactInfo();
        info3.setArtifact(InstallManagerArtifact.NAME);
        info3.setVersion("1.0.1");

        Set<ArtifactInfo> infos = new TreeSet<>();
        infos.add(info1);
        infos.add(info2);
        infos.add(info3);


        Iterator<ArtifactInfo> iterator = infos.iterator();
        ArtifactInfo info = iterator.next();
        assertEquals(info.getArtifact(), CDECArtifact.NAME);
        assertEquals(info.getVersion(), "1.0.2");

        info = iterator.next();
        assertEquals(info.getArtifact(), CDECArtifact.NAME);
        assertEquals(info.getVersion(), "1.0.1");

        info = iterator.next();
        assertEquals(info.getArtifact(), InstallManagerArtifact.NAME);
        assertEquals(info.getVersion(), "1.0.1");
    }
}