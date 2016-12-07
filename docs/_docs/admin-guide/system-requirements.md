---
title: System Requirements
excerpt: "Codenvy installs on Linux, Mac and Windows with Docker."
layout: docs
overview: true
permalink: /docs/system-requirements/
---
Codenvy installs on Linux, Mac and Windows.

## Hardware
* 2 cores
* 3GB RAM
* 3GB disk space

Codenvy requires 2 GB storage and 4 GB RAM for internal services. The RAM, CPU and storage resources required for your users' workspaces are additive. Codenvy's Docker images consume ~900MB of disk and the Docker images for your workspace templates can each range from 5MB up to 1.5GB. Codenvy and its dependent core containers will consume about 500MB of RAM, and your running workspaces will each require at least 250MB RAM, depending upon user requirements and complexity of the workspace code and intellisense.

Boot2Docker, docker-machine, Docker for Windows, and Docker for Mac are all Docker variations that launch VMs with Docker running in the VM with access to Docker from your host. We recommend increasing your default VM size to at least 4GB. Each of these technologies have different ways to allow host folder mounting into the VM. Please enable this for your OS so that Codenvy data is persisted on your host disk.

## Software
* Docker 11.1+

The Codenvy CLI - a Docker image - manages the other Docker images and supporting utilities that Codenvy uses during its configuration or operations phases. The CLI also provides utilities for downloading an offline bundle to run Codenvy while disconnected from the network.

Given the nature of the development and release cycle it is important that you have the latest version of docker installed because any issue that you encounter might have already been fixed with a newer docker release.

Install the most recent version of the Docker Engine for your platform using the [official Docker releases](http://docs.docker.com/engine/installation/), including support for Mac and Windows!  If you are on Linux, you can also install using:
```bash
wget -qO- https://get.docker.com/ | sh
```

Sometimes Fedora and RHEL/CentOS users will encounter issues with SElinux. Try disabling selinux with `setenforce 0` and check if resolves the issue. If using the latest docker version and/or disabling selinux does not fix the issue then please file a issue request on the [issues](https://github.com/codenvy/codenvy/issues) page. If you are a licensed customer of Codenvy, you can get prioritized support with support@codenvy.com.


## Resources
You will need at least 8GB RAM and 16GB storage to run Codenvy. See the [sizing guide](https://codenvy.readme.io/docs/installation#sizing) before you install to ensure you’ll have sufficient resources for the number of developers and size of workspaces you’ll use.

## Hostnames
Codenvy must be accessed over DNS hostnames. You must configure your clients with a [hosts rule or setup a network-wide DNS entry](http://codenvy.readme.io/docs/networking). The default installation configures Codenvy with `http://codenvy.onprem/` as the initial hostname. You can [change Codenvy's hostname](http://codenvy.readme.io/docs/networking#change-hostname) at any time.

## Ports
Check that the [ports required for Codenvy](http://codenvy.readme.io/docs/architecture#single-node-installation) are opened.

## Proxies
You will need the HTTP and HTTPS proxy information in order to install Codenvy. This information is used in multiple places. First, to access our initial bootstrap script, which is hosted on Codenvy's servers.  Second, our installation manager configures itself with a proxy to be able to download Codenvy binaries and patches. Third, the system has to download Codenvy dependencies like Puppet and will use the proxy information for this. Fourth, Codenvy's internal Docker configuration will use a proxy to reach DockerHub. Fifth, the workspaces created by users need to have proxy information.

For Codenvy to work with a proxy you need to:
1. Add your proxy information to the [install CURL command](http://codenvy.readme.io/docs/installation#install).
2. [Configure Codenvy](http://codenvy.readme.io/docs/installation#section-use-codenvy-behind-a-proxy) so workspaces can be loaded properly.

Post-installation the administrator can alter the configuration to disallow workspaces from using a proxy.

## Internet Connection
The installer requires Internet connection to download packages and their dependencies that both installer and Codenvy use.