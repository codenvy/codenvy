---
tag: [ "codenvy" ]
title: Managing
excerpt: ""
layout: docs
permalink: /:categories/managing/
---

**Applies To**: Codenvy on-premises installs.

---
# Scaling
Codenvy workspaces can run on different physical nodes managed by Docker Swarm. This is an essential part of managing large development teams, as workspaces are both RAM and CPU intensive operations, and developers do not like to share their computing power. You will want to allocate enough nodes and resources to handle the number of concurrently *running* workspaces, each of which will have its own RAM and CPU requirements.

Codenvy requires a Docker overlay network to exist for our workspace nodes. An overlay network is a network that spans across the various nodes that allows Docker containers to simplify how they communicate with one another. This is mandatory for Codenvy since your workspaces can themselves be composed of multiple containers (such as defined by Docker Compose). If a single workspace has multiple runtimes, we can deploy those runtimes on different physical nodes. An overlay network allows those containers to have a common nework so that they can communicate with each other using container names, without each container having to understand the location of the other.

Overlay networks require a distributed key-value store. We embed Consul, a key-value storage implementation as part of the Codenvy master node. We currently only support adding Linux nodes into an overlay network.

The default network in Docker is a "bridge" network. If you know that your users will only ever have single container workspaces (this would be unusual and rare) and you can scale your system by using a larger single node, then bridge network can be used for production systems.

## Scaling With Overlay Network (Linux Only)
1: Collect the IP address of Codenvy `CODENVY-IP` and the network interface of the new workspace node `WS-IF` to be used in other configuration steps:

```shell
# Codenvy IP is either set by you to CODENVY_HOST or auto-discovered with:
docker run --net=host eclipse/che-ip:nightly

# Get the network interface for your ws node, typically 'eth1' or 'eth0':
ifconfig
```

2: On the Codenvy master node, start Consul: `docker -H <CODENVY-IP>:2376 run -d -p 8500:8500 -h consul progrium/consul -server -bootstrap`

3: On each workspace node, [configure and restart Docker](https://docs.docker.com/engine/admin/) with three new options: `--cluster-store=consul://<CODENVY-IP>:8500`, `--cluster-advertise=<WS-IF>:2376`, and `--engine-insecure-registry=<CODENVY-IP>:5000`. The first parameter tells Docker where the key-value store is located. The second parameter tells Docker how to link its workspace node to the key-value storage broadcast. The third parameter opens Docker to communicate on Codenvy's swarm cluster. And the fourth parameter allows the Docker daemon to push snapshots to Codenvy's internal registry. If you are running Codenvy behind a proxy, each workspace node Docker daemon should get the same proxy configuration that you placed on the master node. If you would like your Codenvy master node to also host workspaces, you can add these parameters to your master Docker daemon as well.

4: Verify that Docker is running properly. Docker will not start if it is not able to connect to the key-value storage. Run a simple `docker run hello-world` to verify Docker is happy. Each workspace node that successfully runs this command is part of the overlay network.

5: On the Codenvy master node, modify `codenvy.env` to uncomment or add:
```json
# Comma-separated list of IP addresses for each workspace node
# The ports must match the `--cluster-advertise` port added to Docker daemons
CODENVY_SWARM_NODES=<WS-IP>:2376,<WS2-IP>:2376,<WSn-IP>:2376
```

6: Restart Codenvy with `codenvy/cli restart`.

## Simulated Scaling
You can simulate what it is like to scale Codenvy with different nodes by launching Codenvy and its various cluster nodes within VMs using `docker-machine`, a utility that ships with Docker. Docker machine is a way to launch VMs that have Docker pre-installed in the VM using boot2docker. Docker machine uses different "drivers", such as HyperV or VirtualBox as the underlying hypervisor engine to launch the VMs. By lauching a set of VMs with different IP addresses, you can then simulate using Codenvy's Docker commands to start a main system and then having the other nodes add themselves to the cluster.

This simulated scaling can be used for production, but it is generally discouraged because you would be running Docker in VMs that are on a host, and you are just taking on some extra I/O overhead that may not generally be necessary.  However, this simulated-based approach gives good pointers on configuration of a distributed, cluster-based system if you were to use VMs-only.

As an example, the following sequence launches a 3-node cluster of Codenvy using Docker machine with a VirtualBox hypervisor. In this example, we launch 4 VMs: a Codenvy node, 2 additional workspace nodes, and a node to handle key-value storage. The key-value storage node is typically not part of the scaling configuration. However, Codenvy requires an "overlay" network, which is powered by a key-value storage provider such as Consule, etcd, or zookeeper. When running Codenvy on the host, we are able to setup an etcd key-value storage system automatically and associate the nodes with it. However, in a VM scale-out scenario, a dedicated key-value storage provider is needed. This particular example uses Consul key-value storage to setup the overlay network. 

One difference between running Codenvy using `docker-machine` is that each VM, including the Codenvy master, must have an additional option '--host=tcp://0.0.0.0:2375` added into the Docker daemon. This option is not part of the normal scaling configuration.

Start a VM with key-value storage and start Consul:
```shell
# Key-Value storage for overlay network
# Grab the IP address of this VM and use it in other commands where we have <KV-IP> 
docker-machine create -d virtualbox --engine-env DOCKER_TLS=no kv
docker -H <KV-IP:2376> run -d -p 8500:8500 -h consul progrium/consul -server -bootstrap
```

Start 3 VMs named 'codenvy', 'ws1', 'ws2'):
```shell
# Codenvy 
# Grab the IP address of this VM and use it in other commands where we have <CODENVY-IP>
docker-machine create -d virtualbox --engine-env DOCKER_TLS=no --virtualbox-memory "2048" \
                      --engine-opt="host=tcp://0.0.0.0:2375" codenvy

# Workspace Node 1
# 3GB RAM - enough to run a couple workspaces at the same time
docker-machine create -d virtualbox --engine-env DOCKER_TLS=no --virtualbox-memory "3000" \
                      --engine-opt="host=tcp://0.0.0.0:2375" \
                      --engine-opt="cluster-store=consul://<KV-IP>:8500" \
                      --engine-opt="cluster-advertise=eth1:2376" \
                      --engine-insecure-registry="<CODENVY-IP>:5000" ws1

# Workspace Node 2
docker-machine create -d virtualbox --engine-env DOCKER_TLS=no --virtualbox-memory "3000" \
                      --engine-opt="host=tcp://0.0.0.0:2375" \
                      --engine-opt="cluster-store=consul://<KV-IP>:8500" \
                      --engine-opt="cluster-advertise=eth1:2376" \
                      --engine-insecure-registry="<CODENVY-IP>:5000" ws2
```

Connect to the Codenvy VM and start Codenvy:
```shell
# SSH into the VM
docker-machine ssh codenvy

# Initialize a Codenvy installation
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock \
           -v /home/docker/.codenvy:/data codenvy/cli:nightly init

# Setup Codenvy's configuration file to have the IP addresses of each workspace node
sudo sed -i "s/^#CODENVY_WORKSPACE_AUTO_SNAPSHOT=true.*/CODENVY_WORKSPACE_AUTO_SNAPSHOT=true/g" \
            ~/.codenvy/codenvy.env
sudo sed -i "s/^CODENVY_SWARM_NODES=.*/CODENVY_SWARM_NODES=<WS1-IP>:2376,<WS2-IP>:2376/g" \
            ~/.codenvy/codenvy.env

# Start Codenvy with this configuration
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock \
           -v /home/docker/.codenvy:/data codenvy/cli:nightly start

# You can then access Codenvy at http://<CODENVY-IP>
```

# Upgrading
Upgrading Codenvy is done by downloading a `codenvy/cli:<version>` that is newer than the version you currently have installed. You can run `codenvy version` to see the list of available versions that you can upgrade to.

For example, if you have 5.0.0-M2 installed and want to upgrade to 5.0.0-M8, then:
```shell
# Get the new version of Codenvy
docker pull codenvy/cli:5.0.0-M8

# You now have two codenvy/cli images (one for each version)
# Perform an upgrade - use the new image to upgrade old installation
docker run <volume-mounts> codenvy/cli:5.0.0-M8 upgrade
```

The upgrade command has numerous checks to prevent you from upgrading Codenvy if the new image and the old version are not compatible. In order for the upgrade procedure to advance, the CLI image must be newer that the version in `/instance/codenvy.ver`.

The upgrade process:
1. Performs a version compatibility check
2. Downloads new Docker images that are needed to run the new version of Codenvy
3. Stops Codenvy if it is currently running
4. Triggers a maintenance window
5. Backs up your installation
6. Initializes the new version
7. Starts Codenvy

# Backup
You can run `codenvy backup` to create a copy of the relevant configuration information, user data, projects, and workspaces. We do not save workspace snapshots as part of a routine backup exercise. You can run `codenvy restore` to recover Codenvy from a particular backup snapshot. The backup is saved as a TAR file that you can keep in your records.

## Microsoft Windows and NTFS
Due to differences in file system types between NTFS and what is commonly used in the Linux world, there is no convenient way to directly host mount Postgres database data from within the container onto your host. We store your database data in a Docker named volume inside your boot2docker or Docker for Windows VM. Your data is persisted but if the underlying VM is destroyed, then the data will be lost.

However, when you do a `codenvy backup`, we do copy the Postgres data from the container's volume to your host drive, and make it available as part of a `codenvy restore` function. The difference is that if you are browsing your `/instance` folder, you will not see the database data on Windows.

# Migration
It is possible to migrate your configuration and user data from a puppet-based installation of Codenvy (5.0.0-M8 and earlier) to the Dockerized version of Codenvy. Please contact our support team for instructions.

# Disaster Recovery
You can run Codenvy with hot-standy nodes, so that in the event of a failure of one Codenvy, you can perform a switchover to another standby system.

A secondary Codenvy system should be installed and kept at the same version level as the primary system. On a nightly or more frequent basis the Codenvy data store, Docker images and configuration can be transferred to the secondary system.

In the event of a failure of the primary, the secondary system can be powered on and traffic re-routed to it. Users accounts and workspaces will appear in the state they were in as of the last data transfer.  

## Initial Setup
### Create the Secondary System
Install Codenvy on a secondary system taking care to ensure:
- Version matches the primary system
- License file is added to the secondary system.
- Number of nodes and their resource capacity is the same as the primary system.
- Source code repositories, artifact repositories and Docker registries are accessible from the secondary system.

### Transfer Data
1. Execute `codenvy-backup` on the primary system to get a copy of the `/instance` folder and the `codevy.env`.
2. Execute `codenvy-restore` on the secondary system.

### Setup Integrations
Any integrations that are used with Codenvy must be setup on both primary and secondary systems (e.g. LDAP, JIRA and others).

### Setup Network Routing
Codenvy requires a DNS entry. In the event of a failure traffic will need to be re-routed from the primary to secondary systems. There are a number of ways to accomplish this - consult with your networking team to determine which is most appropriate for your environment.

### Test the Secondary System
Log into the newly created secondary system and ensure that it works as expected, including any integrations. The tests should include logging in, instantiating a workspace using your custom images and snapshotting workspaces (if appropriate). Exercise any integrations at this time to ensure they function as expected.

Once everything checks out you can leave the system idle (hot standby) or power it down (cold standby).

## Encourage Developers to Commit
Source code should be committed to the code repository frequently - this will facilitate the smooth transition from primary to secondary systems.

## On-Going Maintenance
### System Updates and Changes
Each time the primary system is updated to a new Codenvy version the secondary system should be updated as well.  Test both systems after update to confirm that they are functioning correctly.

If new nodes are added or removed, or if existing nodes are resized, a matching change should be made to the secondary system.

## Triggering Failover
If there is a failure with the primary system, log into the secondary system to ensure that everything is working as expected. Then re-route DNS to the secondary nodes. The secondary must have the same DNS name as the primary after switchover to ensure all functions operate correctly.
