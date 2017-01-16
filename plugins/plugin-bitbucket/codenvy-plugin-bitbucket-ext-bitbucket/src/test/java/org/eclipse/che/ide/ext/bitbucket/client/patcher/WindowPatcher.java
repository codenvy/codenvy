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
package org.eclipse.che.ide.ext.bitbucket.client.patcher;

import com.google.gwt.user.client.Window;
import com.googlecode.gwt.test.patchers.PatchClass;
import com.googlecode.gwt.test.patchers.PatchMethod;

/**
 * Patcher for Window.Location class.
 *
 * @author Kevin Pollet
 */
@PatchClass(Window.Location.class)
public class WindowPatcher {

    /** Patch getProtocol method. */
    @PatchMethod(override = true)
    public static String getProtocol() {
        return "";
    }

    /** Patch getHost method. */
    @PatchMethod(override = true)
    public static String getHost() {
        return "";
    }
}
