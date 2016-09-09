// databases
var organization = db.getSiblingDB("organization");
var factory      = db.getSiblingDB("factory");

// collections
var workspaces = organization.getCollection("workspaces2");
var stacks     = organization.getCollection("stacks");
var factories  = factory.getCollection("factory")


// migration for workspaces
function migrateWorkspaces() {
    print("Migrating workspaces");
    workspaces.find({ "config" : { $exists : true } }).forEach(function(workspace) {
        workspace.config = adaptConfig(workspace.config, workspace._id);
        workspaces.update({ "_id" : workspace._id }, workspace);
    })
}

// migration for stacks
function migrateStacks() {
    print("Migrating stacks")
    stacks.find({ "workspaceConfig" : { $exists : true } }).forEach(function(stack) {
        stack.workspaceConfig = adaptConfig(stack.workspaceConfig, stack._id);
        stacks.update({ "_id" : stack._id }, stack);
    })
}

// migration for factories
function migrateFactories() {
    print("Migrating factories");
    factories.find({ "factory.workspace" : { $exists : true } }).forEach(function(f) {
        f.factory.workspace = adaptConfig(f.factory.workspace, f._id);
        factories.update({ "_id" : f._id}, f)
    });
}

function adaptConfig(config) {
    var environments = config.environments;
    if (environments) {
        for (var env in environments) {
            environments[env] = updateAgents(environments[env]);
        }
    }

    return config;
}

function updateAgents(env) {
    for (var machine in env.machines) {
       var agents = env.machines[machine].agents;
       if (agents) {
            var wsAgent = env.machines[machine].agents.indexOf("ws-agent");
            if (wsAgent != -1) {
                env.machines[machine].agents[wsAgent] = "org.eclipse.che.ws-agent";
                env.machines[machine].agents.push("org.eclipse.che.terminal", "org.eclipse.che.ssh");
           }
       }
    }

    return env;
}


migrateWorkspaces();
migrateStacks();
migrateFactories();
