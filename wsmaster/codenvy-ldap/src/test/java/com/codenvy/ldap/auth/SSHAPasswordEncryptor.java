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
package com.codenvy.ldap.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * SHA+salt encryption.
 */
class SSHAPasswordEncryptor {

    private static final String       SSHA_PREFIX   = "{SSHA}";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public byte[] encrypt(byte[] password) {
        byte[] salt = new byte[6];
        SECURE_RANDOM.nextBytes(salt);
        try {
            byte[] buff = new byte[password.length + salt.length];
            System.arraycopy(password, 0, buff, 0, password.length);
            System.arraycopy(salt, 0, buff, password.length, salt.length);

            byte[] res = new byte[20 + salt.length];
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.reset();
            System.arraycopy(md.digest(buff), 0, res, 0, 20);
            System.arraycopy(salt, 0, res, 20, salt.length);

            return (SSHA_PREFIX + Base64.getEncoder().encodeToString(res)).getBytes();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

}
