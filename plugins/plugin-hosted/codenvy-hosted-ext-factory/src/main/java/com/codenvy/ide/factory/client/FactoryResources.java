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
package com.codenvy.ide.factory.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

import org.eclipse.che.ide.ui.Styles;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Factory extension resources (css styles, images).
 *
 * @author Ann Shumilova
 * @author Anton Korneta
 */
public interface FactoryResources extends ClientBundle {
    interface FactoryCSS extends CssResource, Styles {
        String label();

        String createFactoryButton();

        String labelErrorPosition();
    }

    interface Style extends CssResource {
        String launchIcon();

        String configureIcon();
    }

    @Source({"Factory.css", "org/eclipse/che/ide/api/ui/style.css", "org/eclipse/che/ide/ui/Styles.css"})
    FactoryCSS factoryCSS();

    @Source("factory/export-config.svg")
    SVGResource exportConfig();

    @Source("factory/import-config.svg")
    SVGResource importConfig();

    @Source("factory/execute.svg")
    SVGResource execute();

    @Source("factory/cog-icon.svg")
    SVGResource configure();
}
