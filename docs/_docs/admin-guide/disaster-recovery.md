---
title: Disaster Recovery
excerpt: ""
layout: docs
overview: true
permalink: /docs/disaster-recovery/
---
Codenvy is not designed to be a "5-9s" system, however there are steps that can be taken by an operations team responsible for Codenvy to ensure a quick recovery from any crashes.

A secondary Codenvy system should be installed and kept at the same version level as the primary system. On a nightly or more frequent basis the Codenvy data store, Docker images and configuration can be transferred to the secondary system.

In the event of a failure of the primary, the secondary system can be powered on and traffic re-routed to it. Users accounts and workspaces will appear in the state they were in as of the last data transfer.  

# Initial Setup
## Create the Secondary System
Install Codenvy on a secondary system taking care to ensure that the version matches the primary system, remember to include the license file in this system. The secondary system should have the same number and size of nodes as the primary system.

## Transfer Data from Primary System
### On the primary system's master node:
1. Execute [codenvy backup](https://codenvy.readme.io/docs/cli#codenvy-backup).
2. Run `docker images` to get a list of all the images used in Codenvy.
3. Run `docker save` for each of the listed images to create a TAR of each image for transfer.
4. In the `/etc/puppet/manifests/nodes/codenvy/` directory copy the `codenvy.pp` file to a location where it is ready to transfer.

### On the secondary system's master node:
1. Execute [codenvy restore](https://codenvy.readme.io/docs/cli#codenvy-restore).
2. Run `docker load` against each of the TARs generated from the primary system.
3. Replace the `codenvy.pp` file at `/etc/puppet/manifests/nodes/codenvy/` with the version copied from the primary system.
4. Restart the Codenvy system.

## Setup Integrations
Any integrations that are used in the system (like LDAP, JIRA and others) should be configured identically on the secondary system.

## Setup Network Routing
Codenvy requires a DNS entry. In the event of a failure traffic will need to be re-routed from the primary to secondary systems. There are a number of ways to accomplish this - consult with your networking team to determine which is most appropriate for your environment.

## Test the Secondary System
Log into the secondary system and ensure that it works as expected, including any integrations. The tests should include logging in and instantiating a workspace at minimum. Once everything checks out you can leave the system idle (hot standby) or power it down (cold standby).

## Encourage Developers to Commit
The source of truth for code should be the source code repository. Developers should be encouraged to commit their changes nightly (at least) so that the code is up-to-date.

# On-Going Maintenance
## Version Updates
Each time the primary system is updated the secondary system should be updated as well.  Test both systems after update to confirm that they are functioning correctly.

## Adding / Removing Nodes
Each time the primary system nodes change (new nodes are added, existing are removed, or node resources are significantly changed) the same changes should be made to the secondary nodes.

## Nightly Data Transfers
On a periodic basis (we suggest nightly) the data transfer steps below should be executed. These can be scripted.  This is best done off-hours.

### On the primary system's master node:
1. Execute [codenvy backup](https://codenvy.readme.io/docs/cli#codenvy-backup).
2. Run `docker images` to get a list of all the images used in Codenvy.
3. Run `docker save` for each of the listed images to create a TAR of each image for transfer.
4. In the `/etc/puppet/manifests/nodes/codenvy/` directory copy the `codenvy.pp` file to a location where it is ready to transfer.

### On the secondary system's master node:
1. Execute [codenvy restore](https://codenvy.readme.io/docs/cli#codenvy-restore).
2. Run `docker load` against each of the TARs generated from the primary system.
3. Replace the `codenvy.pp` file at `/etc/puppet/manifests/nodes/codenvy/` with the version copied from the primary system.
4. Restart the Codenvy system.

# Triggering Failover
