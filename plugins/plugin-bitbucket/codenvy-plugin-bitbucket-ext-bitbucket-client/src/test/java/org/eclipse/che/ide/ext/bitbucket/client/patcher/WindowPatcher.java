/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
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
