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
package com.codenvy.api.dao.util;

import com.codenvy.api.dao.exception.ItemNamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to validate entity names to fit organization service entities naming conventions.
 * <p/>
 * Currently entity names are not allowed to: <ul> <li>contain <code>','</code> character</li> <li>contain
 * <code>'='</code> characters</li> <li>be empty</li> <li>be <code>null</code></li> </ul>
 */
public class NamingValidator {

    private static final Pattern pattern = Pattern.compile("[^,=]+");

    public static void validate(String entityName) throws ItemNamingException {
        if (entityName == null) {
            throw new ItemNamingException("Entity name must not be null");
        }

        Matcher matcher = pattern.matcher(entityName);
        if (!matcher.matches()) {
            throw new ItemNamingException("Incorrect entity name: " + entityName);
        }
    }
}
