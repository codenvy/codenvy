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
package com.codenvy.auth.sso.server;


import com.codenvy.api.dao.authentication.TokenGenerator;

import javax.inject.Singleton;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Generator of tokens based on SecureRandom class.
 *
 * @author Andrey Parfonov
 * @author Sergey Kabashniuk
 */
@Singleton
public class SecureRandomTokenGenerator implements TokenGenerator {

    private final Random random = new SecureRandom();
    private final char[] chars  = new char[62];

    public SecureRandomTokenGenerator() {
        int i = 0;
        for (int c = 48; c <= 57; c++) {
            chars[i++] = (char)c;
        }
        for (int c = 65; c <= 90; c++) {
            chars[i++] = (char)c;
        }
        for (int c = 97; c <= 122; c++) {
            chars[i++] = (char)c;
        }
    }
    @Override
    public String generate() {
        final char[] tokenChars = new char[255];
        for (int i = 0; i < tokenChars.length; i++) {
            tokenChars[i] = chars[random.nextInt() & (chars.length - 1)];
        }
        return new String(tokenChars);
    }
}
