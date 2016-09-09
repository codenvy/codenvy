var fixStacks = function() {
    var organizationDb = db.getSiblingDB('organization');
    organizationDb.stacks.update({"workspaceConfig.environments.default.machines.dev-machine.agents":"ws-agent"}, {$push : {"workspaceConfig.environments.default.machines.dev-machine.agents" : "org.eclipse.che.ws-agent"}}, {multi:true})
    organizationDb.stacks.update({"workspaceConfig.environments.default.machines.dev-machine.agents":"ws-agent"}, {$push : {"workspaceConfig.environments.default.machines.dev-machine.agents" : "org.eclipse.che.terminal"}}, {multi:true})
    organizationDb.stacks.update({"workspaceConfig.environments.default.machines.dev-machine.agents":"ws-agent"}, {$push : {"workspaceConfig.environments.default.machines.dev-machine.agents" : "org.eclipse.che.ssh"}}, {multi:true})
    organizationDb.stacks.update({"workspaceConfig.environments.default.machines.dev-machine.agents":"ws-agent"}, {$pull : {"workspaceConfig.environments.default.machines.dev-machine.agents" : "ws-agent"}}, , {multi:true})
}

fixStacks();
