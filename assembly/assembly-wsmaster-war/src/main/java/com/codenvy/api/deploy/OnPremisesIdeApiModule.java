/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.api.deploy;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.eclipse.che.inject.Matchers.names;

import com.codenvy.api.AdminApiModule;
import com.codenvy.api.audit.server.AuditService;
import com.codenvy.api.audit.server.AuditServicePermissionsFilter;
import com.codenvy.api.dao.authentication.AuthenticationDaoInterceptor;
import com.codenvy.api.dao.authentication.PassportValidator;
import com.codenvy.api.user.server.AdminUserService;
import com.codenvy.api.workspace.SystemRamCheckingWorkspaceManager;
import com.codenvy.auth.aws.ecr.AwsEcrAuthResolver;
import com.codenvy.auth.sso.client.OnPremisesMachineSessionInvalidator;
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
import com.codenvy.ldap.LdapModule;
import com.codenvy.ldap.auth.LdapAuthenticationHandler;
import com.codenvy.machine.agent.WorkspaceInfrastructureModule;
import com.codenvy.machine.backup.DockerEnvironmentBackupManager;
import com.codenvy.machine.backup.EnvironmentBackupManager;
import com.codenvy.plugin.gitlab.factory.resolver.GitlabFactoryParametersResolver;
import com.codenvy.service.bitbucket.BitbucketConfigurationService;
import com.codenvy.service.system.DockerBasedSystemRamInfoProvider;
import com.codenvy.service.system.HostedSystemService;
import com.codenvy.service.system.SystemRamInfoProvider;
import com.codenvy.service.system.SystemRamLimitMessageSender;
import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplateProcessorImpl;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.palominolabs.metrics.guice.InstrumentationModule;
import java.util.Map;
import javax.sql.DataSource;
import org.eclipse.che.account.spi.AccountDao;
import org.eclipse.che.account.spi.jpa.JpaAccountDao;
import org.eclipse.che.api.auth.AuthenticationDao;
import org.eclipse.che.api.auth.AuthenticationService;
import org.eclipse.che.api.core.jsonrpc.impl.JsonRpcModule;
import org.eclipse.che.api.core.notification.WSocketEventBusServer;
import org.eclipse.che.api.core.rest.ApiInfoService;
import org.eclipse.che.api.core.rest.CheJsonProvider;
import org.eclipse.che.api.core.rest.MessageBodyAdapter;
import org.eclipse.che.api.core.rest.MessageBodyAdapterInterceptor;
import org.eclipse.che.api.core.websocket.impl.WebSocketModule;
import org.eclipse.che.api.environment.server.MachineInstanceProvider;
import org.eclipse.che.api.environment.server.MachineLinksInjector;
import org.eclipse.che.api.factory.server.FactoryAcceptValidator;
import org.eclipse.che.api.factory.server.FactoryCreateValidator;
import org.eclipse.che.api.factory.server.FactoryEditValidator;
import org.eclipse.che.api.factory.server.FactoryMessageBodyAdapter;
import org.eclipse.che.api.factory.server.FactoryParametersResolver;
import org.eclipse.che.api.factory.server.FactoryService;
import org.eclipse.che.api.factory.server.jpa.FactoryJpaModule;
import org.eclipse.che.api.factory.server.jpa.JpaFactoryDao;
import org.eclipse.che.api.factory.server.spi.FactoryDao;
import org.eclipse.che.api.machine.server.jpa.JpaSnapshotDao;
import org.eclipse.che.api.machine.server.recipe.RecipeLoader;
import org.eclipse.che.api.machine.server.recipe.RecipeService;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
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
import org.eclipse.che.api.workspace.server.WorkspaceConfigMessageBodyAdapter;
import org.eclipse.che.api.workspace.server.WorkspaceMessageBodyAdapter;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.server.WorkspaceServiceLinksInjector;
import org.eclipse.che.api.workspace.server.WorkspaceValidator;
import org.eclipse.che.api.workspace.server.event.WorkspaceMessenger;
import org.eclipse.che.api.workspace.server.stack.StackLoader;
import org.eclipse.che.api.workspace.server.stack.StackMessageBodyAdapter;
import org.eclipse.che.api.workspace.server.stack.StackService;
import org.eclipse.che.commons.schedule.executor.ScheduleModule;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.JndiDataSourceProvider;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.PlaceholderReplacerProvider;
import org.eclipse.che.everrest.CheAsynchronousJobPool;
import org.eclipse.che.everrest.ETagResponseFilter;
import org.eclipse.che.everrest.EverrestDownloadFileResponseFilter;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.mail.template.TemplateProcessor;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;
import org.eclipse.che.multiuser.api.permission.server.PermissionCheckerImpl;
import org.eclipse.che.multiuser.api.permission.server.jpa.SystemPermissionsJpaModule;
import org.eclipse.che.multiuser.machine.authentication.server.MachineAuthLinksInjector;
import org.eclipse.che.multiuser.organization.api.OrganizationApiModule;
import org.eclipse.che.multiuser.organization.api.OrganizationJpaModule;
import org.eclipse.che.multiuser.permission.machine.jpa.MultiuserMachineJpaModule;
import org.eclipse.che.multiuser.permission.system.SystemServicePermissionsFilter;
import org.eclipse.che.multiuser.permission.workspace.server.jpa.MultiuserWorkspaceJpaModule;
import org.eclipse.che.multiuser.resource.api.ResourceModule;
import org.eclipse.che.multiuser.resource.api.workspace.LimitsCheckingWorkspaceManager;
import org.eclipse.che.plugin.github.factory.resolver.GithubFactoryParametersResolver;
import org.eclipse.che.security.oauth.OAuthAuthenticatorProvider;
import org.eclipse.che.security.oauth.OAuthAuthenticatorProviderImpl;
import org.eclipse.che.security.oauth.OAuthAuthenticatorTokenProvider;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.guice.ServiceBindingHelper;
import org.flywaydb.core.internal.util.PlaceholderReplacer;

/**
 * Guice container configuration file. Replaces old REST application composers and servlet context
 * listeners.
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
    bind(PreferencesService.class);

    install(new AdminApiModule());

    bind(AsynchronousJobPool.class).to(CheAsynchronousJobPool.class);
    bind(ServiceBindingHelper.bindingKey(AsynchronousJobService.class, "/async/{ws-id}"))
        .to(AsynchronousJobService.class);

    bind(ETagResponseFilter.class);
    bind(EverrestDownloadFileResponseFilter.class);
    bind(WSocketEventBusServer.class);

    bind(WsMasterAnalyticsAddresser.class);

    install(new org.eclipse.che.api.core.rest.CoreRestModule());
    install(new org.eclipse.che.api.core.util.FileCleaner.FileCleanerModule());

    install(new org.eclipse.che.api.machine.server.MachineModule());
    install(new org.eclipse.che.plugin.docker.compose.ComposeModule());

    install(new org.eclipse.che.swagger.deploy.DocsModule());

    install(new WebSocketModule());
    install(new JsonRpcModule());

    install(new com.codenvy.plugin.webhooks.bitbucketserver.inject.BitbucketServerWebhookModule());

    //oauth
    bind(OAuthAuthenticatorProvider.class).to(OAuthAuthenticatorProviderImpl.class);
    bind(org.eclipse.che.security.oauth.shared.OAuthTokenProvider.class)
        .to(OAuthAuthenticatorTokenProvider.class);
    bind(org.eclipse.che.security.oauth1.OAuthAuthenticationService.class);

    install(new org.eclipse.che.security.oauth.BitbucketModule());
    install(new org.eclipse.che.security.oauth1.BitbucketModule());

    bind(BitbucketConfigurationService.class);

    bind(FactoryAcceptValidator.class)
        .to(org.eclipse.che.api.factory.server.impl.FactoryAcceptValidatorImpl.class);
    bind(FactoryCreateValidator.class)
        .to(org.eclipse.che.api.factory.server.impl.FactoryCreateValidatorImpl.class);
    bind(FactoryEditValidator.class)
        .to(org.eclipse.che.api.factory.server.impl.FactoryEditValidatorImpl.class);
    bind(FactoryService.class);

    Multibinder<FactoryParametersResolver> factoryParametersResolverMultibinder =
        Multibinder.newSetBinder(binder(), FactoryParametersResolver.class);
    factoryParametersResolverMultibinder.addBinding().to(GithubFactoryParametersResolver.class);
    factoryParametersResolverMultibinder.addBinding().to(GitlabFactoryParametersResolver.class);

    Multibinder<ProjectHandler> projectHandlerMultibinder =
        Multibinder.newSetBinder(
            binder(), org.eclipse.che.api.project.server.handlers.ProjectHandler.class);

    install(new JpaPersistModule("main"));
    bind(SchemaInitializer.class).to(FlywaySchemaInitializer.class);
    bind(DBInitializer.class).asEagerSingleton();
    bind(DataSource.class).toProvider(JndiDataSourceProvider.class);
    bind(PlaceholderReplacer.class).toProvider(PlaceholderReplacerProvider.class);
    install(new UserJpaModule());
    install(new SshJpaModule());
    install(new MultiuserWorkspaceJpaModule());
    install(new MultiuserMachineJpaModule());
    install(new FactoryJpaModule());
    bind(AccountDao.class).to(JpaAccountDao.class);
    install(new OrganizationApiModule());
    install(new OrganizationJpaModule());
    install(new com.codenvy.api.invite.InviteApiModule());
    install(new com.codenvy.spi.invite.jpa.InviteJpaModule());
    install(new ResourceModule());
    bind(FactoryDao.class).to(JpaFactoryDao.class);
    bind(SnapshotDao.class).to(JpaSnapshotDao.class);
    // Auth
    bind(PassportValidator.class);
    bind(AuthenticationDao.class)
        .to(com.codenvy.api.dao.authentication.AuthenticationDaoImpl.class);
    final AuthenticationDaoInterceptor authInterceptor = new AuthenticationDaoInterceptor();
    requestInjection(authInterceptor);
    bindInterceptor(subclassesOf(AuthenticationDao.class), names("login"), authInterceptor);

    bind(RecipeService.class);
    bind(org.eclipse.che.api.machine.server.recipe.RecipeLoader.class);
    Multibinder.newSetBinder(
            binder(), String.class, Names.named(RecipeLoader.CHE_PREDEFINED_RECIPES))
        .addBinding()
        .toInstance("predefined-recipes.json");

    bind(StackService.class);
    //    bind(org.eclipse.che.api.workspace.server.stack.StackLoader.class);
    MapBinder.newMapBinder(
            binder(), String.class, String.class, Names.named(StackLoader.CHE_PREDEFINED_STACKS))
        .addBinding("stacks.json")
        .toInstance("stacks-images");

    bind(WorkspaceValidator.class)
        .to(org.eclipse.che.api.workspace.server.DefaultWorkspaceValidator.class);
    bind(LimitsCheckingWorkspaceManager.class).to(SystemRamCheckingWorkspaceManager.class);
    bind(org.eclipse.che.api.workspace.server.TemporaryWorkspaceRemover.class);
    bind(WorkspaceMessenger.class).asEagerSingleton();

    //Permission filters
    bind(org.eclipse.che.multiuser.permission.user.UserProfileServicePermissionsFilter.class);
    bind(org.eclipse.che.multiuser.permission.user.UserServicePermissionsFilter.class);
    bind(org.eclipse.che.plugin.activity.ActivityPermissionsFilter.class);
    bind(
        org.eclipse.che.multiuser.permission.resource.filters.ResourceUsageServicePermissionsFilter
            .class);
    bind(
        org.eclipse.che.multiuser.permission.resource.filters
            .FreeResourcesLimitServicePermissionsFilter.class);

    bind(com.codenvy.service.password.PasswordService.class);

    bind(SystemRamLimitMessageSender.class);

    bind(HostedSystemService.class);
    bind(SystemServicePermissionsFilter.class);
    bind(org.eclipse.che.api.system.server.SystemEventsWebsocketBroadcaster.class)
        .asEagerSingleton();

    bind(SystemRamInfoProvider.class).to(DockerBasedSystemRamInfoProvider.class);

    bind(AuditService.class);
    bind(AuditServicePermissionsFilter.class);

    //authentication

    bind(TokenValidator.class).to(com.codenvy.auth.sso.server.BearerTokenValidator.class);
    bind(com.codenvy.auth.sso.oauth.SsoOAuthAuthenticationService.class);

    //machine authentication
    bind(
        org.eclipse.che.multiuser.machine.authentication.server.MachineTokenPermissionsFilter
            .class);
    bind(org.eclipse.che.multiuser.machine.authentication.server.MachineTokenRegistry.class);
    bind(org.eclipse.che.multiuser.machine.authentication.server.MachineTokenService.class);
    bind(WorkspaceServiceLinksInjector.class)
        .to(
            org.eclipse.che.multiuser.machine.authentication.server
                .WorkspaceServiceAuthLinksInjector.class);
    bind(MachineLinksInjector.class).to(MachineAuthLinksInjector.class);
    install(
        new org.eclipse.che.multiuser.machine.authentication.server.interceptor
            .InterceptorModule());
    bind(ServerClient.class).to(com.codenvy.auth.sso.client.MachineSsoServerClient.class);
    bind(OnPremisesMachineSessionInvalidator.class);

    //SSO
    Multibinder<com.codenvy.api.dao.authentication.AuthenticationHandler> handlerBinder =
        Multibinder.newSetBinder(
            binder(), com.codenvy.api.dao.authentication.AuthenticationHandler.class);
    handlerBinder
        .addBinding()
        .to(com.codenvy.auth.sso.server.OrgServiceAuthenticationHandler.class);

    bind(UserCreator.class).to(com.codenvy.auth.sso.server.OrgServiceUserCreator.class);

    bind(UserCreationValidator.class).to(com.codenvy.auth.sso.server.OrgServiceUserValidator.class);
    bind(PermissionChecker.class).to(PermissionCheckerImpl.class);
    bind(TokenHandler.class).to(com.codenvy.api.permission.server.PermissionTokenHandler.class);
    bind(TokenHandler.class)
        .annotatedWith(Names.named("delegated.handler"))
        .to(com.codenvy.auth.sso.client.NoUserInteractionTokenHandler.class);

    bindConstant().annotatedWith(Names.named("auth.jaas.realm")).to("default_realm");
    bindConstant()
        .annotatedWith(Names.named("auth.sso.access_cookie_path"))
        .to("/api/internal/sso/server");
    bindConstant()
        .annotatedWith(Names.named("auth.sso.create_workspace_page_url"))
        .to("/site/auth/create");
    bindConstant().annotatedWith(Names.named("auth.sso.login_page_url")).to("/site/login");
    bindConstant()
        .annotatedWith(Names.named("che.auth.access_denied_error_page"))
        .to("/site/login");
    bindConstant()
        .annotatedWith(Names.named("error.page.workspace_not_found_redirect_url"))
        .to("/site/error/error-tenant-name");
    bindConstant()
        .annotatedWith(Names.named("auth.sso.cookies_disabled_error_page_url"))
        .to("/site/error/error-cookies-disabled");
    bindConstant()
        .annotatedWith(Names.named("auth.no.account.found.page"))
        .to("/site/error/no-account-found");

    bind(RequestFilter.class)
        .toInstance(
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
                            new NegationRequestFilter(
                                new UriStartFromRequestFilter("/api/factory/find"))))),
                new UriStartFromRequestFilter("/api/analytics/public-metric"),
                new UriStartFromRequestFilter("/api/docs"),
                new RegexpRequestFilter("^/api/builder/(\\w+)/download/(.+)$"),
                new ConjunctionRequestFilter(
                    new UriStartFromRequestFilter("/api/oauth/authenticate"),
                    r -> isNullOrEmpty(r.getParameter("userId"))),
                new UriStartFromRequestFilter("/api/user/settings"),
                new ConjunctionRequestFilter(
                    new RegexpRequestFilter("^/api/permissions$"),
                    new RequestMethodFilter("GET"))));

    bindConstant()
        .annotatedWith(Names.named("notification.server.propagate_events"))
        .to("vfs,workspace");

    install(new com.codenvy.workspace.interceptor.InterceptorModule());
    install(new com.codenvy.auth.sso.server.deploy.SsoServerModule());

    install(new InstrumentationModule());
    bind(org.eclipse.che.api.ssh.server.SshService.class);

    install(new ScheduleModule());

    bind(org.eclipse.che.plugin.docker.client.DockerConnector.class)
        .to(com.codenvy.swarm.client.SwarmDockerConnector.class);
    MapBinder<String, org.eclipse.che.plugin.docker.client.DockerConnector> dockerConnectors =
        MapBinder.newMapBinder(
            binder(), String.class, org.eclipse.che.plugin.docker.client.DockerConnector.class);
    dockerConnectors.addBinding("swarm").to(com.codenvy.swarm.client.SwarmDockerConnector.class);
    bindConstant().annotatedWith(Names.named("che.docker.connector")).to("swarm");
    bind(org.eclipse.che.plugin.docker.client.DockerRegistryDynamicAuthResolver.class)
        .to(AwsEcrAuthResolver.class);

    Multibinder<String> allMachineVolumes =
        Multibinder.newSetBinder(
            binder(), String.class, Names.named("machine.docker.machine_volumes"));
    allMachineVolumes
        .addBinding()
        .toProvider(org.eclipse.che.plugin.docker.machine.ext.provider.ExtraVolumeProvider.class);

    Multibinder<String> allMachinesEnvVars =
        Multibinder.newSetBinder(binder(), String.class, Names.named("machine.docker.machine_env"))
            .permitDuplicates();
    allMachinesEnvVars
        .addBinding()
        .toProvider(com.codenvy.machine.MaintenanceConstraintProvider.class);

    install(new org.eclipse.che.plugin.docker.machine.proxy.DockerProxyModule());

    install(new SystemPermissionsJpaModule());
    install(new org.eclipse.che.multiuser.api.permission.server.PermissionsModule());
    install(new com.codenvy.api.node.server.NodeModule());
    install(
        new org.eclipse.che.multiuser.permission.workspace.server.WorkspaceApiPermissionsModule());

    install(
        new FactoryModuleBuilder()
            .implement(
                org.eclipse.che.api.machine.server.spi.Instance.class,
                com.codenvy.machine.HostedDockerInstance.class)
            .implement(
                org.eclipse.che.api.machine.server.spi.InstanceProcess.class,
                org.eclipse.che.plugin.docker.machine.DockerProcess.class)
            .implement(
                org.eclipse.che.plugin.docker.machine.node.DockerNode.class,
                com.codenvy.machine.RemoteDockerNode.class)
            .implement(
                org.eclipse.che.plugin.docker.machine.DockerInstanceRuntimeInfo.class,
                com.codenvy.machine.HostedServersInstanceRuntimeInfo.class)
            .build(org.eclipse.che.plugin.docker.machine.DockerMachineFactory.class));

    MapBinder<String, EnvironmentBackupManager> backupManagers =
        MapBinder.newMapBinder(binder(), String.class, EnvironmentBackupManager.class);
    backupManagers.addBinding("compose").to(DockerEnvironmentBackupManager.class);
    backupManagers.addBinding("dockerfile").to(DockerEnvironmentBackupManager.class);
    backupManagers.addBinding("dockerimage").to(DockerEnvironmentBackupManager.class);

    bind(org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider.class)
        .to(com.codenvy.machine.RemoteWorkspaceFolderPathProvider.class);

    install(new org.eclipse.che.plugin.docker.machine.ext.DockerExtServerModule());

    bind(com.codenvy.machine.backup.WorkspaceFsBackupScheduler.class).asEagerSingleton();

    bind(String.class)
        .annotatedWith(Names.named("che.workspace.che_server_endpoint"))
        .to(Key.get(String.class, Names.named("che.api")));

    //        install(new com.codenvy.router.MachineRouterModule());

    bind(org.eclipse.che.api.workspace.server.event.MachineStateListener.class).asEagerSingleton();

    install(new org.eclipse.che.plugin.docker.machine.DockerMachineModule());
    Multibinder<org.eclipse.che.api.machine.server.spi.InstanceProvider>
        machineImageProviderMultibinder =
            Multibinder.newSetBinder(
                binder(), org.eclipse.che.api.machine.server.spi.InstanceProvider.class);
    machineImageProviderMultibinder
        .addBinding()
        .to(org.eclipse.che.plugin.docker.machine.DockerInstanceProvider.class);

    //workspace activity service
    install(new com.codenvy.plugin.activity.inject.WorkspaceActivityModule());

    MapBinder<String, com.codenvy.machine.MachineServerProxyTransformer> mapBinder =
        MapBinder.newMapBinder(
            binder(), String.class, com.codenvy.machine.MachineServerProxyTransformer.class);
    mapBinder
        .addBinding(org.eclipse.che.api.machine.shared.Constants.TERMINAL_REFERENCE)
        .to(com.codenvy.machine.TerminalServerProxyTransformer.class);
    mapBinder
        .addBinding(org.eclipse.che.api.machine.shared.Constants.EXEC_AGENT_REFERENCE)
        .to(com.codenvy.machine.TerminalServerProxyTransformer.class);
    mapBinder
        .addBinding(org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE)
        .to(com.codenvy.machine.WsAgentServerProxyTransformer.class);

    install(new org.eclipse.che.plugin.machine.ssh.SshMachineModule());
    bind(org.eclipse.che.multiuser.permission.factory.FactoryPermissionsFilter.class);

    bind(MachineInstanceProvider.class).to(com.codenvy.machine.HostedMachineProviderImpl.class);

    final Multibinder<MessageBodyAdapter> adaptersMultibinder =
        Multibinder.newSetBinder(binder(), MessageBodyAdapter.class);
    adaptersMultibinder.addBinding().to(FactoryMessageBodyAdapter.class);
    adaptersMultibinder.addBinding().to(WorkspaceConfigMessageBodyAdapter.class);
    adaptersMultibinder.addBinding().to(WorkspaceMessageBodyAdapter.class);
    adaptersMultibinder.addBinding().to(StackMessageBodyAdapter.class);

    final MessageBodyAdapterInterceptor interceptor = new MessageBodyAdapterInterceptor();
    requestInjection(interceptor);
    bindInterceptor(subclassesOf(CheJsonProvider.class), names("readFrom"), interceptor);

    //ldap
    if (LdapAuthenticationHandler.TYPE.equals(System.getProperty("auth.handler.default"))) {
      install(new LdapModule());
    }

    bind(org.eclipse.che.api.workspace.server.WorkspaceFilesCleaner.class)
        .to(com.codenvy.workspace.WorkspaceFilesCleanUpScriptExecutor.class);
    install(new com.codenvy.machine.agent.CodenvyAgentModule());
    bind(org.eclipse.che.api.environment.server.InfrastructureProvisioner.class)
        .to(com.codenvy.machine.agent.CodenvyInfrastructureProvisioner.class);

    MapBinder<String, org.eclipse.che.plugin.docker.machine.ServerEvaluationStrategy> strategies =
        MapBinder.newMapBinder(
            binder(),
            String.class,
            org.eclipse.che.plugin.docker.machine.ServerEvaluationStrategy.class);
    strategies
        .addBinding("codenvy")
        .to(com.codenvy.machine.CodenvyDockerServerEvaluationStrategy.class);

    bindConstant()
        .annotatedWith(Names.named("che.docker.server_evaluation_strategy"))
        .to("codenvy");
    install(new WorkspaceInfrastructureModule());

    install(new org.eclipse.che.plugin.docker.machine.dns.DnsResolversModule());

    bind(TemplateProcessor.class).to(ThymeleafTemplateProcessorImpl.class);

    bind(new TypeLiteral<Map<String, String>>() {})
        .annotatedWith(Names.named("codenvy.email.logos"))
        .toInstance(
            ImmutableMap.<String, String>builder()
                .put("codenvy", "/email-templates/logos/logo-codenvy-white.png")
                .put("codenvySmall", "/email-templates/logos/196x196-white.png")
                .put("linkedin", "/email-templates/logos/logo_social_linkedin.png")
                .put("facebook", "/email-templates/logos/logo_social_facebook.png")
                .put("twitter", "/email-templates/logos/logo_social_twitter.png")
                .put("medium", "/email-templates/logos/logo_social_medium.png")
                .build());

    String[] blockedCountries = {
      "Cuba",
      "Iran",
      "Korea, North",
      "Sudan",
      "Syria",
      "Iran, Islamic Republic Of",
      "Syrian Arab Republic",
      "Korea, Democratic People'S Republic of",
      "Korea, Democratic People",
      "ایران، جمهوری اسلامی",
      "الجمهورية العربية السورية"
    };
    bind(new TypeLiteral<String[]>() {})
        .annotatedWith(Names.named("auth.blocked_country_names"))
        .toInstance(blockedCountries);

    bind(org.eclipse.che.api.agent.server.filters.AddExecAgentInWorkspaceFilter.class);
    bind(org.eclipse.che.api.agent.server.filters.AddExecAgentInStackFilter.class);

    bind(org.eclipse.che.api.workspace.server.event.WorkspaceJsonRpcMessenger.class)
        .asEagerSingleton();
  }
}
