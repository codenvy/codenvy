---
title: Managing
excerpt: ""
layout: docs
permalink: /docs/admin-guide/managing/
---
## Scaling
Codenvy workspaces can run on different physical nodes that are part of a Codenvy cluster managed by Docker Swarm. This is an essential part of managing large development teams, as workspaces are both RAM and CPU intensive operations, and developers do not like to share their computing power when they have a compilation that they want done. So you will want to allocate enough physical nodes to smartly handle the right number of concurrently *running* workspaces, each of which will have a RAM block.

Each Codenvy instance generates a configuration on how to add nodes into the cluster. Run `codenvy add-node` for instructions of what to run on each physical node that should be added into the cluster. The physical node runs a script which installs some software from the Codenvy master node, configures its Docker daemon, and then registers itself as a member of the Codenvy cluster.

You can remove nodes with `codenvy remove-node <ip>`.

## Upgrading
Upgrading Codenvy is done by downloading a `codenvy/cli:<version>` that is newer than the version you currently have installed. For example, if you have 5.0.0-M2 installed and want to upgrade to 5.0.0-M8, then:
```
# Get the new version of Codenvy
docker pull codenvy/cli:5.0.0-M8

# You now have two codenvy/cli images (one for each version)
# Perform an upgrade - use the new image to upgrade old installation
docker run <volume-mounts> codenvy/cli:5.0.0-M8 upgrade
```

The upgrade command has numerous checks to prevent you from upgrading Codenvy if the new image and the old version are not compatible. In order for the upgrade procedure to advance, the CLI image must be newer that the version in `/instance/codenvy.ver`.

The upgrade process: a) performs a version compatibility check, b) downloads new Docker images that are needed to run the new version of Codenvy, c) stops Codenvy if it is currently running triggering a maintenance window, d) backs up your installation, e) initializes the new version, and f) starts Codenvy.

You can run `codenvy version` to see the list of available versions that you can upgrade to.

## Backup (Backup)
You can run `codenvy backup` to create a copy of the relevant configuration information, user data, projects, and workspaces. We do not save workspace snapshots as part of a routine backup exercise. You can run `codenvy restore` to recover Codenvy from a particular backup snapshot. The backup is saved as a TAR file that you can keep in your records.

### Microsoft Windows and NTFS
Due to differences in file system types between NTFS and what is commonly used in the Linux world, there is no convenient way to directly host mount Postgres database data from within the container onto your host. We store your database data in a Docker named volume inside your boot2docker or Docker for Windows VM. Your data is persisted permanently. If the underlying VM is destroyed, then the data will be lost.

However, when you do a `codenvy backup`, we do copy the Postgres data from the container's volume to your host drive, and make it available as part of a `codenvy restore` function. The difference is that if you are browsing your `/instance` folder, you will not see the database data on Windows.

## Migration
We currently do not support migrating from the puppet-based configuration of Codenvy to the Dockerized version. We do have a manual process which can be followed to move data between the puppet and Dockerized versions. The versions must be identical. Contact us to let our support team perform this migration for you.

## Disaster Recovery
We maintain a disaster recovery [policy and best practices](http://codenvy.readme.io/v5.0/docs/disaster-recovery).
