# Codenvy Installation and Operation
Codenvy makes cloud workspaces for develoment teams. Install, run, and manage Codenvy with Docker.

### Quick Start
With Docker 1.11+ on Windows, Mac, or Linux:
```
$ docker run codenvy/cli start
```
This command will give you additional instructions on how to run the Codenvy CLI while setting your hostname, configuring volume mounts, and testing your Docker setup.

### TOC
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
    + [Logs and User Data](#logs-and-user-data)    + [oAuth](#oauth)
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
    + [Migration](#migration)
    + [Disaster Recovery](#disaster-recovery)
- [CLI Reference](#cli-reference)
- [Architecture](#architecture)
- [Team](#team)

## Beta
This Dockerized packaging is new. We continue to support our [puppet-based installation for production usage](http://codenvy.readme.io/docs/installation). 

When the following are implemented, this Docker approach will be declared generally available:

1. Migrations. We do not yet support migrating an existing Codenvy installation that uses our Puppet-based infrastructure into a Dockerized infrastructure of the same version. Currently, Dockerized Codenvy installations need to be different installations apart from the puppetized infrastructure.

2. Admin dashboard. We provide an administrators' dashboard within the UI. This admin dashboard has not yet been updated for a dockerized version where there is a simpler configuration approach. We are updating this in an upcoming sprint.

3. In some limited firewall cases, workspaces will not start. This happens because certain internal Codenvy traffic is sent over an external IP address which is routed through your system's firewall. We can use socat and internal IP addresses within our Swarm cluster to avoid this issue.

## Getting Help
If you are Codenvy customer, file a ticket through email support for a quicker response.

If you run into an issue, please [open a GitHub issue](http://github.com/codenvy/codenvy/issues) providing:
- the host distribution and release version
- output of the `docker version` command
- output of the `docker info` command
- the full Docker run syntax you used for the `codenvy <command>`
- the output of `cli.log` - see [CLI Reference](#cli-reference)

## System Requirements
Codenvy installs on Linux, Mac and Windows. 

#### Hardware
* 2 cores
* 3GB RAM
* 3GB disk space

Codenvy requires 2 GB storage and 4 GB RAM for internal services. The RAM, CPU and storage resources required for your users' workspaces are additive. Codenvy's Docker images consume ~900MB of disk and the Docker images for your workspace templates can each range from 5MB up to 1.5GB. Codenvy and its dependent core containers will consume about 500MB of RAM, and your running workspaces will each require at least 250MB RAM, depending upon user requirements and complexity of the workspace code and intellisense.

Boot2Docker, docker-machine, Docker for Windows, and Docker for Mac are all Docker variations that launch VMs with Docker running in the VM with access to Docker from your host. We recommend increasing your default VM size to at least 4GB. Each of these technologies have different ways to allow host folder mounting into the VM. Please enable this for your OS so that Codenvy data is persisted on your host disk.

#### Software
* Docker 11.1+

The Codenvy CLI - a Docker image - manages the other Docker images and supporting utilities that Codenvy uses during its configuration or operations phases. The CLI also provides utilities for downloading an offline bundle to run Codenvy while disconnected from the network.

Given the nature of the development and release cycle it is important that you have the latest version of docker installed because any issue that you encounter might have already been fixed with a newer docker release.

Install the most recent version of the Docker Engine for your platform using the [official Docker releases](http://docs.docker.com/engine/installation/), including support for Mac and Windows!  If you are on Linux, you can also install using:
```bash
wget -qO- https://get.docker.com/ | sh
```

Sometimes Fedora and RHEL/CentOS users will encounter issues with SElinux. Try disabling selinux with `setenforce 0` and check if resolves the issue. If using the latest docker version and/or disabling selinux does not fix the issue then please file a issue request on the [issues](https://github.com/codenvy/codenvy/issues) page. If you are a licensed customer of Codenvy, you can get prioritized support with support@codenvy.com.

#### Workspaces
Codenvy's workspaces launch an rsync agent that allows the centralized Codenvy server to backup project source code from within each workspace to the central servers. When workspaces are shut off or restarted, the project files are automatically rsync'd back into the workspace. rsync runs at workspace start, stop, and on a scheduler. This allows us to preserve the integrity of your source code if the workspace's runtime containers were to have a failure during operation.

We install rsync into each user's workspace to run as a background service. In this version of Codenvy, your user workspaces require SSH and rsync in their base image. If you are connected to the Internet, we install rsync and SSH automatically. However, if you are doing an offline installation, then your workspace base images need to have this software included.

Some base images, like ubuntu, support this, but others like alpine, do not. If you create custom workspace recipes from Composefiles or Dockerfiles to run within Codenvy, these images must inherit from a base image that has rsync and SSH or you must ensure that these services are installed. If you do not have these services installed, the workspace will not start and provide an error to the user that may cause them to scratch their head.

In the non-container installation version of Codenvy, this requirement does not exist since we install these dependencies onto each host node that is added into the Codenvy cluster. We will be working to package up the rsync agent as a container that is deployed outside of your workspace's runtime. The container will have the dependencies and then this requirement will be removed.

#### Sizing
Codenvy's core services will run on a single node as a set of microservices. Workspaces will run on the core node and additional workspace nodes that you add into a Codenvy cluster run by Swarm. The number and size of these physical nodes is determined by a few factors.

You need to have enough RAM to support the number of concurrent *running* workspaces. A single user may have multiple running workspaces, but generally the common scenario is a user running a single workspace at a time. Workspace sizes are set by users when they create new workspaces, but you can define workspace limits in the configuration file that prevent RAM sprawl.

For sizing, determine how much RAM you want each user to consume at a time, and then estimate the peak concurrent utilization to determine how much system-wide RAM you will want. For example, internally at Codenvy, we regularly have 75 concurrently running workspaces, each sized at 16 GB RAM, for a total expectation of 1.2TB of RAM. Our machine nodes have 128 GB RAM per node and we routinely run 10-14 machine nodes to support our internal engineering efforts.

Compilation is CPU-heavy and most compilation events are queued to a single CPU. In the same Codenvy enterprise example, if our machine nodes are quad-core, we end up capacity to support 40-52 concurrent builds. If the total workspace CPU activity exceeds 40-52 events, then user performance begins to suffer due to thrashing and blocking.

The default configuration of workspaces is to auto-snapshot the workspace runtime to disk whenever it is stopped, whether by the user or through idle timeout. Many stack base images can grow to be >1GB, especially if you are installing complicated software inside of them, and thus their snapshots can be sizable as well. If you allow users to have many workspaces, even if they are stopped, each of those workspaces will have a snapshot on disk. You can apply a workspace cap on the total number of workspaces that can exist, which prevents users from having too many snapshots. The monitoring system also has parameters you can set to perform routine cleanup of old workspaces, where their snapshots are destroyed or removed from the user's system.

We have experimented with adding 1000 physical nodes into a single physical cluster. You can use the `codenvy add-node` command which generates a utility for you to run on each node that should be added to the cluster. You can also run `codenvy remove-node` to automate the removal of the node from the cluster and the movement of any remaining workspaces onto another node. 

The additional physical nodes must have Docker pre-configured similar to how you have Docker configured on the master node, including any configurations that you add for proxies or an alternative key-value store like Consul. Codenvy generates an automated script that can be run on each new node which prepares the node by installing some dependencies, adding the Codenvy SSH key, and registering itself within the Codenvy cluster.

## Installation
The Codenvy CLI (a Docker image) is downloaded when you first execute `docker run codenvy/cli:<version>` command. The CLI downloads other images that run Codenvy and its supporting utilities. The CLI also provides utilities for downloading an offline bundle to run Codenvy while disconnected from the network.

#### Nightly and Latest
Each version of Codenvy is available as a Docker image tagged with a label that matches the version, such as `codenvy/cli:5.0.0-M7`. You can see all versions available by running `docker run codenvy/cli version` or by [browsing DockerHub](https://hub.docker.com/r/codenvy/cli/tags/).

We maintain "redirection" labels which reference special versions of Codenvy:

| Variable | Description |
|----------|-------------|
| `latest` | The most recent stable release of Codenvy. |
| `5.0.0-latest` | The most recent stable release of Codenvy on the 5.x branch. |
| `nightly` | The nightly build of Codenvy. |

The software referenced by these labels can change over time. Since Docker will cache images locally, the `codenvy/cli:<version>` image that you are running locally may not be current with the one cached on DockerHub. Additionally, the `codenvy/cli:<version>` image that you are running references a manifest of Docker images that Codenvy depends upon, which can also change if you are using these special redirection tags.

In the case of 'latest' images, when you initialize an installation using the CLI, we encode your `/instance/codenvy.ver` file with the numbered version that latest references. If you begin using a CLI version that mismatches what was installed, you will be presented with an error.

To avoid issues that can appear from using 'nightly' or 'latest' redirections, you may:
1. Verify that you have the most recent version with `docker pull codenvy/cli:<version>`.
2. When running the CLI, commands that use other Docker images have an optional `--pull` and `--force` command line option [which will instruct the CLI to check DockerHub](https://github.com/codenvy/codenvy/tree/master/docs#codenvy-init) for a newer version and pull it down. Using these flags will slow down performance, but ensures that your local cache is current.

If you are running Codenvy using a tagged version that is a not a redirection label, such as `5.0.0-M7`, then these caching issues will not happen, as the software installed is tagged and specific to that particular version, never changing over time.

#### Linux:
There is nothing additional you need to install other than Docker.

#### Mac:
There is nothing additional you need to install other than Docker.

#### Windows:
There is nothing additional you need to install other than Docker.

#### Verification:
You can verify that the CLI is working:
```
docker run codenvy/cli
```
The CLI is bound inside of Docker images that are tagged with different versions. If you were to run `codenvy/cli:5.0.0-latest` this will run the latest shipping release of Codenvy and the CLI. This list of all versions available can be seen by running `codenvy version` or browsing the list of [tags available in Docker Hub](https://hub.docker.com/r/codenvy/cli/tags/).

#### Proxies
You can install and operate behind a proxy. You will be operating a clustered system that is managed by Docker, and itself is managing a cluster of workspaces each with their own runtime(s). There are three proxy configurations:
1. Configuring Docker proxy access so that Codenvy can download images from DockerHub.
2. Configuring Codenvy's system containers so that internal services can proxy to the Internet.
3. Optionally, configuring workspace proxy settings to allow users within a workspace to proxy to the Internet.

Before starting Codenvy, configure [Docker's daemon for proxy access](https://docs.docker.com/engine/admin/systemd/#/http-proxy). If you plan to scale Codenvy with multiple host nodes, each host node must have its Docker daemon configured for proxy access.

Codenvy's system runs on Java, and the JVM requires proxy environment variables in our `JAVA_OPTS`. We use the JVM for the core Codenvy server and the workspace agents that run within each workspace. You must set the proxy parameters for these system properties from `codenvy.env`. Please be mindful of the proxy URL formatting. Proxies are unforgiving if do not enter the URL perfectly, inclduing the protocol, port and whether they allow a trailing slash/.
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
While connected to the Internet, download Codenvy's Docker images:
```
docker run codenvy/cli offline
``` 
The CLI will download images and save them to `/codenvy/backup/*.tar` with each image saved as its own file. The `/backup` folder will be created as a subdirectory of the folder you volume mounted to `:/codenvy`. You can optionally save these files to a differnet location by volume mounting that folder to `:/backup`. The version tag of the CLI Docker image will be used to determine which versions of dependent images to download. There is about 1GB of data that will be saved.

##### Save Codenvy CLI
```
docker save codenvy/cli:<version>
```

##### Save Codenvy Stacks
Out of the box, Codenvy has configured a few dozen stacks for popular programming languages and frameworks. These stacks use "recipes" which contain links to Docker images that are needed to create workspaces from these stacks. These workspace runtime images are not saved as part of `codenvy offline`. There are many of these images and they consume a lot of disk space. Most users do not require all of these stacks and most replace default stacks with custom stacks using their own Docker images. If you'd like to get the images that are associated with Codenvy's stacks:
```
docker save <codenvy-stack-image-name> > backup/<base-image-name>.tar
```
The list of images that Codenvy manages is sourced from Eclipse Che's [Dockerfiles repository](https://github.com/eclipse/che-dockerfiles/tree/master/recipes). Each folder is named the same way that our images are stored.  The `alpine_jdk8` folder represents the `codenvy/alpine_jdk8` Docker image, which you would save with `docker save codenvy/alpine_jdk8 > backup/alpine_jdk8.tar`.

##### Start Offline
Extract your files to an offline computer with Docker already configured. Install the CLI files to a directory on your path and ensure that they have execution permissions. Execute the CLI in the directory that has the `offline` sub-folder which contains your tar files. Then start Codenvy in `--offline` mode:
```
docker run codenvy/cli:<version> start --offline
```
When invoked with the `--offline` parameter, the Codenvy CLI performs a preboot sequence, which loads all saved `backup/*.tar` images including any Codenvy stack images you saved. The preboot sequence takes place before any CLI configuration, which itself depends upon Docker. The `codenvy start`, `codenvy download`, and `codenvy init` commands support `--offline` mode which triggers this preboot seequence.

## Usage
#### Syntax
```
Usage: docker run -it --rm 
                  -v /var/run/docker.sock:/var/run/docker.sock
                  -v <host-path-for-codenvy-data>:/codenvy
                  ${CHE_MINI_PRODUCT_NAME}/cli:<version> [COMMAND]

    help                                 This message
    version                              Installed version and upgrade paths
    init                                 Initializes a directory with a ${CHE_MINI_PRODUCT_NAME} install
         [--no-force                         Default - uses cached local Docker images
          --pull                             Checks for newer images from DockerHub  
          --force                            Removes all images and re-pulls all images from DockerHub
          --offline                          Uses images saved to disk from the offline command
          --accept-license                   Auto accepts the Codenvy license during installation
          --reinit]                          Reinstalls using existing $CHE_MINI_PRODUCT_NAME.env configuration
    start [--pull | --force | --offline] Starts ${CHE_MINI_PRODUCT_NAME} services
    stop                                 Stops ${CHE_MINI_PRODUCT_NAME} services
    restart [--pull | --force]           Restart ${CHE_MINI_PRODUCT_NAME} services
    destroy                              Stops services, and deletes ${CHE_MINI_PRODUCT_NAME} instance data
            [--quiet                         Does not ask for confirmation before destroying instance data
             --cli]                          If :/cli is mounted, will destroy the cli.log
    rmi [--quiet]                        Removes the Docker images for <version>, forcing a repull
    config                               Generates a ${CHE_MINI_PRODUCT_NAME} config from vars; run on any start / restart
    add-node                             Adds a physical node to serve workspaces intto the ${CHE_MINI_PRODUCT_NAME} cluster
    remove-node <ip>                     Removes the physical node from the ${CHE_MINI_PRODUCT_NAME} cluster
    upgrade                              Upgrades Codenvy from one version to another with migrations and backups
    download [--pull|--force|--offline]  Pulls Docker images for the current Codenvy version
    backup [--quiet | --skip-data]           Backups ${CHE_MINI_PRODUCT_NAME} configuration and data to /codenvy/backup volume mount
    restore [--quiet]                    Restores ${CHE_MINI_PRODUCT_NAME} configuration and data from /codenvy/backup mount
    offline                              Saves ${CHE_MINI_PRODUCT_NAME} Docker images into TAR files for offline install
    info                                 Displays info about ${CHE_MINI_PRODUCT_NAME} and the CLI 
         [ --all                             Run all debugging tests
           --debug                           Displays system information
           --network]                        Test connectivity between ${CHE_MINI_PRODUCT_NAME} sub-systems
    ssh <wksp-name> [machine-name]       SSH to a workspace if SSH agent enabled
    mount <wksp-name>                    Synchronize workspace with current working directory
    action <action-name> [--help]        Start action on ${CHE_MINI_PRODUCT_NAME} instance
    compile <mvn-command>                SDK - Builds Che source code or modules
    test <test-name> [--help]            Start test on ${CHE_MINI_PRODUCT_NAME} instance

Variables:
    CODENVY_HOST                         IP address or hostname where ${CHE_MINI_PRODUCT_NAME} will serve its users
    CLI_DEBUG                            Default=false.Prints stack trace during execution
    CLI_INFO                             Default=true. Prints out INFO messages to standard out
    CLI_WARN                             Default=true. Prints WARN messages to standard out
    CLI_LOG                              Default=true. Prints messages to cli.log file
```

In these docs, when you see `codenvy [COMMAND]`, it is assumed that you run the CLI with the full `docker run ...` syntax. We short hand the docs for readability.

#### Sample Start
For example, to start the nightly build of Codenvy with its data saved on Windows in C:\tmp:
`docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock -v /c/tmp:/codenvy codenvy/cli:5.0.0-latest start`

This installs a Codenvy configuration, downloads Codenvy's Docker images, run pre-flight port checks, boot Codenvy's services, and run post-flight checks. You do not need root access to start Codenvy, unless your environment requires it for Docker operations.

A successful start will display:
```
INFO: (codenvy cli): Downloading cli-latest
INFO: (codenvy cli): Checking registry for version 'nightly' images
INFO: (codenvy config): Generating codenvy configuration...
INFO: (codenvy config): Customizing docker-compose for Windows
INFO: (codenvy start): Preflight checks
         port 80:  [OK]
         port 443: [OK]
         port 5000: [OK]

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
#### Versions
While we provide `nightly`, `latest`, and `5.0.0-latest` [redirection versions](https://github.com/codenvy/codenvy/tree/master/docs#nightly-and-latest) which are tags that simplify helping you retrieve a certain build, you should always run Codenvy with a specific version label to avoid [redirection caching issues](https://github.com/codenvy/codenvy/tree/master/docs#nightly-and-latest). So, running `docker run codenvy/cli` is great syntax for testing and getting started quickly, you should always run `docker run codenvy/cli:<version>` for production usage.

#### Volume Mounts
If you volume mount a single local folder to `<your-local-path>:/codenvy`, then Codenvy creates `/codenvy/codenvy.env` (configuration), `/codenvy/instance` (user data, projects, runtime logs, and database), and `/codenvy/backup` (data backup).

However, if you do not want your `/instance`, and `/backup` folder to all be children of the same parent folder, you can set them individually with separate overrides.

```
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock
                    -v <local-codenvy-folder>:/codenvy
                    -v <local-instance-path>:/codenvy/instance
                    -v <local-backup-path>:/codenvy/backup
                       codenvy/cli:<version> [COMMAND]    

```

#### Hosting
If you are hosting Codenvy at a cloud service like DigitalOcean, set `CODENVY_HOST` to the server's IP address or its DNS. We use an internal utility, `eclipse/che-ip`, to determine the default value for `CODENVY_HOST`, which is your server's IP address. This works well on desktops, but usually fails on hosted servers requiring you to explicitly set this value.

```
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock
                    -v <local-path>:/codenvy
                    -e CODENVY_HOST=<your-ip-or-host>
                       codenvy/cli:<version> [COMMAND]
``` 

## Uninstall
```
# Remove your Codevy configuration and destroy user projects and database
docker run codenvy/cli destroy

# Deletes Codenvy's images from your Docker registry
docker run codenvy/cli rmi
```

## Configuration
Configuration is done with environment variables in `codenvy.env` placed into the root of the folder you volume mounted to `:/codenvy`. Environment variables are stored in `codenvy.env`, a file that is generated during the `codenvy init` phase. If you rerun `codenvy init` in an already initialized folder, the process will abort unless you pass `--force`, `--pull`, or `--reinit`. 

Each variable is documented with an explanation and usually commented out. If you need to set a variable, uncomment it and configure it with your value. You can then run `codenvy config` to apply this configuration to your system. `codenvy start` also reapplies the latest configuration.

You can run `codenvy init` to install a new configuration into an empty directory. This command uses the `codenvy/init:<version>` Docker container to deliver a version-specific set of puppet templates into the folder.

If you run `codenvy config`, Codenvy runs puppet to transform your puppet templates into a Codenvy instance configuration, placing the results into `/codenvy/instance` if you volume mounted that, or into a `instance` subdirectory of the path you mounted to `/codenvy`.  Each time you start Codenvy, `codenvy config` is run to ensure instance configuration files are properly generated and consistent with the configuration you have specified in `codenvy.env`.

#### Saving Configuration in Version Control
Administration teams that want to version control your Codenvy configuration should save `codenvy.env`. This is the only file that should be saved with version control. It is not necessary, and even discouraged, to save the other files. If you were to perform a `codenvy upgrade` we may replace these files with templates that are specific to the version that is being upgraded. The `codenvy.env` file maintains fidelity between versions and we can generate instance configurations from that.

The version control sequence would be:
1. `codenvy init` to get an initial configuration for a particular version.
2. Edit `codenvy.env` with your environment-specific configuration.
3. Save `codenvy.env` to version control.
4. When pulling from version control, copy `codenvy.env` into the root of the folder you volume mount to `:/codenvy`.
5. You can then run `codenvy config` or `codenvy start` and the instance configuration will be generated from this file.
    
#### Logs and User Data
When Codenvy initializes itself, it stores logs, user data, database data, and instance-specific configuration in the folder mounted to `/codenvy/instance` or an `instance` subfolder of what you mounted to `/codenvy`.  

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
/codenvy.ver                       # Version of Codenvy installed
/docker-compose-container.yml      # Docker compose to launch internal services
/docker-compose.yml                # Docker compose to launch Codenvy from the host without contianer
/config                            # Configuration files which are input mounted into the containers
```

#### oAuth
You can configure Google, GitHub, Microsoft, BitBucket, or WSO2 oAuth for use when users login or create an account.

Codenvy is shipped with a preconfigured GitHub oAuth application for the `codenvy.onprem` hostname. To enable GitHub oAuth, add `CODENVY_HOST=codenvy.onprem` to `codenvy.env` and restart. If you have a custom DNS, you need to register a GitHub oAuth application with GitHub's oAuth registration service. You will be asked for the callback URL, which is `http://<your_hostname>/api/oauth/callback`. You will receive from GitHub a client ID and secret, which must be added to `codenvy.env`:
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
For Codenvy developers that are building and customizing Codenvy from its source repository, you can run Codenvy in development mode where your local assembly is used instead of the one that is provided in the default containers downloaded from DockerHub. This allows for a rapid edit / build / run cycle. 

Dev mode is activated by volume mounting the Codenvy git repository to `:/repo` in your Docker run command.
```
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock \
                    -v <local-path>:/codenvy \
                    -v <local-repo>:/repo \
                       codenvy/cli:<version> [COMMAND]
``` 
Dev mode will use files from your host repository in three ways:

1. During the `codenvy config` phase, the source repository's `/dockerfiles/init/modules` and `/dockerfiles/init/manifests` will be used instead of the ones that are included in the `codenvy/init` container.
2. During the CLI bootstrap phase, the source repository's `/dockerfiles/cli/cli.sh` file will be used instead of the one with in the `codenvy/cli` container. This allows CLI developers to iterate without having to rebuild `codenvy/cli` container after each change.
3. During the `codenvy start` phase, a local assembly from `assembly/onpremises-ide-packaging-tomcat-codenvy-allinone/target/onpremises-ide-packaging-tomcat-codenvy-allinone` is mounted into the `codenvy/codenvy` runtime container. You must `mvn clean install` the `assembly/onpremises-ide-packaging-tomcat-codenvy-allinone/` folder prior to activated development mode.

To activate jpda suspend mode for debugging codenvy server initialization, in the `codenvy.env`:
```
CODENVY_DEBUG_SUSPEND=true
```
To change codenvy debug port, in the `codenvy.env`:
```
CODENVY_DEBUG_PORT=8000
```

#### Licensing
Codenvy starts with a Fair Source 3 license, which gives you up to three users and full functionality of the system with limited liabilities and warranties. You can request a trial license from Codenvy for more than 3 users or purchase one from our friendly sales team (your mother would approve). Once you gain the license, start Codenvy and then apply the license in the admin dashboard that is accessible with your login credentials.

#### Hostname
The IP address or DNS name of where the Codenvy endpoint will service your users. If you are running this on a local system, we auto-detect this value as the IP address of your Docker daemon. On many systems, especially those from cloud hosters like DigitalOcean, you may have to explicitly set this to the external IP address or DNS entry provided by the provider. You can edit this in `codenvy.env`, or you can pass it during initialization to the docker command:

```
docker run <other-syntax-here> -e CODENVY_HOST=<ip address or dns entry> codenvy/cli:<version> start
```

#### HTTP/S
By default Codenvy runs over HTTP as this is simplest to install. There are two requirements for configuring HTTP/S:  
1. You must bind Codenvy to a valid DNS name. The HTTP mode of Codenvy allows us to operate over IP addresses. HTTP/S requires certificates that are bound to a DNS entries that you purchase from a DNS provider.  
2. A valid SSL certificate.  

To configure HTTP/S, in `codenvy.env`:
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
You can place limits on how users interact with the system to control overall system resource usage. You can define how many workspaces created, RAM consumed, idle timeout, and a variety of other parameters. See "Workspace Limits" in `codenvy.env`.

#### Private Docker Registries
Some enterprises use a trusted Docker registry to store their Docker images. If you want your workspace stacks and machines to be powered by these images, then you need to configure each registry and the credentialed access. Once these registries are configured, then you can have users or team leaders create stacks that use recipes with Dockerfiles or images using the `FROM <your-registry>/<your-repo>` syntax.

There are different configurations for AWS EC2 and the Docker regsitry. You can define as many different registries as you'd like, using the numerical indicator in the environment variable. In case of adding several registries just copy set of properties and append `REGISTRY[n]` for each variable.

In `codenvy.env` file:
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
Upgrading Codenvy is done by downloading a `codenvy/cli:<version>` that is newer than the version you currently have installed. For example, if you have 5.0.0-M2 installed and want to upgrade to 5.0.0-M7, then:
```
# Get the new version of Codenvy
docker pull codenvy/cli:5.0.0-M7

# You now have two codenvy/cli images (one for each version)
# Perform an upgrade - use the new image to upgrade old installation
docker run <volume-mounts> codenvy/cli:5.0.0-M7 upgrade
``` 

The upgrade command has numerous checks to prevent you from upgrading Codenvy if the new image and the old version are not compatible. In order for the upgrade procedure to advance, the CLI image must be newer that the version in `/instance/codenvy.ver`.

The upgrade process: a) performs a version compatibility check, b) downloads new Docker images that are needed to run the new version of Codenvy, c) stops Codenvy if it is currently running triggering a maintenance window, d) backs up your installation, e) initializes the new version, and f) starts Codenvy.

You can run `codenvy version` to see the list of available versions that you can upgrade to.

#### Backup (Backup)
You can run `codenvy backup` to create a copy of the relevant configuration information, user data, projects, and workspaces. We do not save workspace snapshots as part of a routine backup exercise. You can run `codenvy restore` to recover Codenvy from a particular backup snapshot. The backup is saved as a TAR file that you can keep in your records.

##### Microsoft Windows and NTFS
Due to differences in file system types between NTFS and what is commonly used in the Linux world, there is no convenient way to directly host mount Postgres database data from within the container onto your host. We store your database data in a Docker named volume inside your boot2docker or Docker for Windows VM. Your data is persisted permanently. If the underlying VM is destroyed, then the data will be lost.

However, when you do a `codenvy backup`, we do copy the Postgres data from the container's volume to your host drive, and make it available as part of a `codenvy restore` function. The difference is that if you are browsing your `/codenvy/instance` folder, you will not see the database data on Windows.

#### Migration
We currently do not support migrating from the puppet-based configuration of Codenvy to the Dockerized version. We do have a manual process which can be followed to move data between the puppet and Dockerized versions. The versions must be identical. Contact us to let our support team perform this migration for you.

#### Disaster Recovery
We maintain a disaster recovery [policy and best practices](http://codenvy.readme.io/v5.0/docs/disaster-recovery).

## CLI Reference
The CLI is configured to hide most error conditions from the output screen. The CLI prints internal stack traces and error output to `cli.log`. To see the output of this log, you will need to volume mount a local path to `:/cli`. For example:

```
docker run --rm -it 
           -v /var/run/docker.sock:/var/run/docker.sock 
           -v /c/codenvy:/codenvy 
           -v /c/codenvy/cli:/cli codenvy/cli:nightly [COMMAND]
```

### `codenvy init`
Initializes an empty directory with a Codenvy configuration and instance folder where user data and runtime configuration will be stored. You must provide a `<path>:/codenvy` volume mount, then Codenvy creates a `instance` and `backup` subfolder of `<path>`. You can optionally override the location of `instance` by volume mounting an additional local folder to `:/codenvy/instance`. You can optionally override the location of where backups are stored by volume mounting an additional local folder to `:/codenvy/backup`.  After initialization, a `codenvy.env` file is placed into the root of the path that you mounted to `:/codenvy`. 

These variables can be set in your local environment shell before running and they will be respected during initialization:

| Variable | Description |
|----------|-------------|
| `CODENVY_HOST` | The IP address or DNS name of the Codenvy service. We use `eclipse/che-ip` to attempt discovery if not set. |

Codenvy depends upon Docker images. We use Docker images in three ways:
1. As cross-platform utilites within the CLI. For example, in scenarios where we need to perform a `curl` operation, we use a small Docker image to perform this function. We do this as a precaution as many operating systems (like Windows) do not have curl installed.
2. To look up the master version and upgrade manifest, which is stored as a singleton Docker image called `codenvy/version`. 
3. To perform initialization and configuration of Codenvy such as with `codenvy/init`. This image contains templates that are delivered as a payload and installed onto your computer. These payload images can have different files based upon the image's version.
4. To run Codenvy and its dependent services, which include Codenvy, HAproxy, nginx, Postgres, socat, and Docker Swarm.

You can control the nature of how Codenvy downloads these images with command line options. All image downloads are performed with `docker pull`. 

| Mode>>>> | Description |
|------|-------------|
| `--no-force` | Default behavior. Will download an image if not found locally. A local check of the image will see if an image of a matching name is in your local registry and then skip the pull if it is found. This mode does not check DockerHub for a newer version of the same image. |
| `--pull` | Will always perform a `docker pull` when an image is requested. If there is a newer version of the same tagged image at DockerHub, it will pull it, or use the one in local cache. This keeps your images up to date, but execution is slower. |
| `--force` | Performs a forced removal of the local image using `docker rmi` and then pulls it again (anew) from DockerHub. You can use this as a way to clean your local cache and ensure that all images are new. |
| `--offline` | Loads Docker images from `backup/*.tar` folder during a pre-boot mode of the CLI. Used if you are performing an installation or start while disconnected from the Internet. |

The initialization of a Codenvy installation requires the acceptance of our default Fair Source 3 license agreement, which allows for some access to the source code and [usage for up to three people](http://codenvy.com/legal). You can auto-accept the license agreement without prompting for a response for silent installation by passing the `--accept-license` command line option.

You can reinstall Codenvy on a folder that is already initialized and preserve your `/codenvy/codenvy.env` values by passing the `--reinit` flag.

### `codenvy config`
Generates a Codenvy instance configuration thta is placed in `/codenvy/instance`. This command uses puppet to generate configuration files for Codenvy, haproxy, swarm, socat, nginx, and postgres which are mounted when Codenvy services are started. This command is executed on every `start` or `restart`.

If you are using a `codenvy/cli:<version>` image and it does not match the version that is in `/instance/codenvy.ver`, then the configuration will abort to prevent you from running a configuration for a different version than what is currently installed.

This command respects `--no-force`, `--pull`, `--force`, and `--offline`.

### `codenvy start`
Starts Codenvy and its services using `docker-compose`. If the system cannot find a valid configuration it will perform a `codenvy init`. Every `start` and `restart` will run a `codenvy config` to generate a new configuration set using the latest configuration. The starting sequence will perform pre-flight testing to see if any ports required by Codenvy are currently used by other services and post-flight checks to verify access to key APIs.  

### `codenvy stop`
Stops all of the Codenvy service containers and removes them.

### `codenvy restart`
Performs a `codenvy stop` followed by a `codenvy start`, respecting `--pull`, `--force`, and `--offline`.

### `codenvy destroy`
Deletes `/docs`, `codenvy.env` and `/codenvy/instance`, including destroying all user workspaces, projects, data, and user database. If you pass `--quiet` then the confirmation warning will be skipped. 

If you have mounted the `:/cli` path, then we write the `cli.log` to your host directory. By default, this log is not destroyed in a `codenvy destroy` command so that you can maintain a record of all CLI executions. You can also have this file removed from your host by mounting `:/cli` and passing the `--cli` parameter to this command.

### `codenvy offline`
Saves all of the Docker images that Codenvy requires into `/backup/*.tar` files. Each image is saved as its own file. If the `backup` folder is available on a machine that is disconnected from the Internet and you start Codenvy with `--offline`, the CLI pre-boot sequence will load all of the Docker images in the `/backup/` folder.

### `codenvy rmi`
Deletes the Docker images from the local registry that Codenvy has downloaded for this version.

### `codenvy download`
Used to download Docker images that will be stored in your Docker images repository. This command downloads images that are used by the CLI as utilities, for Codenvy to do initialization and configuration, and for the runtime images that Codenvy needs when it starts.  This command respects `--offline`, `--pull`, `--force`, and `--no-force` (default).  This command is invoked by `codenvy init`, `codenvy config`, and `codenvy start`.

This command is invoked by `codenvy init` before initialization to download the images for the version specified by `codenvy/cli:<version>`.

### `codenvy version`
Provides information on the current version and the available versions that are hosted in Codenvy's repositories. `codenvy upgrade` enforces upgrade sequences and will prevent you from upgrading one version to another version where data migrations cannot be guaranteed.

### `codenvy upgrade`
Manages the sequence of upgrading Codenvy from one version to another. Run `codenvy version` to get a list of available versions that you can upgrade to.

Upgrading Codenvy is done by using a `codenvy/cli:<version>` that is newer than the version you currently have installed. For example, if you have 5.0.0-M2 installed and want to upgrade to 5.0.0-M7, then:
```
# Get the new version of Codenvy
docker pull codenvy/cli:5.0.0-M7

# You now have two codenvy/cli images (one for each version)
# Perform an upgrade - use the new image to upgrade old installation
docker run <volume-mounts> codenvy/cli:5.0.0-M7 upgrade
``` 

The upgrade command has numerous checks to prevent you from upgrading Codenvy if the new image and the old version are not compatiable. In order for the upgrade procedure to proceed, the CLI image must be newer than the value of '/instance/codenvy.ver'.

The upgrade process: a) performs a version compatibility check, b) downloads new Docker images that are needed to run the new version of Codenvy, c) stops Codenvy if it is currently running triggering a maintenance window, d) backs up your installation, e) initializes the new version, and f) starts Codenvy.

You can run `codenvy version` to see the list of available versions that you can upgrade to.

### `codenvy info`
Displays system state and debugging information. `--network` runs a test to take your `CODENVY_HOST` value to test for networking connectivity simulating browser > Codenvy and Codenvy > workspace connectivity.

### `codenvy backup`
Tars your `/instance` into files and places them into `/backup`. These files are restoration-ready.

### `codenvy restore`
Restores `/instance` to its previous state. You do not need to worry about having the right Docker images. The normal start / stop / restart cycle ensures that the proper Docker images are available or downloaded, if not found.

This command will destroy your existing `/instance` folder, so use with caution, or set these values to different folders when performing a restore.

### `codenvy add-node`
Adds a new physical node into the Codenvy cluster. That node must have Docker pre-configured similar to how you have Docker configured on the master node, including any configurations that you add for proxies or an alternative key-value store like Consul. Codenvy generates an automated script that can be run on each new node which prepares the node by installing some dependencies, adding the Codenvy SSH key, and registering itself within the Codenvy cluster.

### `codenvy remove-node`
Takes a single parameter, `ip`, which is the external IP address of the remote physical node to be removed from the Codenvy cluster. This utility does not remove any software from the remote node, but it does ensure that workspace runtimes are not executing on that node. 

## Architecture
![Architecture](https://cloud.githubusercontent.com/assets/5337267/19623944/f2366c74-989d-11e6-970b-db0ff41f618a.png)

## Team
See [Contributors](../../graphs/contributors) for the complete list of developers that have contributed to this project.
the protocol, port and whether they allow a trailing slash/.
