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
package com.codenvy.ide.hosted.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Hosted extension resources (css styles, images).
 *
 * @author Ann Shumilova
 */
public interface HostedResources extends ClientBundle {
    interface HostedCSS extends CssResource {
        String bottomMenuTooltip();

        String bottomMenuTooltipBody();

        String bottomMenuTooltipHeader();

        String temporary();

        String temporaryLabel();

        String blueButton();
    }

    @Source({"Hosted.css", "org/eclipse/che/ide/api/ui/style.css"})
    HostedCSS hostedCSS();

    @Source("temporary/temporary.svg")
    SVGResource temporaryButton();
}
