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
package com.codenvy.ide.factory.server.inject;

import com.codenvy.ide.factory.server.FactoryProjectType;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.core.model.project.type.ProjectType;
import org.eclipse.che.inject.DynaModule;

/**
 * Factory project type binding
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@DynaModule
public class FactoryProjectTypeModule extends AbstractModule {
    @Override
    protected void configure() {
        final Multibinder<ProjectType> projectTypeMultibinder = Multibinder.newSetBinder(binder(), ProjectType.class);
        projectTypeMultibinder.addBinding().to(FactoryProjectType.class);
    }
}
