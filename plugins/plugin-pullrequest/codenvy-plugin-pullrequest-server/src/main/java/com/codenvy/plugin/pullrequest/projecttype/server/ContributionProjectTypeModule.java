/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.plugin.pullrequest.projecttype.server;

import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.inject.DynaModule;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * The contribution project type module bindings.
 *
 * @author Kevin Pollet
 */
@DynaModule
public class ContributionProjectTypeModule extends AbstractModule {
    @Override
    protected void configure() {
        final Multibinder<ProjectTypeDef> projectTypeMultibinder = Multibinder.newSetBinder(binder(), ProjectTypeDef.class);
        projectTypeMultibinder.addBinding().to(ContributionProjectType.class);
    }
}
