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

import com.codenvy.api.AdminApiModule;
import com.codenvy.api.permission.server.PermissionChecker;
import com.codenvy.api.user.server.AdminUserService;
<<<<<<< HEAD
import com.codenvy.api.workspace.server.jpa.PermissionsJpaModule;
=======
import com.codenvy.api.workspace.server.jpa.OnPremisesJpaWorkspaceDao;
import com.codenvy.api.workspace.server.jpa.WorkerJpaModule;
>>>>>>> jpa-integration
import com.codenvy.auth.aws.ecr.AwsEcrAuthResolver;
import com.codenvy.auth.sso.client.ServerClient;
import com.codenvy.auth.sso.client.TokenHandler;
import com.codenvy.auth.sso.client.filter.ConjunctionRequestFilter;
import com.codenvy.auth.sso.client.filter.DisjunctionRequestFilter;
import com.codenvy.auth.sso.client.filter.NegationRequestFilter;
import com.codenvy.auth.sso.client.filter.PathSegmentNumberFilter;
import com.codenvy.auth.sso.client.filter.PathSegmentValueFilter;
import com.codenvy.auth.sso.client.filter.RegexpRequestFilter;
import com.codenvy.auth.sso.client.filter.RequestFilter;
import com.codenvy.auth.sso.client.filter.RequestMethodFilter;
import com.codenvy.auth.sso.client.filter.UriStartFromRequestFilter;
import com.codenvy.auth.sso.server.organization.UserCreationValidator;
import com.codenvy.auth.sso.server.organization.UserCreator;
import com.codenvy.plugin.github.factory.resolver.GithubFactoryParametersResolver;
import com.codenvy.plugin.gitlab.factory.resolver.GitlabFactoryParametersResolver;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.palominolabs.metrics.guice.InstrumentationModule;

import org.eclipse.che.account.spi.AccountDao;
import org.eclipse.che.account.spi.jpa.JpaAccountDao;
import org.eclipse.che.api.auth.AuthenticationDao;
import org.eclipse.che.api.agent.server.wsagent.WsAgentLauncher;
import org.eclipse.che.api.auth.AuthenticationService;
import org.eclipse.che.api.core.jdbc.jpa.eclipselink.EntityListenerInjectionManagerInitializer;
import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;
import org.eclipse.che.api.core.notification.WSocketEventBusServer;
import org.eclipse.che.api.core.rest.ApiInfoService;
import org.eclipse.che.api.environment.server.MachineServiceLinksInjector;
import org.eclipse.che.api.factory.server.FactoryAcceptValidator;
import org.eclipse.che.api.factory.server.FactoryCreateValidator;
import org.eclipse.che.api.factory.server.FactoryEditValidator;
import org.eclipse.che.api.factory.server.FactoryParametersResolver;
import org.eclipse.che.api.factory.server.FactoryService;
import org.eclipse.che.api.factory.server.jpa.JpaFactoryDao;
import org.eclipse.che.api.factory.server.spi.FactoryDao;
import org.eclipse.che.api.machine.server.jpa.MachineJpaModule;
import org.eclipse.che.api.machine.server.recipe.RecipeLoader;
import org.eclipse.che.api.machine.server.recipe.RecipeService;
import org.eclipse.che.api.machine.server.recipe.providers.RecipeProvider;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.template.ProjectTemplateDescriptionLoader;
import org.eclipse.che.api.project.server.template.ProjectTemplateRegistry;
import org.eclipse.che.api.project.server.template.ProjectTemplateService;
import org.eclipse.che.api.ssh.server.jpa.SshJpaModule;
import org.eclipse.che.api.user.server.PreferencesService;
import org.eclipse.che.api.user.server.ProfileService;
import org.eclipse.che.api.user.server.TokenValidator;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.user.server.jpa.UserJpaModule;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.server.WorkspaceServiceLinksInjector;
import org.eclipse.che.api.workspace.server.WorkspaceValidator;
import org.eclipse.che.api.workspace.server.event.WorkspaceMessenger;
import org.eclipse.che.api.workspace.server.jpa.JpaWorkspaceDao;
import org.eclipse.che.api.workspace.server.jpa.WorkspaceJpaModule;
import org.eclipse.che.api.workspace.server.stack.StackLoader;
import org.eclipse.che.api.workspace.server.stack.StackService;
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
        bind(ProfileService.class);
        bind(PreferencesService.class);

        //recipe service
        bind(RecipeService.class);

        install(new AdminApiModule());

        bind(AsynchronousJobPool.class).to(CheAsynchronousJobPool.class);
        bind(ServiceBindingHelper.bindingKey(AsynchronousJobService.class, "/async/{ws-id}")).to(AsynchronousJobService.class);

        bind(ETagResponseFilter.class);
        bind(EverrestDownloadFileResponseFilter.class);
        bind(WSocketEventBusServer.class);

        install(new org.eclipse.che.api.core.rest.CoreRestModule());
        install(new org.eclipse.che.api.core.util.FileCleaner.FileCleanerModule());

        install(new org.eclipse.che.api.machine.server.MachineModule());

        install(new org.eclipse.che.swagger.deploy.DocsModule());

        //oauth 2
        bind(OAuthAuthenticatorProvider.class).to(OAuthAuthenticatorProviderImpl.class);
        bind(org.eclipse.che.api.auth.oauth.OAuthTokenProvider.class).to(OAuthAuthenticatorTokenProvider.class);

        bind(FactoryAcceptValidator.class).to(org.eclipse.che.api.factory.server.impl.FactoryAcceptValidatorImpl.class);
        bind(FactoryCreateValidator.class).to(org.eclipse.che.api.factory.server.impl.FactoryCreateValidatorImpl.class);
        bind(FactoryEditValidator.class).to(org.eclipse.che.api.factory.server.impl.FactoryEditValidatorImpl.class);
        bind(FactoryService.class);

        Multibinder<FactoryParametersResolver> factoryParametersResolverMultibinder =
                Multibinder.newSetBinder(binder(), FactoryParametersResolver.class);
        factoryParametersResolverMultibinder.addBinding()
                                                  .to(GithubFactoryParametersResolver.class);
        factoryParametersResolverMultibinder.addBinding()
                                            .to(GitlabFactoryParametersResolver.class);

        Multibinder<ProjectHandler> projectHandlerMultibinder =
                Multibinder.newSetBinder(binder(), org.eclipse.che.api.project.server.handlers.ProjectHandler.class);


        install(new JpaPersistModule("main"));
        bind(JpaInitializer.class).asEagerSingleton();
        bind(EntityListenerInjectionManagerInitializer.class).asEagerSingleton();
        install(new UserJpaModule());
        install(new SshJpaModule());
        install(new WorkspaceJpaModule());
        install(new PermissionsJpaModule());
        install(new MachineJpaModule());
        bind(AccountDao.class).to(JpaAccountDao.class);
        bind(FactoryDao.class).to(JpaFactoryDao.class);
        bind(JpaWorkspaceDao.class).to(OnPremisesJpaWorkspaceDao.class);
        bind(AuthenticationDao.class).to(com.codenvy.api.dao.authentication.AuthenticationDaoImpl.class);
        bind(RecipeLoader.class);
        final Multibinder<String> recipeBinder = Multibinder.newSetBinder(binder(), String.class, Names.named("predefined.recipe.path"));
        recipeBinder.addBinding().toProvider(RecipeProvider.class);
        recipeBinder.addBinding().toInstance("predefined-recipes.json");

        bind(StackService.class);
        bind(StackLoader.class);

        bind(WorkspaceValidator.class).to(org.eclipse.che.api.workspace.server.DefaultWorkspaceValidator.class);
        bind(WorkspaceManager.class).to(com.codenvy.api.workspace.LimitsCheckingWorkspaceManager.class);
        bind(WorkspaceMessenger.class).asEagerSingleton();


        bind(com.codenvy.service.http.WorkspaceInfoCache.class);

        bind(com.codenvy.service.password.PasswordService.class);

        //authentication

        bind(TokenValidator.class).to(com.codenvy.auth.sso.server.BearerTokenValidator.class);
        bind(com.codenvy.auth.sso.oauth.SsoOAuthAuthenticationService.class);

        //machine authentication
        bind(com.codenvy.machine.authentication.server.MachineTokenPermissionsFilter.class);
        bind(com.codenvy.machine.authentication.server.MachineTokenRegistry.class);
        bind(com.codenvy.machine.authentication.server.MachineTokenService.class);
        bind(WorkspaceServiceLinksInjector.class).to(com.codenvy.machine.authentication.server.WorkspaceServiceAuthLinksInjector.class);
        bind(MachineServiceLinksInjector.class).to(com.codenvy.machine.authentication.server.MachineServiceAuthLinksInjector.class);
        install(new com.codenvy.machine.authentication.server.interceptor.InterceptorModule());
        bind(ServerClient.class).to(com.codenvy.auth.sso.client.MachineSsoServerClient.class);
        bind(com.codenvy.auth.sso.client.MachineSessionInvalidator.class);

        //SSO
        Multibinder<com.codenvy.api.dao.authentication.AuthenticationHandler> handlerBinder =
                Multibinder.newSetBinder(binder(), com.codenvy.api.dao.authentication.AuthenticationHandler.class);
        handlerBinder.addBinding().to(com.codenvy.auth.sso.server.OrgServiceAuthenticationHandler.class);

        bind(UserCreator.class).to(com.codenvy.auth.sso.server.OrgServiceUserCreator.class);

        bind(UserCreationValidator.class).to(com.codenvy.auth.sso.server.OrgServiceUserValidator.class);
        bind(PermissionChecker.class).to(com.codenvy.api.permission.server.PermissionCheckerImpl.class);
        bind(TokenHandler.class).to(com.codenvy.api.permission.server.PermissionTokenHandler.class);
        bind(TokenHandler.class).annotatedWith(Names.named("delegated.handler"))
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
                        new UriStartFromRequestFilter("/api/user/settings"),
                        new ConjunctionRequestFilter(
                                new RegexpRequestFilter("^/api/permissions$"),
                                new RequestMethodFilter("GET")
                        )
                )
        );


        bindConstant().annotatedWith(Names.named("notification.server.propagate_events")).to("vfs,workspace");

        bind(com.codenvy.service.http.WorkspaceInfoCache.WorkspaceCacheLoader.class)
                .to(com.codenvy.service.http.WorkspaceInfoCache.ManagerCacheLoader.class);

        install(new com.codenvy.workspace.interceptor.InterceptorModule());
        install(new com.codenvy.auth.sso.server.deploy.SsoServerModule());

        install(new InstrumentationModule());
        bind(org.eclipse.che.api.ssh.server.SshService.class);
        bind(org.eclipse.che.api.environment.server.MachineService.class);

        install(new ScheduleModule());

        bind(org.eclipse.che.plugin.docker.client.DockerConnector.class).to(com.codenvy.swarm.client.SwarmDockerConnector.class);
        bind(org.eclipse.che.plugin.docker.client.DockerRegistryDynamicAuthResolver.class)
                .to(AwsEcrAuthResolver.class);

        bind(String.class).annotatedWith(Names.named("machine.docker.machine_env"))
                          .toProvider(com.codenvy.machine.MaintenanceConstraintProvider.class);

        install(new org.eclipse.che.plugin.docker.machine.ext.DockerTerminalModule());
        bind(org.eclipse.che.api.agent.server.terminal.MachineTerminalLauncher.class);

        install(new org.eclipse.che.plugin.docker.machine.proxy.DockerProxyModule());

        install(new com.codenvy.api.permission.server.PermissionsModule());
        install(new com.codenvy.api.workspace.server.WorkspaceApiModule());

        install(new FactoryModuleBuilder()
                        .implement(org.eclipse.che.api.machine.server.spi.Instance.class,
                                   org.eclipse.che.plugin.docker.machine.DockerInstance.class)
                        .implement(org.eclipse.che.api.machine.server.spi.InstanceProcess.class,
                                   org.eclipse.che.plugin.docker.machine.DockerProcess.class)
                        .implement(org.eclipse.che.plugin.docker.machine.node.DockerNode.class,
                                   com.codenvy.machine.RemoteDockerNode.class)
                        .implement(org.eclipse.che.plugin.docker.machine.DockerInstanceRuntimeInfo.class,
                                   com.codenvy.machine.HostedServersInstanceRuntimeInfo.class)
                        .build(org.eclipse.che.plugin.docker.machine.DockerMachineFactory.class));

        bind(org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider.class)
                .to(com.codenvy.machine.RemoteWorkspaceFolderPathProvider.class);

        install(new org.eclipse.che.plugin.docker.machine.ext.DockerExtServerModule());

        bind(com.codenvy.machine.backup.WorkspaceFsBackupScheduler.class).asEagerSingleton();

        bind(String.class).annotatedWith(Names.named("machine.docker.che_api.endpoint"))
                          .to(Key.get(String.class, Names.named("api.endpoint")));

//        install(new com.codenvy.router.MachineRouterModule());

        bind(org.eclipse.che.api.workspace.server.event.MachineStateListener.class).asEagerSingleton();

        install(new org.eclipse.che.plugin.docker.machine.DockerMachineModule());
        Multibinder<org.eclipse.che.api.machine.server.spi.InstanceProvider> machineImageProviderMultibinder =
                Multibinder.newSetBinder(binder(), org.eclipse.che.api.machine.server.spi.InstanceProvider.class);
        machineImageProviderMultibinder.addBinding()
                                       .to(com.codenvy.machine.HostedDockerInstanceProvider.class);
        bind(WsAgentLauncher.class).to(com.codenvy.machine.launcher.WsAgentWithAuthLauncherImpl.class);

        //workspace activity service
        install(new com.codenvy.activity.server.inject.WorkspaceActivityModule());

        bind(org.eclipse.che.api.agent.server.terminal.MachineTerminalLauncher.class);

        MapBinder<String, com.codenvy.machine.MachineServerProxyTransformer> mapBinder =
                MapBinder.newMapBinder(binder(),
                                       String.class,
                                       com.codenvy.machine.MachineServerProxyTransformer.class);
        mapBinder.addBinding(org.eclipse.che.api.machine.shared.Constants.TERMINAL_REFERENCE)
                 .to(com.codenvy.machine.TerminalServerProxyTransformer.class);
        mapBinder.addBinding(org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE)
                 .to(com.codenvy.machine.WsAgentServerProxyTransformer.class);

        install(new org.eclipse.che.plugin.machine.ssh.SshMachineModule());
        bind(com.codenvy.api.factory.server.filters.FactoryPermissionsFilter.class);
    }
}
