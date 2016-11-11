# Codenvy Installation and Operation
Codenvy makes cloud workspaces for develoment teams. Install Codenvy as a set of Docker containers.

- [Beta](#beta)
- [Getting Help](#getting-help)
- [Getting Started](#getting-started)
- [System Requirements](#system-requirements)
    + [Hardware](#hardware)
    + [Software](#software)
    + [Workspaces](#workspaces)
    + [Sizing](#sizing)
- [Installation](#installation)
    + [Linux:](#linux)
    + [Mac:](#mac)
    + [Windows:](#windows)
    + [Verification:](#verification)
    + [Proxies](#proxies)
    + [Offline Installation](#offline-installation)
- [Usage](#usage)
    + [Hosting](#hosting)
- [Uninstall](#uninstall)
- [Configuration](#configuration)
    + [Saving Configuration in Version Control](#saving-configuration-in-version-control)
    + [Logs and User Data](#logs-and-user-data)
    + [oAuth](#oauth)
    + [LDAP](#ldap)
    + [Development Mode](#development-mode)
    + [Licensing](#licensing)
    + [Hostname](#hostname)
    + [HTTP/S](#https)
    + [SMTP](#smtp)
    + [Workspace Limits](#workspace-limits)
    + [Private Docker Registries](#private-docker-registries)
- [Managing](#managing)
    + [Scaling](#scaling)
    + [Upgrading](#upgrading)
    + [Backup (Backup)](#backup-backup)
    + [Runbook](#runbook)
    + [Monitoring](#monitoring)
    + [Migration](#migration)
    + [Disaster Recovery](#disaster-recovery)
- [CLI Reference](#cli-reference)
- [Architecture](#architecture)
- [Team](#team)

## Beta
This packaging and deployment approach is relatively new. We do not yet consider this ready for production deployment of Codenvy. We hope to offer this as the primary production configuration by the end of 2016. Items to be added:

1. `codenvy upgrade` is not yet implemented. You can switch between versions, but we do not yet support automatic data migration inside of images. 

2. Networking overlay mode. If you are running a Codenvy cluster on different physical nodes and your users launch compose workspaces that themselves have multiple containers, there are cases where Swarm will place those different containers on different physical nodes. This is great for scalability. However, our default networking mode is `bridge`, which will prevent those workspace containers from seeing each other, and your users will scratch their heads. We are testing an `overlay` mode which configures `etcd` automatically that will let workspace containers see one another regardless of where Swarm places their operation.

3. HTTP/S. We are working to make configuration of SSL and HTTP/S a single line so that you can swap between configurations. The current version only supports HTTP.

4. Migrations. We do not yet support migrating an existing Codenvy installation that uses our Puppet-based infrastructure into a Dockerized infrastructure of the same version. Currently, Dockerized Codenvy installations need to be different installations apart from the puppetized infrastructure.

5. Boot2docker. We have not thoroughly tested certain configurations using boot2docker for Windows. You can expect issues if you do not set CODENVY_CONFIG or CODENVY_INSTANCE to %userprofile%.

6. If you `codenvy remove-node`, we trigger a system-wide restart. Your workspaces and users are not affected. This is a temporary limitation.

7. We do a single-node deployment of etcd, which is used as a distributed key-value store. If your users are creating workspaces that use Docker compose syntax, there are scenarios where separate containers for a single workspace will be scheduled onto different physical nodes. With our single node implementation of etcd, those containers will not be part of the same network and cannot communicate with one another. Your users will yell at you. The current work around is to manually configure etcd, zookeeper, or Consul on each physical node before you add it into the Codenvy cluster, and then activate "overlay" networking mode in `codenvy.env` file. Contact us for guidance on how to configure this. For GA we will provide a distributed key value store that does not have this limitation.

## Getting Help
If you run into an issue, please open a GitHub issue providing:
- the host distribution and release version
- output of the `docker version` command
- output of the `docker info` command
- the `codenvy <command>` you used to run Codenvy

If you are Codenvy customer, you can also file a ticket through our email support channel for a quicker response.

## Getting Started
1. Get Docker 1.11+, Docker Compose 1.8+.
2. Get the [Codenvy CLI](#installation).
3. [Start Codenvy](#usage).

## System Requirements
Codenvy installs on Linux, Mac and Windows. 

#### Hardware
* 2 cores
* 3GB RAM
* 3GB disk space

Codenvy requires 2 GB storage and 4 GB RAM for internal services. The RAM, CPU and storage resources required for your users' workspaces are additive. Codenvy's Docker images consume ~800MB of disk and the Docker images for your workspace templates can each range from 5MB up to 1.5GB. Codenvy and its dependent core containers will consume about 500MB of RAM, and your running workspaces will each require at least 250MB RAM, depending upon user requirements and complexity of the workspace code and intellisense.

Boot2Docker, docker-machine, Docker for Windows, and Docker for Mac are all Docker variations that launch VMs with Docker running in the VM with access to Docker from your host. We recommend increasing your default VM size to at least 4GB. Each of these technologies have different ways to allow host folder mounting into the VM. Please enable this for your OS so that Codenvy data is persisted on your host disk.

#### Software
* Docker 11.1+
* Docker Compose 1.8+. 
* Bash

The Codenvy CLI manages the Codenvy Docker images and supporting utilities that must be downloaded. The CLI also provides utilities for downloading an offline bundle to run Codenvy while disconnected from the network.

Docker for Mac and Windows have compose pre-installed. See: [Install Docker Compose on Linux](https://docs.docker.com/compose/install/). The Docker Toolbox for Windows installs [Git Bash for Windows](https://git-for-windows.github.io/), which is needed to run the CLI, a cross-platform set of bash scripts.

Given the nature of the development and release cycle it is important that you have the latest version of docker installed because any issue that you encounter might have already been fixed with a newer docker release.

Install the most recent version of the Docker Engine for your platform using the [official Docker releases](http://docs.docker.com/engine/installation/), including support for Mac and Windows!  If you are on Linux, you can also install using:
```bash
wget -qO- https://get.docker.com/ | sh
```

Sometimes Fedora and RHEL/CentOS users will encounter issues with SElinux. Try disabling selinux with `setenforce 0` and check if resolves the issue. If using the latest docker version and/or disabling selinux does not fix the issue then please file a issue request on the [issues](https://github.com/codenvy/codenvy/issues) page. If you are a licensed customer of Codenvy, you can get prioritized support with support@codenvy.com.

Install the most recent version of Docker Compose for your platform using the [official Docker releases](https://github.com/docker/compose/releases). With Windows and Mac, this comes with Docker Toolbox. If you are on Linux, you can also install using:
```bash
curl -L "https://github.com/docker/compose/releases/download/1.8.1/docker-compose-$(uname -s)-$(uname -m)" > /usr/local/bin/docker-compose
```
#### Workspaces
Currently, Codenvy's workspaces launch a tiny rsync-agent that allows the centralized Codenvy server to backup project source code from within each workspace to the central servers. When workspaces are shut off or restarted, the project files are automatically rsync'd back into the workspace. rsync runs at workspace start, stop, and on a scheduler. This allows us to preserve the integrity of your source code if the workspace's runtime containers were to have a failure during operation.

We install rsync into each user's workspace to run as a background service. In this version of Codenvy, your user workspaces require SSH and rsync in their base image. If you are connected to the Internet, we install rsync and SSH automatically. However, if you are doing an offline installation, then your workspace base images need to have this software included.

Some base images, like ubuntu, support this, but others like alpine, do not. If you create custom workspace recipes from Composefiles or Dockerfiles to run within Codenvy, these images must inherit from a base image that has rsync and SSH or you must ensure that these services are installed. If you do not have these services installed, the workspace will not start and provide an error to the user that may cause them to scratch their head.

In the non-container installation version of Codenvy, this requirement does not exist since we install these dependencies onto each host node that is added into the Codenvy cluster. We will be working to package up the rsync agent as a container that is deployed outside of your workspace's runtime. The container will have the dependencies and then this requirement will be removed.

#### Sizing
Codenvy's core services will run on a single node as a set of microservices. Workspaces will run on the core node and additional workspace nodes that you add into a Codenvy cluster run by Swarm. The number and size of these physical nodes is determined by a few factors.

You need to have enough RAM to support the number of concurrent *running* workspaces. A single user may have multiple running workspaces, but generally the common scenario is a user running a single workspace at a time. Workspace sizes are set by users when they create new workspaces, but you can define workspace limits in the configuration file that prevent RAM sprawl.

For sizing, determine how much RAM you want each user to consume at a time, and then estimate the peak concurrent utilization to determine how much system-wide RAM you will want. For example, internally at Codenvy, we regularly have 75 concurrently running workspaces, each sized at 16 GB RAM, for a total expectation of 1.2TB of RAM. Our machine nodes have 128 GB RAM per node and we routinely run 10-14 machine nodes to support our internal engineering efforts.

Compilation is CPU-heavy and most compilation events are queued to a single CPU. In the same Codenvy enterprise example, if our machine nodes are quad-core, we end up capacity to support 40-52 concurrent builds. If the total workspace CPU activity exceeds 40-52 events, then user performance begins to suffer due to thrashing and blocking.

The default configuration of workspaces is to auto-snapshot the workspace runtime to disk whenever it is stopped, whether by the user or through idle timeout. Many stack base images can grow to be >1GB, especially if you are installing complicated software inside of them, and thus their snapshots can be sizable as well. If you allow users to have many workspaces, even if they are stopped, each of those workspaces will have a snapshot on disk. You can apply a workspace cap on the total number of workspaces that can exist, which prevents users from having too many snapshots. The monitoring system also has parameters you can set to perform routine cleanup of old workspaces, where their snapshots are destroyed or removed from the user's system.

We have experitmented with adding 1000 physical nodes into a single physical cluster. You can use the `codenvy add-node` command which generates a utility for you to run on each node that should be added to the cluster. You can also run `codenvy remove-node` to automate the removal of the node from the cluster and the movement of any remaining workspaces onto another node. 

The additional physical nodes must have Docker pre-configured similar to how you have Docker configured on the master node, including any configurations that you add for proxies or an alternative key-value store like Consul. Codenvy generates an automated script that can be run on each new node which prepares the node by installing some dependencies, adding the Codenvy SSH key, and registering itself within the Codenvy cluster.

## Installation
Get the Codenvy CLI. The Codenvy images and supporting utilities are downloaded and maintained by the CLI. The CLI also provides utilities for downloading an offline bundle to run Codenvy while disconnected from the network.

#### Linux:
```
sudo curl -sL https://raw.githubusercontent.com/codenvy/codenvy/docker/codenvy.sh > /usr/local/bin/codenvy
sudo curl -sL https://raw.githubusercontent.com/codenvy/codenvy/docker/cli.sh > /usr/local/bin/cli.sh
chmod +x /usr/local/bin/codenvy
chmod +x /usr/local/bin/cli.sh
```

#### Mac:
```
curl -sL https://raw.githubusercontent.com/codenvy/codenvy/docker/codenvy.sh > /usr/local/bin/codenvy
curl -sL https://raw.githubusercontent.com/codenvy/codenvy/docker/cli.sh > /usr/local/bin/cli.sh
chmod +x /usr/local/bin/codenvy
chmod +x /usr/local/bin/cli.sh
```
The Moby VM that is provided with Docker for Mac is unavailable over the direct IP address that we auto-detect. You can create a loopback alias which will make communications flow from your host into Codenvy and back:
```
# Grabs the IP address of the Xhyve VM
export DOCKER_VM_IP=$(docker run --rm --net host alpine sh -c "ip a show eth0" | \
                    grep 'inet ' | cut -d/ -f1 | awk '{ print $2}')

# Create a loopback alias for the DOCKER_VM_IP
sudo ifconfig lo0 alias $DOCKER_VM_IP

# Add this to your ~/.bash_profile to have it activated in each shell window
```
#### Windows:
```
curl -sL https://raw.githubusercontent.com/codenvy/codenvy/docker/codenvy.sh > codenvy.sh
curl -sL https://raw.githubusercontent.com/codenvy/codenvy/docker/codenvy.bat > codenvy.bat
curl -sL https://raw.githubusercontent.com/codenvy/codenvy/docker/cli.sh > cli.sh
set PATH=<path-to-cli>;%PATH%
```

#### Verification:
You can verify that the CLI is working:
```
codenvy help
```
The CLI is self-updating. If you modify the `cli.sh` companion script or change your `CODENVY_VERSION` then an updated CLI will be downloaded. The CLI installs its core subsystems into `~/.codenvy/cli`.

If you run the CLI and you get issues (or no output), we do advanced logging and include all error messages in `~/.codenvy/cli/cli.log`. It will have more information to tell you what happened.

#### Proxies
You can install and operate behind a proxy. You will be operating a clustered system that is managed by Docker, and itself is managing a cluster of workspaces each with their own runtime(s). There are three proxy configurations:
1. Configuring Docker proxy access so that Codenvy can download images from DockerHub.
2. Configuring Codenvy's system containers so that internal services can proxy to the Internet.
3. Optionally, configuring workspace proxy settings to allow users within a workspace to proxy to the Internet.

Before starting Codenvy, configure [Docker's daemon for proxy access](https://docs.docker.com/engine/admin/systemd/#/http-proxy). If you plan to scale Codenvy with multiple host nodes, each host node must have its Docker daemon configured for proxy access.

Codenvy's system runs on Java, and the JVM requires proxy environment variables in our `JAVA_OPTS`. We use the JVM for the core Codenvy server and the workspace agents that run within each workspace. You must set the proxy parameters for these system properties `/CODENVY_INSTANCE/codenvy.env`. Please be mindful of the proxy URL formatting. Proxies are unforgiving if do not enter the URL perfectly, inclduing the protocol, port and whether they allow a trailing slash/.
```
CODENVY_HTTP_PROXY_FOR_CODENVY=http://myproxy.com:8001/
CODENVY_HTTPS_PROXY_FOR_CODENVY=http://myproxy.com:8001/
CODENVY_NO_PROXY_FOR_CODENVY=<ip-or-domains-that-do-not-require-proxy-access>
```

If you would like your users to have proxified access to the Internet from within their workspace, those workspace runtimes need to have proxy settings applied to their environment variables in their .bashrc or equivalent. Configuring these parameters will have Codenvy automatically configure new workspaces with the proper environment variables. 
```
# CODENVY_HTTP_PROXY_FOR_CODENVY_WORKSPACES=http://myproxy.com:8001/
# CODENVY_HTTPS_PROXY_FOR_CODENVY_WORKSPACES=http://myproxy.com:8001/
# CODENVY_NO_PROXY_FOR_CODENVY_WORKSPACES=<ip-or-domains-that-do-not-require-proxy-access>
```

A `NO_PROXY` variable is required if you use a fake local DNS. Java and other internal utilities will avoid accessing a proxy for internal communications when this value is set.

#### Offline Installation
We support the ability to install and run Codenvy while disconnected from the Internet. This is helpful for certain restricted environments, regulated datacenters, or offshore installations. 

##### Save Docker Images
While connected to the Internet and with access to DockerHub, download Codenvy's Docker images as a set of files with `codenvy offline`. Codenvy will download all dependent images and save them to `offline/*.tar` with each image saved as its own file. `CODENVY_VERSION` environment variable is used to determine which images to download unless you already have a Codenvy installation, then the value of `instance/codenvy.ver` will be used. There is about 1GB of data that will be saved.

##### Save Codenvy CLI
Save `codenvy.sh`, `cli.sh`, and if on Windows, `codenvy.bat`.

##### Save Codenvy Stacks
Out of the box, Codenvy has configured a few dozen stacks for popular programming languages and frameworks. These stacks use "recipes" which contain links to Docker images that are needed to create workspaces from these stacks. These workspace runtime images are not saved as part of `codenvy offline`. There are many of these images and they consume a lot of disk space. Most users do not require all of these stacks and most replace default stacks with custom stacks using their own Docker images. If you'd like to get the images that are associated with Codenvy's stacks:
```
docker save <codenvy-stack-image-name> > offline/<base-image-name>.tar
```
The list of images that Codenvy manages is sourced from Eclipse Che's [Dockerfiles repository](https://github.com/eclipse/che-dockerfiles/tree/master/recipes). Each folder is named the same way that our images are stored.  The `alpine_jdk8` folder represents the `codenvy/alpine_jdk8` Docker image, which you would save with `docker save codenvy/alpine_jdk8 > offline/alpine_jdk8.tar`.

##### Start Offline
Extract your files to an offline computer with Docker already configured. Install the CLI files to a directory on your path and ensure that they have execution permissions. Execute the CLI in the directory that has the `offline` sub-folder which contains your tar files. Then start Codenvy in `--offline` mode:
```
codenvy start --offline
```
When invoked with the `offline` parameter, the Codenvy CLI performs a preboot sequence, which loads all saved `offline/*.tar` images including any Codenvy stack images you saved. The preboot sequence takes place before any CLI configuration, which itself depends upon Docker. The `codenvy start`, `codenvy download`, and `codenvy init` commands support `--offline` mode which triggers this preboot seequence.

## Usage
`codenvy start`
This installs a Codenvy configuration, downloads Codenvy's Docker images, run pre-flight port checks, boot Codenvy's services, and run post-flight checks. You do not need root access to start Codenvy, unless your environment requires it for Docker operations. You will need write access to the current directory and to `~/.codenvy` where certain CLI and manifest information is stored.

A successful start will display:
```
INFO: (codenvy cli): Downloading cli-latest
INFO: (codenvy cli): Checking registry for version 'nightly' images
INFO: (codenvy config): Generating codenvy configuration...
INFO: (codenvy config): Customizing docker-compose for Windows
INFO: (codenvy start): Preflight checks
         port 80:  [OK]
         port 443: [OK]

INFO: (codenvy start): Starting containers...
INFO: (codenvy start): Server logs at "docker logs -f codenvy_codenvy_1"
INFO: (codenvy start): Server booting...
INFO: (codenvy start): Booted and reachable
INFO: (codenvy start): Ver: 5.0.0-M6-SNAPSHOT
INFO: (codenvy start): Use: http://10.0.75.2
INFO: (codenvy start): API: http://10.0.75.2/swagger
```
The administrative login is:
```
user: admin
pass: password
```

#### Hosting
If you are hosting Codenvy at a cloud service like DigitalOcean, set `CODENVY_HOST` to the server's IP address or its DNS. We use an internal utility, `eclipse/che-ip`, to determine the default value for `CODENVY_HOST`, which is your server's IP address. This works well on desktops, but usually fails on hosted servers.

## Uninstall
```
# Remove your Codevy configuration and destroy user projects and database
codenvy destroy

# Deletes Codenvy's images from your Docker registry
codenvy rmi

# Removes CLI logs, configuration and manifest data
rm -rf ~/.codenvy
```

## Configuration
Configuration is done with environment variables. Environment variables are stored in `CODENVY_CONFIG/codenvy.env`, a file that is generated during the `codenvy init` phase. If you rerun `codenvy init` in the same `CODENVY_CONFIG`, the process will abort unless you pass `--force` or `--pull`. You can have multiple `CODENVY_CONFIG` folders in order to keep profiles of configuration.

Each variable is documented with an explanation and usually commented out. If you need to set a variable, uncomment it and configure it with your value. You can then run `codenvy config` to apply this configuration to your system. Any `codenvy start` will reapply this configuration.

When Codenvy initializes itself, it creates a `/config` folder in the current directory or uses the value of `CODENVY_CONFIG`. It then populates `CODENVY_CONFIG` with puppet configuration templatees specific to the version of Codenvy that you are planning to run. While similar, `CODENVY_CONFIG` is different from `CODENVY_INSTANCE/config`, which has instance-specific configuration for a Codenvy installation. 

You can run `codenvy init` to install a new configuration into an empty directory. This command uses the `codenvy/init:<version>` Docker container to deliver a version-specific set of puppet templates into the folder.

If you run `codenvy config`, Codenvy runs puppet to transform your puppet templates into a Codenvy instance configuration, placing the results into `CODENVY_INSTANCE` or if you have not set that then a subdirectory named `/instance`. Each time you start Codenvy, we rerun `codenvy config` to make sure that instance configuration files are properly generated and consistent with the configuration you have specified in `CODENVY_CONFIG/codenvy.env`.

When doing an initialization, if you have `CODENVY_VERSION`, `CODENVY_HOST`, `CODENVY_CONFIG`, or `CODENVY_INSTANCE` set in memory of your shell, then those values will be inserted into your `CODENVY_CONFIG/codenvy.env` template. After initialization, you can edit any environment variable in `codenvy.env` and rerun `codenvy config` to update the system.

#### Saving Configuration in Version Control
Administration teams that want to version control your Codenvy configuration should save `CODENVY_CONFIG/codenvy.env`. This is the only file that should be saved with version control. It is not necessary, and even discouraged, to save the other files in the `CODENVY_CONFIG` folder. If you were to perform a `codenvy upgrade` we may replace these files with templates that are specific to the version that is being upgraded. The `codenvy.env` file maintains fidelity between versions and we can generate instance configurations from that.

The version control sequence would be:
1. `codenvy init` to get an initial configuration for a particular version.
2. Edit `CODENVY_CONFIG/codenvy.env` with your environment-specific configuration.
3. Save `CODENVY_CONFIG/codenvy.env` to version control.
4. When pulling from version control, copy `CODENVY_CONFIG/codenvy.env` into any configuration folder after initialization.
5. You can then run `codenvy config` or `codenvy start` and the instance configuration will be generated from this file.
    
#### Logs and User Data
When Codenvy initializes itself, it creates a `/instance` folder in the directory to store logs, user data, the database, and instance-specific configuration. Codenvy's containers are started with `host:container` volume bindings to mount this information into and out of the containers that require it. You can save the `/instance` folder as a backup for an entire Codenvy instance. 

Codenvy's containers save their logs in the same location:
```
/logs/codenvy/2016                 # Server logs
/logs/codenvy/che-machine-logs     # Workspace logs
/logs/nginx                        # nginx access and error logs
/logs/haproxy                      # HAproxy logs
```

User data is stored in:
```
/data/codenvy                      # Project backups (we synchronize projs from remote ws here)
/data/postgres                     # Postgres data folder (users, workspaces, stacks etc)
/data/registry                     # Workspace snapshots
```

Instance configuration is generated by Codenvy and is updated by our internal configuration utilities. These 'generated' configuration files should not be modified and stored in:
```
/codenvy.var                       # Version of Codenvy installed
/docker-compose.yml                # Docker compose to launch internal services
/config                            # Configuration files which are input mounted into the containers
```

#### oAuth
You can configure Google, GitHub, Microsoft, BitBucket, or WSO2 oAuth for use when users login or create an account.

Codenvy is shipped with a preconfigured GitHub oAuth application for the `codenvy.onprem` hostname. To enable GitHub oAuth, add `CODENVY_HOST=codenvy.onprem` to `CODENVY_CONFIG/codenvy.env` and restart. If you have a custom DNS, you need to register a GitHub oAuth application with GitHub's oAuth registration service. You will be asked for the callback URL, which is `http://<your_hostname>/api/oauth/callback`. You will receive from GitHub a client ID and secret, which must be added to `codenvy.env`:
```
CODENVY_GITHUB_CLIENT_ID=yourID
CODENVY_GITHUB_SECRET=yourSecret
```

Google oAuth (and others) are configured the same:
```
CODENVY_GOOGLE_CLIENT_ID=yourID
CODENVY_GOOGLE_SECRET=yourSecret
```

#### LDAP
Codenvy is compatible with `InetOrgPerson.schema`. For other schemas please contact us at info@codenvy.com. We support user authentication, LDAP connections, SSL, SASL, and various synchronization strategies. See the [LDAP page](docs/LDAP.md) for details.

#### Development Mode
For Codenvy developers that are building and customizing Codenvy from its source repository, there is a development that maps the runtime containers to your source repository. If you are developing in the `http://github.com/codenvy/codenvy` repository, you can turn on development mode to allow puppet configuration files and your local Codenvy assembly to be mounted into the appropriate containers. Dev mode is activated by setting environment variables and restarting (if Codenvy is running) or starting Codenvy (if this is the first run):
```
CODENVY_DEVELOPMENT_MODE="on"
CODENVY_DEVELOPMENT_REPO=<path-codenvy-repo>
```
You must run Codenvy from the root of the Codenvy repository. By running in the repository, the local `codenvy.sh` and `cli.sh` scripts will override any installed CLI packages. Additionally, two containers will have host mounted files from the local repository. During the `codenvy config` phase, the repository's `/modules` and `/manifests` will be mounted into the puppet configurator.  During the `codenvy start` phase, a local assembly from `assembly/onpremises-ide-packaging-tomcat-codenvy-allinone/target/onpremises-ide-packaging-tomcat-codenvy-allinone` is mounted into the `codenvy/codenvy` runtime container.

#### Licensing
Codenvy starts with a Fair Source 3 license, which gives you up to three users and full functionality of the system with limited liabilities and warranties. You can request a trial license from Codenvy for more than 3 users or purchase one from our friendly sales team (your mother would approve). Once you gain the license, start Codenvy and then apply the license in the admin dashboard that is accessible with your login credentials.

#### Hostname
The IP address or DNS name of where the Codenvy endpoint will service your users. If you are running this on a local system, we auto-detect this value as the IP address of your Docker daemon. On many systems, especially those from cloud hosters like DigitalOcean, you may have to explicitly set this to the external IP address or DNS entry provided by the provider.

```
CODENVY_HOST=<ip address or dns entry>
```

#### HTTP/S
By default Codenvy runs over HTTP as this is simplest to install. There are two requirements for configuring HTTP/S:  
1. You must bind Codenvy to a valid DNS name. The HTTP mode of Codenvy allows us to operate over IP addresses. HTTP/S requires certificates that are bound to a DNS entries that you purchase from a DNS provider.  
2. A valid SSL certificate.  

To configure HTTP/S, in the `codenvy.env`:
```
CODENVY_HOST_PROTOCOL=https
CODENVY_PATH_TO_HAPROXY_SSL_CERTIFICATE=<path-to-certificate>
```

#### SMTP
By default, Codenvy is configured to use a dummy mail server which makes registration with user email not possible, although admin can still create users or configure oAuth. To configure Codenvy to use SMTP server of choice, provide values for the following environment variables in `codenvy.env` (below is an example for GMAIL):

```
CODENVY_MAIL_HOST=smtp.gmail.com
CODENVY_MAIL_HOST_PORT=465
CODENVY_MAIL_SMTP_AUTH=true
СODENVY_MAIL_TRANSPORT_PROTOCOL=smtp
CODENVY_MAIL_SMTP_AUTH_USERNAME=example@gmail.com
CODENVY_MAIL_SMTP_AUTH_PASSWORD=password
CODENVY_MAIL_SMTP_SOCKETFACTORY_PORT=465
CODENVY_MAIL_SMTP_SOCKETFACTORY_CLASS=javax.net.ssl.SSLSocketFactory
CODENVY_MAIL_SMTP_SOCKETFACTORY_FALLBACK=false
```

#### Workspace Limits
You can place limits on how users interact with the system to control overall system resource usage. You can define how many workspaces created, RAM consumed, idle timeout, and a variety of other parameters. See "Workspace Limits" in `CODENVY_CONFIG/codenvy.env`.

#### Private Docker Registries
Some enterprises use a trusted Docker registry to store their Docker images. If you want your workspace stacks and machines to be powered by these images, then you need to configure each registry and the credentialed access. Once these registries are configured, then you can have users or team leaders create stacks that use recipes with Dockerfiles or images using the `FROM <your-registry>/<your-repo>` syntax.

There are different configurations for AWS EC2 and the Docker regsitry. You can define as many different registries as you'd like, using the numerical indicator in the environment variable. In case of adding several registries just copy set of properties and append `REGISTRY[n]` for each variable.

In the `codenvy.env` file:
```
CODENVY_DOCKER_REGISTRY_AUTH_REGISTRY1_URL=url1
CODENVY_DOCKER_REGISTRY_AUTH_REGISTRY1_USERNAME=username1
CODENVY_DOCKER_REGISTRY_AUTH_REGISTRY1_PASSWORD=password1

CODENVY_DOCKER_REGISTRY_AWS_REGISTRY1_ID=id1
CODENVY_DOCKER_REGISTRY_AWS_REGISTRY1_REGION=region1
CODENVY_DOCKER_REGISTRY_AWS_REGISTRY1_ACCESS__KEY__ID=key_id1
CODENVY_DOCKER_REGISTRY_AWS_REGISTRY1_SECRET__ACCESS__KEY=secret1
```

## Managing

#### Scaling
Codenvy workspaces can run on different physical nodes that are part of a Codenvy cluster managed by Docker Swarm. This is an essential part of managing large development teams, as workspaces are both RAM and CPU intensive operations, and developers do not like to share their computing power when they have a compilation that they want done. So you will want to allocate enough physical nodes to smartly handle the right number of concurrently *running* workspaces, each of which will have a RAM block.

Each Codenvy instance generates a configuration on how to add nodes into the cluster. Run `codenvy add-node` for instructions of what to run on each physical node that should be added into the cluster. The physical node runs a script which installs some software from the Codenvy master node, configures its Docker daemon, and then registers itself as a member of the Codenvy cluster.

You can remove nodes with `codenvy remove-node <ip>`.

#### Upgrading
Not yet supported. We will support this in time for Dockerized Codenvy GA.

You can run `codenvy version` to see the list of available upgrade paths for your current Codenvy installation. Since Codenvy is deployed as a set of stateless Docker containers, the upgrade process involves our utilities performing a pull of the new Docker images for the new version, orderly pausing of developers services, backing up data, activating new containers for Codenvy, running internal data migration scripts, and restoring developer services. If any situation were to fail, we wipe the system, create a new Codenvy instance with the backup and launch a set of Codenvy containers matching the old version and then restoring services. Microservices are great as they allow for this simple recovery approach.

#### Backup (Backup)
You can run `codenvy backup` to create a copy of the relevant configuration information, user data, projects, and workspaces. We do not save workspace snapshots as part of a routine backup exercise. You can run `codenvy restore` to recover Codenvy from a particular backup snapshot. The backup is saved as a TAR file that you can keep in your records.

##### Microsoft Windows and NTFS
Due to differences in file system types between NTFS and what is commonly used in the Linux world, there is no convenient way to directly host mount Postgres database data from within the container onto your host. We store your database data in a Docker named volume inside your boot2docker or Docker for Windows VM. Your data is persisted permanently. If the underlying VM is destroyed, then the data will be lost.

However, when you do a `codenvy backup`, we do copy the Postgres data from the container's volume to your host drive, and make it available as part of a `codenvy restore` function. The difference is that if you are browsing your `CODENVY_INSTANCE` folder, you will not see the database data on Windows.


#### Runbook

#### Monitoring

#### Migration
We currently do not support migrating from the puppet-based configuration of Codenvy to the Dockerized version. We do have a manual process which can be followed to move data between the puppet and Dockerized versions. The versions must be identical. Contact us to let our support team perform this migration for you.

#### Disaster Recovery
We maintain a disaster recovery [policy and best practices](http://codenvy.readme.io/v5.0/docs/disaster-recovery).

## CLI Reference
The Codenvy CLI is a self-updating utility. Once installed on your system, it will update itself when you perform a new invocation, by checking for the appropriate version that matches `CODENVY_VERSION`. The CLI saves its version-specific progarms in `~/.codenvy/cli`. The CLI also logs command execution into `~/.codenvy/cli/cli.logs`.  

The CLI is configured to hide most error conditions from the output screen. If you believe that Codenvy or the CLI is starting with errors, the `cli.logs` file will have all of the traces and error output from your executions.

Refer to [CLI](docs/cli) documentation for additional information.

## Architecture
![Architecture](https://cloud.githubusercontent.com/assets/5337267/19623944/f2366c74-989d-11e6-970b-db0ff41f618a.png)

## Team
See [Contributors](../../graphs/contributors) for the complete list of developers that have contributed to this project.
the protocol, port and whether they allow a trailing slash/.
