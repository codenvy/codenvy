---
title: CLI
excerpt: "Manage your Codenvy installation on the command line."
layout: docs
overview: true
permalink: /docs/cli/
---
The CLI is located at `~/codenvy/codenvy-cli/bin`. It is added to your `PATH` when Codenvy is installed.  You may need to start a new terminal session after the installation of Codenvy for the CLI PATH to be picked up.
# codenvy add-node  

```shell  
codenvy add-node --codenvy-ip $IP node[n].<hostname>\
```
Adds a new machine node to the Codenvy cluster. Machines provide resources to workspaces. You can add up to 1000 machine nodes.

`$IP` (optional) stands for the IP of the Codenvy master host (do not use the IP of the node you are adding). It is only needed if the workspace master does not have a DNS name that a workspace agent can resolve.

`[n]` is replaced with a unique number for the node you're adding - your first additional node should begin at 1 and then increment from there. The new node name must start with `node`.

`<hostname>` is the fully qualified DNS hostname of your Codenvy installation.
#### Codenvy Master DNS Hostname
Before adding nodes your Codenvy master node must be accessible with a fully qualified DNS name. The DNS domain for your master node will be used in the DNS names for all additional nodes (it's how we find the additional nodes and ensure that they're added to the cluster).  

*Steps to Add Node*
1. Create a new machine node running CentOS / RHEL.
2. Create a new entry in your DNS for the machine node's IP that looks like “node1.{my-codenvy-master-domain.com}”. This must be a fully qualified DNS name (FQDN).
3. Open the [appropriate ports](http://codenvy.readme.io/docs/architecture#section-machine-nodes-1-n-) on the machine node.
4. Check that the master node's DNS hostname is reachable from the machine node.
5. Check that the machine node's DNS hostname is reachable from the master node.
6. Check that the master node has an SSH private key placed at `/root/.ssh/id_rsa`.
7. Place the public key that matches your master node's private key into `/root/.ssh/authorized_keys` on the machine node.
8. Check you can SSH between the nodes.
9. Execute the `add-node` command from the master node.

The IM will connect to the node, install puppet, activate itself, and then register itself into the Codenvy routers.

*Troubleshooting*
If the node does not appear to connect then it's likely that it is a DNS issue. Ensure that the DNS entries for the master node and machine node are correct and that each node can see the other using that DNS entry. Once they're correct restart the system (from the master node) execute the following:
`service nginx restart`
`service dnsmasq restart`
`service codenvy restart`
#### Changing DNS of Machine Nodes
}  


# codenvy backup  

```shell  
codenvy backup [directory]\
```
Creates an archive of the user and project data. If you do not specify a directory, the backup will be saved in `~/.codenvy/backups/`.  Backing up data will trigger a maintenance window.
# codenvy config  

```shell  
codenvy config [options] [property] [value]

Arguments:
property - Codenvy property name
value    - Codenvy property value

Options:
--hostname=<hostname>
--help\
```
Displays the properties of the system. that can be used to configure the Codenvy system. Run `codenvy config` with no options to display all known properties. You can provide a `[property]` to display the value of a single property. If you also provide `[value]` the property will be updated and puppet will be instructed to apply that change system-wide.
#### Changing DNS of Machine Nodes
When DNS of a Codenvy installation is changed, a system administrator must manually update DNS names of machine nodes, if any, un-register these nodes, and then register again with new DNS names:\n\n`remove-node node<number>.<old_codenvy_url>`\n`add-node node<number>.<new_codenvy_url>`  


# codenvy download  

```shell  
codenvy download [options] [<artifact>] [<version>]

Options:
--list-local
--list-remote\
```
Downloads new Codenvy binaries and patches from Codenvy.com. Running the command with no parameters will download the latest stable version of Codenvy.  `[artifact]` and `[version]` let you specify a specific asset and version. The `--list-local` flag shows which artifacts have already been downloaded.  The `--list-remote` flag shows the versions that are currently available for download from Codenvy's repos.
```shell  
# See what you have installed locally
codenvy download --list-local

# See what is available on Codenvy's servers for all artifacts
codenvy download --list-remote

# Download new binary for Codenvy 4.3.0-RC1-SNAPSHOT'
codenvy download codenvy 4.3.0-RC1-SNAPSHOT\
```

# codenvy install  

```shell  
codenvy install [options]

Options:
--binaries=path/to/zip <artifact> <version>
--list\
```
Installs a new Codenvy binary, plug-in or patch that has been downloaded and saved locally. Use `--list` to display the artifacts that have already been installed.  The `--binaries` option lets you set an absolute path to the artifact to install.  For example:
```shell  
codenvy install --binaries=/home/user/tomcat_aio.zip codenvy 3.10.3.2\
```

# codenvy login  

```text  
codenvy login [options] [username] [password]

OPTIONS:
--url=http://your.hostname
--help - displays this help message\
```

# codenvy password  

```shell  
codenvy password <current> <new>\
```
Modifies the current system administration password.
# codenvy remove-node  

```shell  
codenvy remove-node node[n].<hostname>\
```
Removes a machine node from the Codenvy cluster. If you have active machines on the node, those machines will be migrated off to another node.
# codenvy restore  

```shell  
codenvy restore <backup-file>\
```
Restores Codenvy with the projects and user data saved from a backup. The archive must be restored into a Codenvy that had the same version as the backup. Restoring data will trigger a maintenance window.

# codenvy version  

```shell  
codenvy version\
```
Tells you the currently installed versions of all installed artifacts and also displays the latest stable and beta versions available for download.
```json  
{
  "artifact" : "codenvy\n  "version" : "3.13.1-SNAPSHOT\n  "availableVersion" : {
    "stable" : "3.13.3\n    "unstable" : "3.13.7"
 },
 "status" : "There is a new stable version of Codenvy available. Run im-download 3.13.3."
}
```
