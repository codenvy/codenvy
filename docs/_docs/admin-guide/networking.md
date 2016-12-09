---
title: Networking
excerpt: "Details related to how Codenvy advertises its services for users and internal nodes"
layout: docs
overview: true
permalink: /docs/networking/
---
Internal to Codenvy are dozens of services that communicate with each other. These services use hostname entries for communication.  Due to the nature of various internal services, IP addresses are not used, only hostnames.

You can install Codenvy using a fake hostname or a publicly registered DNS name. When you install the initial version of Codenvy, we default to a fake `codenvy` hostname and pick the IP address bound to the `docker` virtual interface. We do this to simplify the installation process, but this IP address will not be reachable by services unless they are on the same physical node as Codenvy. Codenvy will be fully functional during the initial installation, but before you add additional nodes you must resolve the reachability issue. You can either change the hostname of Codenvy to a hostname that is reachable by any user such as DNS, or map the `codenvy` hostname to an IP address that is externally reachable.
# Change Hostname  
There are a couple ways to change the hostname that Codenvy is using for communicate.

### CLI
[See `codenvy config`](http://codenvy.readme.io/docs/cli#config)

### Codenvy Configuration
You can set the value of `$host_url` that is located in the Codenvy configuration properties.
# Issue: Workspace Agent Fails During API Endpoint Lookup  
If Codenvy was installed on an IP address that is not reachable by other nodes, Codenvy workspace agents may start generating errors. Codenvy workspaces are bound to machines which may be distributed to different nodes by Swarm. By default, workspaces are launched into machines that are running on the same node as Codenvy.  Whether the workspaces are on the same node or distributed nodes, you may run into communication issues.

Inside of a workpace machine, Codenvy injects a workpace agent, which is a Tomcat service that runs within the workspace machine that connects back to Codenvy's workspace master API. Codenvy and the workspace can then communicate bi-directionally.  

If the hostname or the IP address for the `codenvy` hostname is unreachable, this workspace agent will gracefully fail and provide a friendly error message to the user. To resolve this issue:
1. Reconfigure Codenvy to operate on a publicly available IP address, or:
2. Have Codenvy use a DNS hostname which is publicly reachable by any client

Once this has been completed, you must also update the Codenvy puppet manifest:
```json  
$machine_extra_hosts = "<hostname>:<public-ip-address>"
```
