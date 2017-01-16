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
package com.codenvy.im.service;

import com.codenvy.api.permission.server.PermissionChecker;
import com.codenvy.auth.sso.client.TokenHandler;
import com.codenvy.im.artifacts.Artifact;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.artifacts.InstallManagerArtifact;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.swagger.deploy.DocsModule;

/** @author Dmytro Nochevnov */
@DynaModule
public class InstallationManagerServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ApiExceptionMapper.class);
        bind(InstallationManagerService.class);
        Multibinder.newSetBinder(this.binder(), Artifact.class).addBinding().to(InstallManagerArtifact.class);
        Multibinder.newSetBinder(this.binder(), Artifact.class).addBinding().to(CDECArtifact.class);

        install(new DocsModule());

        bind(com.codenvy.auth.sso.client.WebAppClientUrlExtractor.class);
        bind(com.codenvy.auth.sso.client.token.ChainedTokenExtractor.class);

        bind(PermissionChecker.class).to(com.codenvy.api.permission.server.HttpPermissionCheckerImpl.class);

        bind(TokenHandler.class).to(com.codenvy.api.permission.server.PermissionTokenHandler.class);
        bind(TokenHandler.class).annotatedWith(Names.named("delegated.handler"))
                                .to(com.codenvy.auth.sso.client.RecoverableTokenHandler.class);

        bind(InstallationManagerPermissionsFilter.class);
    }
}
