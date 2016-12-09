---
title: Installation
excerpt: "Install Codenvy in a public cloud or on your own servers."
layout: docs
overview: true
permalink: /docs/installation/
---
# Installation Types  
There are four ways to install Codenvy:
* [Standard installation](doc:installation) on a single server
* With [Vagrant](http://codenvy.readme.io/v4.0/docs/installation-other#vagrant)
* A [multi-node configuration](http://codenvy.readme.io/v4.0/docs/installation-other#multi-node)
* From the [Microsoft Azure Marketplace](http://codenvy.readme.io/v4.0/docs/installation-other#microsoft-azure-marketplace)
# Pre-Install Checklist  
To ensure a smooth install the following must be checked before installing.

## Operating System
Core Codenvy requires an unmodified CentOS 7.1+ or RHEL 7.1+ operating system with access to the internet. If you are using RHEL, you must enable some additional repositories (which requires a subscription):
```shell  
subscription-manager register --username <LOGIN> --password <PASS> --auto-attach
subscription-manager repos --enable=rhel-7-server-optional-rpms
subscription-manager repos --enable=rhel-7-server-extras-rpms\
```
## Install User and Sudoer Rights
The Linux user that runs the installation command must have sudo rights that permits running services without a password entry. We also need write permissions for the install directory. The user shouldn't be called `codenvy`  - we create a `codenvy` and `codenvy-im` user as part of the install.

To check this:
```shell  
# Check if user has sudo rights
sudo -k -n true

# Check if user is in the sudoers file
sudo grep "^#includedir.*/etc/sudoers.d" /etc/sudoers```
# There should be "#includedir ... /etc/sudoers.d" row listed
```
## Password Authenticated VMs
For authenticated VMs, like those hosted on EC2 and Microsoft Azure, you may need to configure passwordless sudo rights for the user account that you have created. Do not use `codenvy` as this user account - we create a `codenvy` and `codenvy-im` user as part of the install.
```shell  
# Add the following to /etc/sudoers.d/waagen immediately after the existing entry
ALL=(ALL) NOPASSWD:ALL

# Change permissions
chown -R user:user /home/user

# Close SSH session and re-login\
```
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
# Sizing  
## Minimum Installation
The Codenvy services running on a server require 2 GB storage and 4 GB RAM for their internal overhead. This applies to both single server, single server with multiple machine servers, and distributed server implementations. The RAM, CPU and storage resources to support your users are additive.

## Concurrent Workspaces
You need to have machine RAM to support the number of concurrent workspaces open by users within your system. A single user may have multiple workspaces open, but generally use a single workspace. The default workspace size is 1GB RAM, but this can be changed by users.  Also, within a single workspace, a user is able to launch additional environments for their projects, which consume additional RAM, though this is uncommon.

For example, at Codenvy, we regularly have 75 concurrently running workspaces, each sized at 16 GB RAM, for a total expectation of 1200 GB of RAM.  Our machine nodes have  128 GB RAM per node and we routinely run 10-14 machine nodes to support everyone.

Note that compilation events are usually CPU-heavy and most compilation activities initiated by developers are sent to a single CPU.  With quad-core machine nodes, we end up with capacity to support 40-52 concurrent builds in workspaces so that there is no thrashing or blocking.  Your team may need more.

## Project Storage
You will need to gauge how much storage you need for projects. A good measuring stick to use is the size of your version control system, as you can expect that Codenvy over time will track similar consumption. Codenvy makes it easy for users to create new workspaces or clone existing ones, so Codenvy will be responsible for managing clones of source trees.

| Resource   | Where to Mount Additional Disk Space   
| --- | --- 
| Project Storage   | User Profile Storage   
| User Account & LDAP Storage   | `/home`\n`/var/lib/docker`   
| `/var/lib/mongo`   | `/var/lib/ldap`   

Most of your storage will be allocated for projects. In the production environment that manages Codenvy.com with >250K users, we have 1 GB for LDAP and 4 GB for user profiles.  Project storage is in the terabytes.
# Install  

```shell  
# Run the following commands as root
bash <(curl -L -s https://start.codenvy.com/install-codenvy) [OPTIONS]

# If behind a proxy
bash <(curl -L -s --proxy <proxy> https://start.codenvy.com/install-codenvy) [OPTIONS]

OPTIONS:
--hostname=<hostname>
--suppress
--silent
--im-cli
--version=<version>
--multi
--license=accept
--config=<path-to-config-file>

# This option controls where artifacts downloaded during install are stored.
# During runtime all processes and storage are run under /home/codenvy
--install-directory=<directory>

# This option is used by the docker for networking.
--advertise-network-interface=<interface>

# The '-proxy' curl option allows the 'install-codenvy' script to download.
# These options are passed to the 'install-codenvy' script to let it use a proxy.
--http-proxy-for-installation=http://<user>:<password>@<host>:<port>
--https-proxy-for-installation=https://<user>:<password>@<host>:<port>

# You can provide a semi-colon separated list of domains that should not be proxied
--no-proxy-for-installation=<host>

# If not provided, inherits from -for-installation.
# Sets the proxy information for the Codenvy servers.
--http-proxy-for-codenvy=http://<user>:<password>@<host>:<port>
--https-proxy-for-codenvy=https://<user>:<password>@<host>:<port>
--no-proxy-for-codenvy=<host>

# If not provided, inherits from '-for-installation'.
# These options will be passed into Codenvy configuration to tell Codenvy workspaces
# how they should use a proxy. 
--http-proxy-for-codenvy-workspaces=http://<user>:<password>@<host>:<port>
--https-proxy-for-codenvy-workspaces=https://<user>:<password>@<host>:<port>
--no-proxy-for-codenvy-workspaces=<host>

# If not provided, inherits from '-for-installation'.
# These options will be passed into Codenvy configuration to tell Codenvy 
# how its Docker daemon should be configured with a proxy. 
--http-proxy-for-docker-daemon=http://<user>:<password>@<host>:<port>
--https-proxy-for-docker-daemon=https://<user>:<password>@<host>:<port>
--no-proxy-for-docker-daemon=<host>

# If you have an internal Docker registry which is mirroring and caching Docker Hub
# you can have the Codenvy Docker daemon use this local mirror for grabbing global
# Docker repository images.
--docker-registry-mirror=<url-to-registry>\
```
Codenvy will be installed with the configuration contained in `codenvy.properties`. If you do not have a `codenvy.properties` file locally, a default will be downloaded and used by the installer. You can download a [properties template for your version](https://github.com/codenvy/codenvy/tree/master/cdec/installation-manager-resources/src/main/resources/codenvy-properties) to customize the configuration before running the installer. 

The single server version of Codenvy is installed unless `--multi` is specified.

| Option>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   | Description   
| --- | --- 
| `--hostname`   | Sets the access URL to be `http://hostname`. Default is `http://codenvy.onprem`.   
| `--suppress`   | `--systemAdminName`   
| `--systemAdminPassword`   | `--version=<version>`   
| `--multi`   | Suppresses interactive prompts and configures Codenvy using the values in  `codenvy.properties` file. Downloads this file from Codenvy servers if not locally available. This option does not accept the Codenvy license agreement.  Use `--license=accept` for that.   
| Sets the default user name for the admin account. Default is `admin`.   | Sets the defaut password for the admin account. Default is `password`. This can be changed after installation.   
| Installs a specific version of Codenvy. If not provided, the installer will install the latest stable version available at Codenvy.com. You can get a list of available versions that we host before doing an installation using `curl https://codenvy.com/update/repository/updates/codenvy`.   | Installs Codenvy with internal services distributed across different nodes each with their own hostname. This configuration is only for systems that need redundancy on all services, which is not common. Please see [Installation - Other](doc:installation-other) for specifics on how this is configured.   
| `--im-cli`   | Only installs the installation manager service and the accompanying CLI. Takes about 3 minutes. You can then use the CLI to schedule installations or install a specific version from a previously downloaded binary.   
| `--http-proxy-for-installation=`\n`<proto>://<user>:<password>@<host>:<port>`\n\n`--https-proxy-for-installation=`\n`<proto>://<user>:<password>@<host>:<port>`\n\n`--no-proxy-for-installation=<host>`   | These parameters specify the proxy configuration that will allow Codenvy to install itself behind a proxy. There are multiple configuration files for yum, curl, and Puppet that are updated to allow Codenvy's bootstrap script, installation manager, and Puppet to reach external repositories. The `--no-proxy` option is available as a way to list domains that should not be proxied.\n\nThese values act as defaults for all of the other proxy settings if they are not provided. Providing the values for other properties will act as overrides to these settings.   
| `--silent`   | Performs an installation with minimal output. Output includes basic pre-flight checks and major installation status.   
| `--license=accept`   | Accepts the Codenvy terms of service license agreement available at `http://codenvy.com/legal`. When used with `--suppress`, you can perform a completely automated installation.   
| `--http-proxy-for-codenvy-workspaces=` \n`<proto>://<user>:<password>@<host>:<port>`\n\n`--https-proxy-for-codenvy-workspaces=` \n`<proto>://<user>:<password>@<host>:<port>`\n\n`--no-proxy-for-codenvy-workspaces=<host>`   | Specify how Codenvy workspaces created by Codenvy will use a proxy. Each Codenvy workspace is its own runtime. Setting these values will set the following inside the workspace:\n1. export http_proxy.\n2. export https_proxy.\n3. export no_proxy.   
| `--http-proxy-for-docker-daemon=` \n`<proto>://<user>:<password>@<host>:<port>`\n\n`--https-proxy-for-docker-daemon=` \n`<proto>://<user>:<password>@<host>:<port>`\n\n`--no-proxy-for-docker-daemon=<host>`   | Configures the Docker daemon running on each node managed by Codenvy to use a proxy. The Docker daemon is used to download Docker images from DockerHub. We configure `systemd` to set Docker proxy. [See more](https://docs.docker.com/engine/admin/systemd/).   
| `--docker-registry-mirror=<url>`   | If you have a private Docker registry configured to cache images from Docker Hub, it is setup as a registry mirror. This parameter tells Codenvy's Docker daemon to use your mirror instead of routing to DockerHub directly.   
| `--config=<path-to-config-file>`   | Passes in a `codenvy.properties` file which contains a configuration that the installer will use when installing Codenvy. The file can be hosted on a server and provided with a URL. The file will be retrieved using curl and honor any system proxy.   
| `--install-directory=<directory>`   | Select the directory where Codenvy properties templates, Codenvy installation manager, and any Codenvy binaries are deployed. During installation, we create `/home/codenvy` and `/home/codenvy-im` user accounts and directories which will house certain assets.   
| `--http-proxy-for-codenvy=` \n`<proto>://<user>:<password>@<host>:<port>`\n\n`--https-proxy-for-codenvy=` \n`<proto>://<user>:<password>@<host>:<port>`\n\n`--no-proxy-for-codenvy=<host>`   | Specifies how the various Codenvy servers will use proxies while Codenvy is running. The properties set include:\n1. export http_proxy for `codenvy` and `codenvy-im` user.\n2. export https_proxy for `codenvy` and `codenvy-im` user.\n3. export no_proxy for `codenvy` and `codenvy-im` user.\n4. Add proxy configuration settings to the `JAVA_OPTS` environment variable so Codenvy's Tomcats inherit the values during boot.   
| `--advertise-network-interface`   | Network interface used by the docker for networking.   

Your installation should be smooth and error free. But if something were to go wrong, please walk through the [Installation - Troubleshooting](doc:installation-troubleshooting) for tips and tactics that can help you (and us) diagnose the problem.
# Configure  
