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
import java.util.List;

/**
 * @author Dmytro Nochevnov
 */
public class PropertiesNotFoundException extends FileNotFoundException {
    private List<String> properties;

    public PropertiesNotFoundException(List<String> properties) {
        super("Properties not found");
        this.properties = properties;
    }

    public List<String> getProperties() {
        return this.properties;
    }
}
