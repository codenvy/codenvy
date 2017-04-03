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
package com.codenvy.plugin.product.info.client;

import com.google.inject.Singleton;

import org.eclipse.che.ide.api.ProductInfoDataProvider;
import org.eclipse.che.ide.api.ProductInfoDataProviderImpl;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.inject.Inject;


/**
 * Implementation of {@link ProductInfoDataProvider}
 *
 * @author Alexander Andrienko
 * @author Oleksii Orel
 */
@Singleton
public class CodenvyProductInfoDataProvider extends ProductInfoDataProviderImpl {
    private final CodenvyLocalizationConstant locale;
    private final CodenvyResources            resources;

    @Inject
    public CodenvyProductInfoDataProvider(CodenvyLocalizationConstant locale, CodenvyResources resources) {
        this.locale = locale;
        this.resources = resources;
    }

    @Override
    public String getName() {
        return locale.getProductName();
    }

    @Override
    public String getSupportLink() {
        return locale.getSupportLink();
    }

    @Override
    public String getDocumentTitle() {
        return locale.codenvyTabTitle();
    }

    @Override
    public String getDocumentTitle(String workspaceName) {
        return locale.codenvyTabTitle(workspaceName);
    }

    @Override
    public SVGResource getLogo() {
        return resources.logo();
    }

    @Override
    public String getSupportTitle() {
        return locale.supportTitle();
    }
}
