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
package com.codenvy.im.managers;

import java.io.FileNotFoundException;

/**
 * @author Dmytro Nochevnov
 */
public class PropertyNotFoundException extends FileNotFoundException {
    public PropertyNotFoundException(String message) {
        super(message);
    }

    public static PropertyNotFoundException from(String key) {
        return new PropertyNotFoundException(String.format("Property '%s' not found", key));
    }
}
