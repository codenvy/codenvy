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
package com.codenvy.machine.agent;

import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.environment.server.AgentConfigApplier;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.inject.CheBootstrap;
import org.eclipse.che.plugin.docker.machine.ext.provider.DockerExtConfBindingProvider;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;

/**
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class CodenvyInContainerInfrastructureProvisionerTest {
    private static final String SYNC_KEY             = "testSyncKey";
    private static final String TEST_PROJECTS_FOLDER = "/test/projects/folder";
    private static final String DEV_MACHINE_NAME     = "dev";

    @Mock
    private AgentConfigApplier agentConfigApplier;

    private EnvironmentImpl                             environment;
    private CheServicesEnvironmentImpl                  internalEnv;
    private ExtendedMachineImpl                         extendedMachine;
    private ExtendedMachineImpl                         devExtendedMachine;
    private CheServiceImpl                              cheService;
    private CodenvyInContainerInfrastructureProvisioner provisioner;

    @BeforeMethod
    public void setUp() throws Exception {
        devExtendedMachine = new ExtendedMachineImpl();
        extendedMachine = new ExtendedMachineImpl();
        environment = new EnvironmentImpl();
        cheService = new CheServiceImpl();
        CheServiceImpl devCheService = new CheServiceImpl();
        internalEnv = new CheServicesEnvironmentImpl();

        provisioner = new CodenvyInContainerInfrastructureProvisioner(agentConfigApplier,
                                                                      SYNC_KEY,
                                                                      TEST_PROJECTS_FOLDER);

        Map<String, ExtendedMachineImpl> extendedMachines = new HashMap<>();
        extendedMachines.put("non-dev", extendedMachine);
        extendedMachines.put(DEV_MACHINE_NAME, devExtendedMachine);
        environment.setMachines(extendedMachines);
        Map<String, CheServiceImpl> services = new HashMap<>();
        services.put("non-dev", cheService);
        services.put(DEV_MACHINE_NAME, devCheService);
        internalEnv.setServices(services);
        devExtendedMachine.setAgents(asList("org.eclipse.che.exec",
                                            "org.eclipse.che.terminal",
                                            "org.eclipse.che.ws-agent"));
        extendedMachine.setAgents(asList("org.eclipse.che.ws-agent2",
                                         "not.che.ws-agent",
                                         "org.eclipse.che.terminal"));
    }

    @Test
    public void shouldApplyAgentConfigOnSingleMachineProvisioning() throws Exception {
        provisioner.provision(extendedMachine, cheService);

        verify(agentConfigApplier).apply(extendedMachine, cheService);
    }

    @Test(expectedExceptions = EnvironmentException.class,
          expectedExceptionsMessageRegExp = "test exception")
    public void shouldThrowEnvironmentExceptionIfAgentApplierThrowsAgentExceptionOnSingleMachineProvisioning()
            throws Exception {

        doThrow(new AgentException("test exception")).when(agentConfigApplier).apply(extendedMachine, cheService);

        provisioner.provision(extendedMachine, cheService);
    }

    @Test(expectedExceptions = EnvironmentException.class,
          expectedExceptionsMessageRegExp = "ws-machine is not found on agents applying")
    public void shouldThrowEnvExceptionIfNoMachinesFoundInEnvironment() throws Exception {
        environment.setMachines(Collections.emptyMap());

        provisioner.provision(environment, internalEnv);
    }

    @Test(expectedExceptions = EnvironmentException.class,
          expectedExceptionsMessageRegExp = "ws-machine is not found on agents applying")
    public void shouldThrowEnvExceptionIfNoMachinesWithDevAgentFoundInEnvironment() throws Exception {
        environment.setMachines(singletonMap("machine1", extendedMachine));
        extendedMachine.setAgents(asList("org.eclipse.che.ws-agent2",
                                         "not.che.ws-agent",
                                         "org.eclipse.che.terminal",
                                         "org.eclipse.che.exec"));

        provisioner.provision(environment, internalEnv);
    }

    @Test
    public void shouldAddSyncKeyEnvVariableInDevMachineOnEnvProvision() throws Exception {
        internalEnv.getServices().get(DEV_MACHINE_NAME).getEnvironment().put("test", "test1");

        provisioner.provision(environment, internalEnv);

        assertEquals(internalEnv.getServices().get(DEV_MACHINE_NAME).getEnvironment().get("test"), "test1");
        assertEquals(internalEnv.getServices()
                                .get(DEV_MACHINE_NAME)
                                .getEnvironment()
                                .get(CodenvyInContainerInfrastructureProvisioner.SYNC_KEY_ENV_VAR_NAME), SYNC_KEY);
    }

    @Test
    public void shouldAddLocalConfDirEnvVariableInDevMachineOnEnvProvision() throws Exception {
        internalEnv.getServices().get(DEV_MACHINE_NAME).getEnvironment().put("test", "test1");

        provisioner.provision(environment, internalEnv);

        assertEquals(internalEnv.getServices().get(DEV_MACHINE_NAME).getEnvironment().get("test"), "test1");
        assertEquals(internalEnv.getServices()
                                .get(DEV_MACHINE_NAME)
                                .getEnvironment()
                                .get(CheBootstrap.CHE_LOCAL_CONF_DIR),
                     DockerExtConfBindingProvider.EXT_CHE_LOCAL_CONF_DIR);
    }

    @Test
    public void shouldAddRsyncAgentBeforeWsAgentInDevMachineOnEnvProvision() throws Exception {
        devExtendedMachine.setAgents(asList("org.eclipse.che.exec",
                                            "org.eclipse.che.terminal",
                                            "org.eclipse.che.ws-agent"));
        List<String> expectedAgents = asList("org.eclipse.che.exec",
                                             "org.eclipse.che.terminal",
                                             "com.codenvy.rsync_in_machine",
                                             "org.eclipse.che.ws-agent");

        provisioner.provision(environment, internalEnv);

        assertEquals(devExtendedMachine.getAgents(), expectedAgents);
    }

    @Test
    public void shouldAddVolumeForProjectsFolderOnEnvironmentProvision() throws Exception {
        internalEnv.getServices().get(DEV_MACHINE_NAME).getVolumes().add("/some/volume");
        internalEnv.getServices().get(DEV_MACHINE_NAME).getVolumes().add("/some/bind:/mount/volume");

        provisioner.provision(environment, internalEnv);

        assertEqualsNoOrder(internalEnv.getServices().get(DEV_MACHINE_NAME).getVolumes().toArray(),
                            asList(TEST_PROJECTS_FOLDER, "/some/volume", "/some/bind:/mount/volume").toArray());
    }

    @Test
    public void shouldApplyAgentConfigToEnvironmentOnEnvironmentProvision() throws Exception {
        provisioner.provision(environment, internalEnv);

        verify(agentConfigApplier).apply(environment, internalEnv);
    }

    @Test(expectedExceptions = EnvironmentException.class,
          expectedExceptionsMessageRegExp = "test exception")
    public void shouldThrowEnvironmentExceptionIfAgentApplierThrowsAgentExceptionOnEnvironmentProvisioning()
            throws Exception {

        doThrow(new AgentException("test exception")).when(agentConfigApplier).apply(environment, internalEnv);

        provisioner.provision(environment, internalEnv);
    }
}
