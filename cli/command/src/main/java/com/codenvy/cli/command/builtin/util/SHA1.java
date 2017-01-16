/*******************************************************************************
 * Copyright (c) [2012] - [2017] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.cli.command.builtin.util;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class to get SHA-1 hexa string
 * @author Florent Benoit
 */
public final class SHA1 {

    public static String sha1(String toEncode) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to find SHA-1 message digest", e);
        }

        Charset utf8 = Charset.forName("UTF-8");

        return bytesToHexa(md.digest(toEncode.getBytes(utf8)));
    }

    public static String sha1(String prefix, String toEncode) {
        return new StringBuilder(prefix).append(sha1(toEncode)).toString();
    }


    /**
     * Helper method used to convert byte into hexa value
     *
     * @param b
     *         the given array of bytes
     * @return a string with hexadecimal values for a pretty print
     */
    protected static String bytesToHexa(final byte[] b) {
        char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7',
                           '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            buffer.append(hexDigit[(b[i] >> 4) & 0x0f]);
            buffer.append(hexDigit[b[i] & 0x0f]);
        }
        return buffer.toString();
    }

}
