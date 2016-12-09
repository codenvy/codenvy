---
title: Installation - Troubleshooting
excerpt: "Helpful tips and tactics to use if things don't go as planned."
layout: docs
overview: true
permalink: /docs/installation-troubleshooting/
---
The Codenvy installation is designed to be simple and fail-safe. Things can go wrong, though, and diagnostics are needed.
# Where We Install Stuff  


| Location>>>>>>>>>   | What   
| --- | --- 
| `~/codenvy`   | Bootstrap properties, the Codenvy CLI, Java, bootstrap installation log, downloaded Codenvy binaries. You can change this location with the `-install-directory=<directory>` installation option.   
| `/home/codenvy-im`   | Codenvy manager, a service that monitors Codenvy, performs upgrades, and configures the system.   
| `/home/codenvy`   | Codenvy server, long-term storage for runtime images.   
| `/etc/puppet`\n`/var`   | Puppet, used to install Codenvy and maintain proper configuration of its underlying dependencies like HAProxy and Postgresql.   


# Essential Files  


| File   | Role   
| --- | --- 
| `~/codenvy/cli/logs/cli.log`   | `/var/log/puppet/puppet-agent.log`   
| Output log of the codenvy manager.   | Output log of puppet.   
| `/etc/puppet/puppet.conf`   | Puppet configuration properties.   
| `/home/codenvy/tomcat/logs/catalina.out`   | Output log of Codenvy.   
| `/home/codenvy/codenvy-data/che-machines-logs/`   | Output log of workspace machines.   

If you modify the puppet configuration files, puppet will detect the changes and re-process the file automatically. Change detection happens on an interval of 300 seconds.  You can modify `runinterval` in `/etc/puppet/puppet.conf` to change the interval.
# Verifying The Install  
### Verify Administrator Access
```http  
# Loads the login page
http://codenvy.onprem/

# Credentials - remember to change these
user: admin
pass: password\
```
### Verify Command Line Control
```shell  
# Refresh your shell to add the CLI to your path
source ${HOME}/.bashrc

# Verify that the admin CLI is installed and working
codenvy im-version\
```
### Verify Puppet
Inside Codenvy is a standard [Puppet](https://puppetlabs.com/) system to distribute files and orchestrate the install. If you are familiar with Puppet and suspect an issue in that section you can troubleshoot it as you would any other Puppet system.
```shell  
# Checks the health of the puppet master
service puppet status

# Starts puppet master if it is disabled.
service puppet start

# Checks the status of the main Codenvy server
service codenvy status

# Applies any configuration changes from puppet master
# Run this after any changes to Codenvy configuration files after installation
puppet agent -t

# Puppet checks for updates to configuration every 300 seconds.
# You can shorten this intervale in /etc/puppet/puppet.conf
runinterval = <seconds>\
```

# Diagnostics  
If Codenvy is not working to specification. These are the commands and logs that we gather for investigation.
```text  
/etc/sysconfig/network-scripts/ifcfg-*
/etc/NetworkManager/*
/var/log/sa/sar*
/etc/yum.repos.d/*
/var/log/yum*
/var/log/puppet/*
/var/log/nginx/*
/var/log/mongodb/*
/var/log/docker-distribution.log
/var/log/fail2ban.log
/var/log/haproxy.log
/var/log/messages
/var/log/swarm.log
/var/log/yum.log
/etc/puppet/auth.conf
/etc/puppet/autosign.conf
/etc/puppet/fileserver.conf
/etc/puppet/puppet.conf
/home/codenvy/codenvy-data/che-machines-logs/*
/home/codenvy/codenvy-data/logs/*
/home/codenvy/codenvy-data/conf/machine.properties
/home/codenvy/codenvy-data/conf/general.properties
/etc/resolv.conf
/etc/hostname
/etc/hosts
/usr/local/swarm/node_list\
```

```shell  
facter fqdn
hostname
hostname -f
df -h
df -i
mount
free
journalctl --since today
ps auxf
w
iptables-save
puppet agent -t\
```
We have a [script that you can run on your system](https://gist.githubusercontent.com/eivantsov/7744cbf9ce6547f2be5e93aa05b5bce2/raw/54a636d9d92753b2066842bb5c8eecdfcafb956a/report.sh) that packages these files and the outputs of all commands into a TAR file that you can send support. We will be incorporating this into `codenvy support` CLI in an upcoming release.
# Potential Issues  
** Issue: Workspace Agent Fails During API Endpoint Lookup**: If Codenvy was installed on an IP address that is not reachable by other nodes, Codenvy workspace agents may start generating errors. Codenvy workspaces are bound to machines which may be distributed to different nodes by Swarm. By default, workspaces are launched into machines that are running on the same node as Codenvy. Whether the workspaces are on the same node or distributed nodes, you may run into communication issues.

Inside of a workpace machine, Codenvy injects a workpace agent, which is a Tomcat service that runs within the workspace machine that connects back to Codenvy's workspace master API. Codenvy and the workspace can then communicate bi-directionally.

If the hostname or the IP address for the `codenvy` hostname is unreachable, this workspace agent will gracefully fail and provide a friendly error message to the user. To resolve this issue:

1. Reconfigure Codenvy to operate on a publicly available IP address, or:
2. Have Codenvy use a DNS hostname which is publicly reachable by any client

Once this has been completed, you must also update the Codenvy puppet manifest:
```text  
codenvy im-config machine_extra_hosts "<hostname>:<public-ip-address>"
```
