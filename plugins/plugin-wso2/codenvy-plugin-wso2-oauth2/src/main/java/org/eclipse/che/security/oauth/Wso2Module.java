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
package org.eclipse.che.security.oauth;

import org.eclipse.che.inject.DynaModule;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * Setup WSO2OAuthAuthenticator  in guice container.
 *
 * @author Sergii Kabashniuk
 */
@DynaModule
public class Wso2Module extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<OAuthAuthenticator> oAuthAuthenticators = Multibinder.newSetBinder(binder(), OAuthAuthenticator.class);
        oAuthAuthenticators.addBinding().to(WSO2OAuthAuthenticator.class);

    }
}