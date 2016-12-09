---
title: Installation - Multi Server
excerpt: "Install Codenvy across many nodes for scalability"
layout: docs
overview: true
permalink: /docs/installation-multi-server/
---
The multi-node installation of Codenvy lets you distribute the internal services that Codenvy depends upon onto separate physical nodes. ***You do not need this configuration*** if you want to scale Codenvy. You can perform a simple single server installation and then add up to 1000 machine nodes using the embedded Swarm cluster manager - see [Scaling](doc:scaling).

This installation is activated with the `--multi` flag for the installer. The nodes that Codenvy will be installed onto must be pre-configured with hostnames, SSH keys, user names, and ports. After these nodes are configured, the Codenvy installer will SSH into each node and install the appropriate software onto each server.
#### Properties File
If you want to customize the configuration before the installation begins, start with a `codenvy.properties` file that is tailored for a multi-node installation.  See: [installation pre-reqs.](https://codenvy.readme.io/docs/installation?#installation)  


# DNS Names  
Each node must be accessible by a pre-configured DNS name. You cannot use IP addresses, as the internal Codenvy services depend upon hostnames. 

The `site` and `machine` nodes must have public IP addresses.  All other nodes can communicate through port openings.  Each node must be reachable through SSH.  It is allowed to use fake DNS names, provided that all nodes can resolve them. 

| DNS>>>>>>>>>>>>>>>>>>>>>>>>>   | Description   
| --- | --- 
| `[your_hostname]`   | This is the primary hostname that users will access Codenvy on.   
| `puppet-master.[your_hostname]`   | Admin node that will host puppet master and the Codenvy installer.   
| `data.[your_hostname]`   | MongoDB and LDAP used for user account and profile storage.   
| `api.[your_hostname]`   | Core Codenvy REST services that manage workspaces and environments.   
| `machine.[your_dns]`   | Machines power the workspaces used by developers to edit, build and run. This node runs a Docker swarm cluster which can add additional servers to the cluster.   


# SSH Keys  
All nodes should be reachable by SSH. Generate an SSH key pair and store it in `./ssh` of the puppet master. Take the public key part and add to all other nodes `~/.ssh/authorized_keys` file.  Once this is configured you should verify that each node is reachable from the puppet master node.
# Linux Users  
The default installation assumes that Codenvy is installed as root user. You can create a different Linux user that is not named `codenvy-im` or `codenvy` as these users are created created and used by Codenvy during installation.

Your users account must be able to execute sudo commands without a password prompt.  
```shell  
# Enable <user-name> to use sudo without a password prompt
echo "<user-name> ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers
```
