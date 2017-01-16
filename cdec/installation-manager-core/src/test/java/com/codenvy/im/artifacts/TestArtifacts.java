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

import com.codenvy.im.utils.Commons;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.testng.Assert.assertEquals;

/** @author Anatoliy Bazko */
public class TestArtifacts {
    private Artifact cdecArtifact;
    private Artifact imArtifact;

    @BeforeMethod
    public void setUp() throws Exception {
        cdecArtifact = ArtifactFactory.createArtifact(CDECArtifact.NAME);
        imArtifact = ArtifactFactory.createArtifact(InstallManagerArtifact.NAME);
    }

    @Test
    public void testOrder() throws Exception {
        Set<Artifact> artifacts = new Commons.ArtifactsSet(new HashSet<Artifact>() {{
            add(cdecArtifact);
            add(imArtifact);
        }});

        Iterator<Artifact> iter = artifacts.iterator();
        assertEquals(iter.next(), cdecArtifact);
        assertEquals(iter.next(), imArtifact);
    }
}
