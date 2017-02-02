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
package com.codenvy.deploy;

import com.google.inject.servlet.ServletModule;

import org.eclipse.che.inject.DynaModule;

import javax.inject.Singleton;

/**
 * @author Sergii Kabashniuk
 */
@DynaModule
public class ResourcesServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        bind(com.xemantic.tadedon.servlet.CacheDisablingFilter.class).in(Singleton.class);
        bind(com.xemantic.tadedon.servlet.CacheForcingFilter.class).in(Singleton.class);
        filterRegex("^.*\\.nocache\\..*$", "^.*/_app/.*$").through(com.xemantic.tadedon.servlet.CacheDisablingFilter.class);
        filterRegex("^.*\\.cache\\..*$").through(com.xemantic.tadedon.servlet.CacheForcingFilter.class);
    }
}
