---
title: Scaling
excerpt: "Add and remove machine nodes to increase workspace capacity."
layout: docs
overview: true
permalink: /docs/scaling/
---
You can add up to 1000 physical nodes to Codenvy to provide workspace elasticity (you will need a [Codenvy Enterprise license](http://codenvy.readme.io/docs/licensing) to unlock clustering). Workspaces are the individual unit of work that houses projects.  Workspaces are bound to environments that are powered by machines. Every workspace has at least one environment and users can configure additional ones.  Machines provide the unit of resource that a project requires to perform editing, building or running.

Codenvy runs a Docker Swarm cluster within it. The default installation of Codenvy has an embedded machine node that also acts as the cluster manager. You can register additional servers into this cluster and Codenvy will take care of balancing the workspace machines appropriately.
#### Making Codenvy Reachable
When Codenvy is initially installed, it is bound to the hostname `codenvy.onprem` against the IP address `172.17.42.1` against interface `docker0`. This combination makes it possible for you to use all of Codenvy's features quickly with minimal admin configuration.\n\nThe IP address `172.17.42.1` is not reachable by services running on different physical nodes. The additional physical nodes connect to the main Codenvy node using both the hostname and the IP address. Before you add a 2nd physical node, you must configure Codenvy with a property that informs new nodes how they can connect back to the main Codenvy node.\n\nIn the Codenvy configuration properties, change `$machine_extra_hosts=\<hostname>:<ip-addr>\`. You can continue to use the `codenvy.onprem` hostname, or you can use another DNS entry.  Change the IP address from `172.17.42.1` to an IP address on the Codenvy main node that is reachable by external nodes. \n\nWhen additional physical nodes come online, Codenvy will tell swarm to launch workspace machines on those nodes. Those workspace machines will launch Docker containers, which themselves have a mini Codenvy server, which we call a workspace agent inside. That workspace agent will use the value of `$machine_extra_hosts` to determine how it connects back to Codenvy.  


# Add Nodes  
Use the CLI to register a new machine server - see [CLI documentation](http://codenvy.readme.io/docs/cli#codenvy-add-node) for the `add-node` command.

It takes about 2 minutes for the new node to come online.

When this command is called, Codenvy will connect to the node, install puppet, activate itself by installing Docker Swarm and other utilities, and then register its readiness within the Codenvy cluster.
# Remove Nodes  
Use the CLI to remove one of the machine servers - see [CLI documentation](http://codenvy.readme.io/docs/cli#codenvy-remove-node) for the `remove-node` command.

It takes about 2 minutes for the new node to be removed.
# Docker Swarm  
Codenvy deploys a Docker Swarm cluster internally to manage the distribution and orchestration of containers used to power various workspaces. You can connect directly to the Swarm port to monitor what Swarm is doing at `http://codenvy.onprem:23750/info`. This connection is only possible from the master node, i.e. only local connections are allowed.
# Distributing Internal Services  
It is possible to distribute the internal services of Codenvy onto different physical nodes. This offers higher degrees of resilience and more scalability around core user management nodes.  See [Installation - Multi-Node](http://codenvy.readme.io/docs/architecture#multi-node-installation) for details on how to set this up.
# Capping Resources  
By default, users can create any number of workspaces consuming RAM of amounts up to 100GB. You can apply system restrictions to control resource consumption. Configure these properties in the Codenvy puppet configuration file.
```text  
## The number of workspaces that a user is allowed to create in his account.
limits.user.workspaces.count=30

## Maximum RAM consumed by a user across all of his workspaces.
limits.user.workspaces.ram=100gb

## Maximum size user can set a single workspace to.
limits.workspace.env.ram=16gb\
```
