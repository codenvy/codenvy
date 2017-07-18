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
package com.codenvy.plugin.product.info.client;

import com.google.gwt.resources.client.ClientBundle;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Hosted extension resources.
 *
 * @author Oleksii Orel
 */
public interface CodenvyResources extends ClientBundle {
    @Source("logo/codenvy-logo.svg")
    SVGResource logo();

    @Source("logo/codenvy-watermark-logo.svg")
    SVGResource waterMarkerLogo();
}
