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
package com.codenvy.service.token;

import org.eclipse.che.api.core.NotFoundException;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.eclipse.che.commons.lang.NameGenerator.generate;

/**
 *
 * Map-based storage of machine security tokens.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Singleton
public class MachineTokenRegistry {

    private Map<String, String> tokens = new HashMap<>();

    public void generateToken(String userId) {
        tokens.put(userId, generate("", 64));
    }

    public String getToken(String userId) throws NotFoundException {
        final String token = tokens.get(userId);
        if (token == null) {
            throw new NotFoundException(format("Token not found for user %s", userId));
        }
        return token;
    }

    public void removeToken(String userId) {
        tokens.remove(userId);
    }


}
