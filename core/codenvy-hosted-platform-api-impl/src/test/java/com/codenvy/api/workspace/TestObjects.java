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

import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineRuntimeInfoImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceRuntimeImpl;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;

/**
 * Test util class, helps to create test objects.
 *
 * @author Yevhenii Voevodin
 */
public final class TestObjects {

    private static final String DEFAULT_USER_NAME = "user123456";

    /** Creates users workspace object based on the owner and machines RAM. */
    public static WorkspaceImpl createWorkspace(String owner, String devMachineRam, String... machineRams) {
        final List<MachineConfigImpl> machineConfigs = new ArrayList<>(1 + machineRams.length);
        machineConfigs.add(createMachineConfig(true, (int)Size.parseSizeToMegabytes(devMachineRam)));
        for (String machineRam : machineRams) {
            machineConfigs.add(createMachineConfig(false, (int)Size.parseSizeToMegabytes(machineRam)));
        }
        return WorkspaceImpl.builder()
                            .generateId()
                            .setConfig(WorkspaceConfigImpl.builder()
                                                          .setName(NameGenerator.generate("workspace", 2))
                                                          .setEnvironments(singletonList(new EnvironmentImpl("dev-env",
                                                                                                             null,
                                                                                                             machineConfigs)))
                                                          .setDefaultEnv("dev-env")
                                                          .build())
                            .setNamespace(owner)
                            .setTemporary(false)
                            .setStatus(WorkspaceStatus.STOPPED)
                            .build();
    }

    /** Creates workspace config object based on the machines RAM. */
    public static WorkspaceConfig createConfig(String devMachineRam, String... machineRams) {
        return createWorkspace(DEFAULT_USER_NAME, devMachineRam, machineRams).getConfig();
    }

    /** Creates runtime workspace object based on the machines RAM. */
    public static WorkspaceImpl createRuntime(String devMachineRam, String... machineRams) {
        final WorkspaceImpl workspace = createWorkspace(DEFAULT_USER_NAME, devMachineRam, machineRams);
        final String env = workspace.getConfig().getDefaultEnv();
        final List<MachineConfigImpl> machineConfigs = workspace.getConfig()
                                                                .getEnvironment(env)
                                                                .get()
                                                                .getMachineConfigs();
        final MachineConfigImpl devCfg = machineConfigs.stream()
                                                       .filter(MachineConfigImpl::isDev)
                                                       .findFirst()
                                                       .get();
        final WorkspaceRuntimeImpl runtime =
                new WorkspaceRuntimeImpl(workspace.getConfig().getDefaultEnv(),
                                         null,
                                         machineConfigs.stream()
                                                       .map(cfg -> createMachine(workspace.getId(), env, cfg))
                                                       .collect(toList()),
                                         createMachine(workspace.getId(), env, devCfg));
        workspace.setStatus(RUNNING);
        workspace.setRuntime(runtime);
        return workspace;
    }

    /** Creates machine config object based on ram and dev flag. */
    public static MachineConfigImpl createMachineConfig(boolean isDev, int ramLimit) {
        return new MachineConfigImpl(isDev,
                                     NameGenerator.generate("machine", 2),
                                     "docker",
                                     new MachineSourceImpl("recipe").setLocation("recipe-location"),
                                     new LimitsImpl(ramLimit),
                                     Arrays.asList(new ServerConfImpl("ref1", "8080/tcp", "https", "some/path"),
                                                   new ServerConfImpl("ref2", "9090/udp", "protocol", "/some/path")),
                                     Collections.singletonMap("key1", "value1"));
    }

    /** Creates machine impl based on configuration. */
    public static MachineImpl createMachine(String wsId, String envName, MachineConfigImpl cfg) {
        return MachineImpl.builder()
                          .setConfig(cfg)
                          .setId(NameGenerator.generate("machine", 10))
                          .setOwner(DEFAULT_USER_NAME)
                          .setStatus(MachineStatus.RUNNING)
                          .setWorkspaceId(wsId)
                          .setEnvName(envName)
                          .setRuntime(new MachineRuntimeInfoImpl(new HashMap<>(),
                                                                 new HashMap<>(),
                                                                 new HashMap<>()))
                          .build();
    }

    private TestObjects() {}
}
