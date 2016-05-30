/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.machine.authentication.server;

import org.eclipse.che.api.core.NotFoundException;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link MachineTokenRegistry}.
 *
 * @author Yevhenii Voevodin
 */
public class MachineTokenRegistryTest {

    @Test
    public void removeTokensShouldReturnUserToTokenMap() throws Exception {
        final MachineTokenRegistry registry = new MachineTokenRegistry();

        final Map<String, String> userToToken = new HashMap<>();
        userToToken.put("user1", registry.generateToken("user1", "workspace123"));
        userToToken.put("user2", registry.generateToken("user2", "workspace123"));
        userToToken.put("user3", registry.generateToken("user3", "workspace123"));
        registry.generateToken("user1", "workspace234");


        final Map<String, String> removedTokens = registry.removeTokens("workspace123");


        assertEquals(removedTokens, userToToken);
        assertTrue(exists(registry, "user1", "workspace234"));
        assertFalse(exists(registry, "user1", "workspace123"));
        assertFalse(exists(registry, "user2", "workspace123"));
        assertFalse(exists(registry, "user3", "workspace123"));
    }

    private static boolean exists(MachineTokenRegistry registry, String user, String workspace) {
        try {
            registry.getOrCreateToken(user, workspace);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }
}
