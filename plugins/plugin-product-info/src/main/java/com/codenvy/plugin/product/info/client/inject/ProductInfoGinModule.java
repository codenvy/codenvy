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
package com.codenvy.plugin.product.info.client.inject;

import com.codenvy.plugin.product.info.client.CodenvyProductInfoDataProvider;
import com.google.gwt.inject.client.AbstractGinModule;

import org.eclipse.che.ide.api.ProductInfoDataProvider;
import org.eclipse.che.ide.api.ProductInfoDataProviderImpl;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;

/**
 * @author Oleksii Orel
 */
@ExtensionGinModule
public class ProductInfoGinModule extends AbstractGinModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bind(ProductInfoDataProviderImpl.class).to(CodenvyProductInfoDataProvider.class);
    }
}
