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
package org.eclipse.che.ide.ext.microsoft.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.ext.microsoft.client.MicrosoftServiceClient;
import org.eclipse.che.ide.ext.microsoft.client.MicrosoftServiceClientImpl;

/**
 * @author Mihail Kuznyetsov
 */
@ExtensionGinModule
public class MicrosoftGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(MicrosoftServiceClient.class).to(MicrosoftServiceClientImpl.class).in(Singleton.class);
    }
}
