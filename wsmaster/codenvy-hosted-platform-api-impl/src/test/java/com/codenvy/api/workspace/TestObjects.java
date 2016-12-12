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
package com.codenvy.api.workspace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineLimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineRuntimeInfoImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentRecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceRuntimeImpl;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Size;
import org.eclipse.che.plugin.docker.compose.ComposeEnvironment;
import org.eclipse.che.plugin.docker.compose.ComposeServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;

/**
 * Test util class, helps to create test objects.
 *
 * @author Yevhenii Voevodin
 * @author Alexander Garagatyi
 */
public final class TestObjects {

    private static final String       DEFAULT_USER_NAME = "user123456";
    private static final ObjectMapper YAML_PARSER       = new ObjectMapper(new YAMLFactory());

    public static EnvironmentImpl createEnvironment(String devMachineRam, String... machineRams) throws Exception {
        Map<String, ExtendedMachineImpl> machines = new HashMap<>();
        machines.put("dev-machine", new ExtendedMachineImpl(singletonList("org.eclipse.che.ws-agent"),
                                                            emptyMap(),
                                                            singletonMap("memoryLimitBytes",
                                                                         Long.toString(Size.parseSize(devMachineRam)))));
        HashMap<String, ComposeServiceImpl> services = new HashMap<>(1 + machineRams.length);
        services.put("dev-machine", createService());
        for (int i = 0; i < machineRams.length; i++) {
            services.put("machine" + i, createService());
            // null is allowed to reproduce situation with default RAM size
            if (machineRams[i] != null) {
                machines.put("machine" + i, new ExtendedMachineImpl(null,
                                                                    null,
                                                                    singletonMap("memoryLimitBytes",
                                                                                 Long.toString(Size.parseSize(machineRams[i])))));
            }
        }
        ComposeEnvironment composeEnvironment = new ComposeEnvironment();
        composeEnvironment.setServices(services);
        String yaml = YAML_PARSER.writeValueAsString(composeEnvironment);
        EnvironmentRecipeImpl recipe = new EnvironmentRecipeImpl("compose", "application/x-yaml", yaml, null);


        return new EnvironmentImpl(recipe,
                                   machines);
    }

    /** Creates users workspace object based on the owner and machines RAM. */
    public static WorkspaceImpl createWorkspace(String owner, String devMachineRam, String... machineRams) throws Exception {

        return WorkspaceImpl.builder()
                            .generateId()
                            .setConfig(WorkspaceConfigImpl.builder()
                                                          .setName(NameGenerator.generate("workspace", 2))
                                                          .setEnvironments(singletonMap("dev-env",
                                                                                        createEnvironment(devMachineRam, machineRams)))
                                                          .setDefaultEnv("dev-env")
                                                          .build())
                            .setAccount(new AccountImpl("accountId", owner, "test"))
                            .setTemporary(false)
                            .setStatus(WorkspaceStatus.STOPPED)
                            .build();
    }

    /** Creates workspace config object based on the machines RAM. */
    public static WorkspaceConfig createConfig(String devMachineRam, String... machineRams) throws Exception {
        return createWorkspace(DEFAULT_USER_NAME, devMachineRam, machineRams).getConfig();
    }

    /** Creates runtime workspace object based on the machines RAM. */
    public static WorkspaceImpl createRuntime(String devMachineRam, String... machineRams) throws Exception {
        final WorkspaceImpl workspace = createWorkspace(DEFAULT_USER_NAME, devMachineRam, machineRams);
        final String envName = workspace.getConfig().getDefaultEnv();
        EnvironmentImpl env = workspace.getConfig().getEnvironments().get(envName);
        Map.Entry<String, ExtendedMachineImpl> devMachine = env.getMachines()
                                                               .entrySet()
                                                               .stream()
                                                               .filter(entry -> entry.getValue().getAgents() != null &&
                                                                                entry.getValue().getAgents()
                                                                                     .contains("org.eclipse.che.ws-agent"))
                                                               .findAny()
                                                               .get();
        final WorkspaceRuntimeImpl runtime =
                new WorkspaceRuntimeImpl(workspace.getConfig().getDefaultEnv(),
                                         null,
                                         env.getMachines().entrySet()
                                            .stream()
                                            .map(entry -> createMachine(workspace.getId(),
                                                                        envName,
                                                                        entry.getKey(),
                                                                        devMachine.getKey().equals(entry.getKey()),
                                                                        entry.getValue().getAttributes().get("memoryLimitBytes")))
                                            .collect(toList()),
                                         createMachine(workspace.getId(),
                                                       envName,
                                                       devMachine.getKey(),
                                                       true,
                                                       devMachine.getValue().getAttributes().get("memoryLimitBytes")));
        workspace.setStatus(RUNNING);
        workspace.setRuntime(runtime);
        return workspace;
    }

    private static MachineImpl createMachine(String workspaceId,
                                             String envName,
                                             String machineName,
                                             boolean isDev,
                                             String memoryBytes) {

        return MachineImpl.builder()
                          .setConfig(MachineConfigImpl.builder()
                                                      .setDev(isDev)
                                                      .setName(machineName)
                                                      .setSource(new MachineSourceImpl("some-type")
                                                                         .setContent("some-content"))
                                                      .setLimits(new MachineLimitsImpl((int)Size.parseSizeToMegabytes(memoryBytes + "b")))
                                                      .setType("someType")
                                                      .build())
                          .setId(NameGenerator.generate("machine", 10))
                          .setOwner(DEFAULT_USER_NAME)
                          .setStatus(MachineStatus.RUNNING)
                          .setWorkspaceId(workspaceId)
                          .setEnvName(envName)
                          .setRuntime(new MachineRuntimeInfoImpl(emptyMap(),
                                                                 emptyMap(),
                                                                 emptyMap()))
                          .build();
    }

    private static ComposeServiceImpl createService() {
        ComposeServiceImpl service = new ComposeServiceImpl();
        service.setImage("image");
        return service;
    }

    private TestObjects() {}
}
