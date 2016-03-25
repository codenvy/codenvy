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
package com.codenvy.api.deploy;

import com.codenvy.activity.server.WorkspaceActivityService;
import com.codenvy.api.dao.authentication.PasswordEncryptor;
import com.codenvy.api.dao.authentication.SSHAPasswordEncryptor;
import com.codenvy.api.dao.ldap.AdminUserDaoImpl;
import com.codenvy.api.dao.ldap.UserDaoImpl;
import com.codenvy.api.dao.mongo.AccountDaoImpl;
import com.codenvy.api.dao.mongo.MachineMongoDatabaseProvider;
import com.codenvy.api.dao.mongo.OrganizationMongoDatabaseProvider;
import com.codenvy.api.dao.mongo.RecipeDaoImpl;
import com.codenvy.api.dao.mongo.WorkspaceDaoImpl;
import com.codenvy.api.dao.util.ProfileMigrator;
import com.codenvy.api.factory.FactoryMongoDatabaseProvider;
import com.codenvy.api.user.server.AdminUserService;
import com.codenvy.api.user.server.dao.AdminUserDao;
import com.codenvy.auth.sso.client.EnvironmentContextResolver;
import com.codenvy.auth.sso.client.SSOContextResolver;
import com.codenvy.auth.sso.client.filter.ConjunctionRequestFilter;
import com.codenvy.auth.sso.client.filter.DisjunctionRequestFilter;
import com.codenvy.auth.sso.client.filter.NegationRequestFilter;
import com.codenvy.auth.sso.client.filter.PathSegmentNumberFilter;
import com.codenvy.auth.sso.client.filter.PathSegmentValueFilter;
import com.codenvy.auth.sso.client.filter.RegexpRequestFilter;
import com.codenvy.auth.sso.client.filter.RequestFilter;
import com.codenvy.auth.sso.client.filter.RequestMethodFilter;
import com.codenvy.auth.sso.client.filter.UriStartFromRequestFilter;
import com.codenvy.auth.sso.server.RolesExtractor;
import com.codenvy.auth.sso.server.organization.UserCreationValidator;
import com.codenvy.auth.sso.server.organization.UserCreator;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.mongodb.client.MongoDatabase;
import com.palominolabs.metrics.guice.InstrumentationModule;

import org.eclipse.che.api.account.server.AccountService;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.auth.AuthenticationService;
import org.eclipse.che.api.core.notification.WSocketEventBusServer;
import org.eclipse.che.api.core.rest.ApiInfoService;
import org.eclipse.che.api.core.rest.permission.PermissionManager;
import org.eclipse.che.api.factory.server.FactoryAcceptValidator;
import org.eclipse.che.api.factory.server.FactoryCreateValidator;
import org.eclipse.che.api.factory.server.FactoryEditValidator;
import org.eclipse.che.api.factory.server.FactoryService;
import org.eclipse.che.api.machine.server.dao.RecipeDao;
import org.eclipse.che.api.machine.server.recipe.PermissionsChecker;
import org.eclipse.che.api.machine.server.recipe.PermissionsCheckerImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeLoader;
import org.eclipse.che.api.machine.server.recipe.RecipeService;
import org.eclipse.che.api.machine.server.recipe.providers.RecipeProvider;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.template.ProjectTemplateDescriptionLoader;
import org.eclipse.che.api.project.server.template.ProjectTemplateRegistry;
import org.eclipse.che.api.project.server.template.ProjectTemplateService;
import org.eclipse.che.api.ssh.server.spi.SshDao;
import org.eclipse.che.api.user.server.TokenValidator;
import org.eclipse.che.api.user.server.UserProfileService;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.eclipse.che.api.workspace.server.WorkspaceConfigValidator;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.server.event.WorkspaceMessenger;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.schedule.executor.ScheduleModule;
import org.eclipse.che.everrest.CheAsynchronousJobPool;
import org.eclipse.che.everrest.ETagResponseFilter;
import org.eclipse.che.everrest.EverrestDownloadFileResponseFilter;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.security.oauth.OAuthAuthenticatorProvider;
import org.eclipse.che.security.oauth.OAuthAuthenticatorProviderImpl;
import org.eclipse.che.security.oauth.OAuthAuthenticatorTokenProvider;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.guice.ServiceBindingHelper;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.inject.Matchers.names;

/**
 * Guice container configuration file. Replaces old REST application composers and servlet context listeners.
 *
 * @author Max Shaposhnik
 */
@DynaModule
public class OnPremisesIdeApiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ApiInfoService.class);
        bind(ProjectTemplateRegistry.class);
        bind(ProjectTemplateDescriptionLoader.class).asEagerSingleton();
        bind(ProjectTemplateService.class);
        bind(AuthenticationService.class);
        bind(WorkspaceService.class);
        bind(UserService.class);
        bind(AdminUserService.class);
        bind(UserProfileService.class);
        bind(AccountService.class);

        //recipe service
        bind(RecipeService.class);
        bind(PermissionsChecker.class).to(PermissionsCheckerImpl.class);

        bind(AsynchronousJobPool.class).to(CheAsynchronousJobPool.class);
        bind(ServiceBindingHelper.bindingKey(AsynchronousJobService.class, "/async/{ws-id}")).to(AsynchronousJobService.class);

        bind(ETagResponseFilter.class);
        bind(EverrestDownloadFileResponseFilter.class);
        bind(WSocketEventBusServer.class);

        install(new org.eclipse.che.api.core.rest.CoreRestModule());
        install(new org.eclipse.che.api.vfs.VirtualFileSystemModule());
        /*
        install(new org.eclipse.che.api.factory.FactoryModule());
        */
        install(new org.eclipse.che.api.machine.server.MachineModule());

        install(new org.eclipse.che.swagger.deploy.DocsModule());

        //oauth 2
        bind(OAuthAuthenticatorProvider.class).to(OAuthAuthenticatorProviderImpl.class);
        bind(org.eclipse.che.api.auth.oauth.OAuthTokenProvider.class).to(OAuthAuthenticatorTokenProvider.class);

        //factory
        bind(com.mongodb.DB.class).annotatedWith(Names.named("mongo.db.organization"))
                                  .toProvider(com.codenvy.api.dao.mongo.OrganizationMongoDBProvider.class);

        bind(MongoDatabase.class).annotatedWith(Names.named("mongo.db.organization"))
                                 .toProvider(OrganizationMongoDatabaseProvider.class);

        bind(MongoDatabase.class).annotatedWith(Names.named("mongo.db.factory"))
                                 .toProvider(FactoryMongoDatabaseProvider.class);



        bind(org.eclipse.che.api.factory.server.FactoryStore.class).to(com.codenvy.api.dao.mongo.MongoDBFactoryStore.class);
        bind(FactoryAcceptValidator.class).to(org.eclipse.che.api.factory.server.impl.FactoryAcceptValidatorImpl.class);
        bind(FactoryCreateValidator.class).to(org.eclipse.che.api.factory.server.impl.FactoryCreateValidatorImpl.class);
        bind(FactoryEditValidator.class).to(org.eclipse.che.api.factory.server.impl.FactoryEditValidatorImpl.class);
        bind(FactoryService.class);

        Multibinder<ProjectHandler> projectHandlerMultibinder =
                Multibinder.newSetBinder(binder(), org.eclipse.che.api.project.server.handlers.ProjectHandler.class);


        //user-workspace-account
        bind(PasswordEncryptor.class).toInstance(new SSHAPasswordEncryptor());

        bind(WorkspaceDao.class).to(WorkspaceDaoImpl.class);
        bind(UserDao.class).to(UserDaoImpl.class);
        bind(AdminUserDao.class).to(AdminUserDaoImpl.class);
        bind(UserProfileDao.class).to(com.codenvy.api.dao.ldap.UserProfileDaoImpl.class);
        bind(PreferenceDao.class).to(com.codenvy.api.dao.mongo.PreferenceDaoImpl.class);
        bind(SshDao.class).to(com.codenvy.api.dao.mongo.ssh.SshDaoImpl.class);
        bind(AccountDao.class).to(AccountDaoImpl.class);
        bind(org.eclipse.che.api.auth.AuthenticationDao.class).to(com.codenvy.api.dao.authentication.AuthenticationDaoImpl.class);
        bind(RecipeDao.class).to(RecipeDaoImpl.class);
        bind(RecipeLoader.class);
        Multibinder<String> recipeBinder = Multibinder.newSetBinder(binder(), String.class, Names.named("predefined.recipe.path"));
        recipeBinder.addBinding().toProvider(RecipeProvider.class);
        recipeBinder.addBinding().toInstance("predefined-recipes.json");

        bind(org.eclipse.che.api.workspace.server.stack.StackService.class);
        bind(org.eclipse.che.api.workspace.server.spi.StackDao.class).to(com.codenvy.api.dao.mongo.StackDaoImpl.class);
        bind(org.eclipse.che.api.workspace.server.stack.StackLoader.class);

        bind(WorkspaceConfigValidator.class).to(com.codenvy.api.workspace.LimitsCheckingWorkspaceConfigValidator.class);
        bind(WorkspaceManager.class).to(com.codenvy.api.workspace.LimitsCheckingWorkspaceManager.class);
        bind(WorkspaceMessenger.class).asEagerSingleton();


        bind(com.codenvy.service.http.WorkspaceInfoCache.class);

        bind(com.codenvy.service.password.PasswordService.class);

        //authentication

        bind(TokenValidator.class).to(com.codenvy.auth.sso.server.BearerTokenValidator.class);
        bind(com.codenvy.auth.sso.oauth.SsoOAuthAuthenticationService.class);

        //SSO
        Multibinder<com.codenvy.api.dao.authentication.AuthenticationHandler> handlerBinder =
                Multibinder.newSetBinder(binder(), com.codenvy.api.dao.authentication.AuthenticationHandler.class);
        handlerBinder.addBinding().to(com.codenvy.auth.sso.server.OrgServiceAuthenticationHandler.class);


        Multibinder<RolesExtractor> rolesExtractorBinder = Multibinder.newSetBinder(binder(), RolesExtractor.class);

        rolesExtractorBinder.addBinding().to(com.codenvy.auth.sso.server.OrgServiceRolesExtractor.class);

        bind(UserCreator.class).to(com.codenvy.auth.sso.server.OrgServiceUserCreator.class);

        bind(UserCreationValidator.class).to(com.codenvy.auth.sso.server.OrgServiceUserValidator.class);


        bind(SSOContextResolver.class).to(EnvironmentContextResolver.class);

        bind(com.codenvy.auth.sso.client.TokenHandler.class)
                .to(com.codenvy.auth.sso.client.NoUserInteractionTokenHandler.class);

        bindConstant().annotatedWith(Names.named("auth.jaas.realm")).to("default_realm");
        bindConstant().annotatedWith(Names.named("auth.handler.default")).to("org");
        bindConstant().annotatedWith(Names.named("auth.sso.access_cookie_path")).to("/api/internal/sso/server");
        bindConstant().annotatedWith(Names.named("auth.sso.access_ticket_lifetime_seconds")).to(259200);
        bindConstant().annotatedWith(Names.named("auth.sso.bearer_ticket_lifetime_seconds")).to(3600);
        bindConstant().annotatedWith(Names.named("auth.sso.create_workspace_page_url")).to("/site/auth/create");
        bindConstant().annotatedWith(Names.named("auth.sso.login_page_url")).to("/site/login");
        bindConstant().annotatedWith(Names.named("auth.oauth.access_denied_error_page")).to("/site/login");
        bindConstant().annotatedWith(Names.named("error.page.workspace_not_found_redirect_url")).to("/site/error/error-tenant-name");
        bindConstant().annotatedWith(Names.named("auth.sso.cookies_disabled_error_page_url"))
                      .to("/site/error/error-cookies-disabled");
        bindConstant().annotatedWith(Names.named("auth.no.account.found.page")).to("/site/error/no-account-found");

        bind(RequestFilter.class).toInstance(
                new DisjunctionRequestFilter(
                        new ConjunctionRequestFilter(
                                new UriStartFromRequestFilter("/api/factory"),
                                new RequestMethodFilter("GET"),
                                new DisjunctionRequestFilter(
                                        new UriStartFromRequestFilter("/api/factory/nonencoded"),
                                        new PathSegmentValueFilter(4, "image"),
                                        new PathSegmentValueFilter(4, "snippet"),
                                        new ConjunctionRequestFilter(
                                                //api/factory/{}
                                                new PathSegmentNumberFilter(3),
                                                new NegationRequestFilter(new UriStartFromRequestFilter("/api/factory/find"))
                                        ))
                        ),
                        new UriStartFromRequestFilter("/api/analytics/public-metric"),
                        new UriStartFromRequestFilter("/api/docs"),
                        new RegexpRequestFilter("^/api/builder/(\\w+)/download/(.+)$"),
                        new ConjunctionRequestFilter(
                                new UriStartFromRequestFilter("/api/oauth/authenticate"),
                                r -> isNullOrEmpty(r.getParameter("userId"))
                        ),
                        new UriStartFromRequestFilter("/api/user/settings")
                )
                                            );


        bindConstant().annotatedWith(Names.named("notification.server.propagate_events")).to("vfs,workspace");

        bind(com.codenvy.service.http.WorkspaceInfoCache.WorkspaceCacheLoader.class)
                .to(com.codenvy.service.http.WorkspaceInfoCache.ManagerCacheLoader.class);

        bind(ProfileMigrator.class).asEagerSingleton();

        install(new com.codenvy.workspace.interceptor.InterceptorModule());
        install(new com.codenvy.auth.sso.server.deploy.SsoServerInterceptorModule());
        install(new com.codenvy.auth.sso.server.deploy.SsoServerModule());

        install(new InstrumentationModule());
        bind(org.eclipse.che.api.ssh.server.SshService.class);
        bind(org.eclipse.che.api.machine.server.MachineService.class);
        bind(org.eclipse.che.api.machine.server.dao.SnapshotDao.class).to(com.codenvy.api.dao.mongo.SnapshotDaoImpl.class);
        bind(com.mongodb.DB.class).annotatedWith(Names.named("mongo.db.machine"))
                                  .toProvider(com.codenvy.api.dao.mongo.MachineMongoDBProvider.class);

        bind(MongoDatabase.class).annotatedWith(Names.named("mongo.db.machine"))
                                 .toProvider(MachineMongoDatabaseProvider.class);

        install(new ScheduleModule());

        bind(org.eclipse.che.plugin.docker.client.DockerConnector.class).to(com.codenvy.swarm.client.SwarmDockerConnector.class);

        install(new org.eclipse.che.plugin.docker.machine.ext.DockerTerminalModule());

        install(new FactoryModuleBuilder()
                        .implement(org.eclipse.che.api.machine.server.spi.Instance.class,
                                   org.eclipse.che.plugin.docker.machine.DockerInstance.class)
                        .implement(org.eclipse.che.api.machine.server.spi.InstanceProcess.class,
                                   org.eclipse.che.plugin.docker.machine.DockerProcess.class)
                        .implement(org.eclipse.che.plugin.docker.machine.node.DockerNode.class,
                                   com.codenvy.machine.RemoteDockerNode.class)
                        .implement(org.eclipse.che.plugin.docker.machine.DockerInstanceRuntimeInfo.class,
                                   com.codenvy.machine.HttpsSupportInstanceRuntimeInfo.class)
                        .build(org.eclipse.che.plugin.docker.machine.DockerMachineFactory.class));

        bind(org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider.class)
                .to(com.codenvy.machine.RemoteWorkspaceFolderPathProvider.class);

        install(new org.eclipse.che.plugin.docker.machine.ext.DockerExtServerModule());

        bind(com.codenvy.machine.backup.WorkspaceFsBackupScheduler.class).asEagerSingleton();

        bind(String.class).annotatedWith(Names.named("machine.docker.che_api.endpoint"))
                          .to(Key.get(String.class, Names.named("api.endpoint")));

//        install(new com.codenvy.router.MachineRouterModule());

        // TODO rebind to WorkspacePermissionManager after account is established
        bind(PermissionManager.class).annotatedWith(Names.named("service.workspace.permission_manager"))
                                     .to(DummyPermissionManager.class);

        bind(org.eclipse.che.api.workspace.server.event.MachineStateListener.class).asEagerSingleton();

        bind(com.codenvy.api.account.DefaultAccountCreator.class);

        install(new org.eclipse.che.plugin.docker.machine.DockerMachineModule());

        bind(org.eclipse.che.api.machine.server.WsAgentLauncher.class).to(org.eclipse.che.api.machine.server.WsAgentLauncherImpl.class);

        //workspace activity service
        install(new com.codenvy.activity.server.inject.WorkspaceActivityModule());
    }
}
