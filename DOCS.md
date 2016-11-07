# Codenvy Installation and Operation
Codenvy makes cloud workspaces for develoment teams. Install Codenvy as a set of Docker containers.

- Beta
- Team
- Getting Stated
 - Explain trial system
 - Explain production system
 - Links to Resources
- Installation: Trial - new page focused on how to setup a AIO or AIO+1 environment
 - Prerequisites (system requirements, ports, data storage, etc...)
 - Install of AIO (incl. proxies)
 - Usage - explain how to start the system then link to tutorials and other sections
 - Upgrading
 - HTTPS
 - Add-node
  - Ports for nodes as well
 - Licensing
- Installation: Production (incl. Proxies, reference the main config sections)
- Installation: Offline
 - Downloading images
 - Transferring
 - Installing
- Configuration  
 - Proxies (Config: Proxies : Take section out of https://codenvy.readme.io/v5.0/docs/configuration-docker)
 - Logs and Data
 - Licensing
- Config: Networking
 - Hostname
 - HTTP/HTTPS
 - SMTP
- Config: Workspaces 
 - Private Registries
 - Mirroring DockerHub
 - Private Repositories
 - Permissions
 - Workspace Limits
- Config: Authentication
 - oAuth
 - LDAP
- Config: Scaling
 - Add-node
 - Remove-node
- Managing
 - Upgrades - Upgrading
 - Runbook
 - Monitoring
 - Backup (Backup)
 - Migration
 - Disaster Recovery - DR page (http://codenvy.readme.io/v5.0/docs/disaster-recovery)
- Reference
 - CLI
 - API
- Architecture
 - Scalability Model
 - Deployment Model
 - Service Architecture

## Beta
This packaging and deployment approach is relatively new. We do not yet consider this ready for production deployment of Codenvy. We hope to offer this as the primary production configuration by the end of 2016. Items to be added:

1. `codenvy upgrade` is not yet implemented. You can switch between versions, but we do not yet support automatic data migration inside of images. 

2. Networking overlay mode. If you are running a Codenvy cluster on different physical nodes and your users launch compose workspaces that themselves have multiple containers, there are cases where Swarm will place those different containers on different physical nodes. This is great for scalability. However, our default networking mode is `bridge`, which will prevent those workspace containers from seeing each other, and your users will scratch their heads. We are testing an `overlay` mode which configures `etcd` automatically that will let workspace containers see one another regardless of where Swarm places their operation.

3. HTTP/S. We are working to make configuration of SSL and HTTP/S a single line so that you can swap between configurations. The current version only supports HTTP.

4. NTFS backups. Due to incompatibilities between NTFS and other file systems, Windows will have their Postgres data stored in a named volume within the boot2docker or Docker for Windows. User data is persisted, but if the VM that you are using is wiped and restarted, Codenvy's Postgres data will be lost. We will add an ability to extract this information with `codenvy backup` and `codenvy restore`.

5. System-level configuration of private Docker registries is not yet enabled in this packaging. It's possible to configure this manually by modifying `CODENVY_CONFIG`/manifests/codenvy.pp. However, if you run `codenvy destroy` or `codenvy init`, your configuration changes for registries will not be preserved.

6. Add a `codenvy reload` command, which resarts services with a SIGHUP signal instead of a container restart. SIGHUP signals instruct container services to reload their configuration without going through a reboot cycle.

## Team
See [Contributors](../../graphs/contributors) for the complete list of developers that have contributed to this project.

## Getting Stated
#### Explain trial system
#### Explain production system
#### Links to Resources
## Installation: Trial

#### System Requirements
Codenvy installs on Linux, Mac and Windows. 

#### Prerequisites (system requirements, ports, data storage, etc...)
Docker is a relatively new project and is actively tested by a thriving community.

Hardware Requirements:
* 2 cores
* 3GB RAM
* 3GB disk space

This will let you install Codenvy and run a single workspace. Codenvy's Docker images consume about 800MB of disk and the Docker images for your workspace templates can each range from 5MB up to 1.5GB. Codenvy and its dependent core containers will consume about 500MB of RAM, and your running workspaces will each require at least 250MB RAM, depending upon user requirements and complexity of the workspace code and intellisense.

Boot2Docker, docker-machine, Docker for Windows, and Docker for Mac are all variations that launch virtual machines that contain a Docker daemon that allows you to run Docker. We recommend increasing your default VM size to at least 4GB. Each virtualization solution has different requirements around mounting VM folders to your host machine - please enable this for your OS so that Codenvy data is persisted on your host disk.

Software Recommendation:
* Codenvy CLI

The Codenvy images and supporting utilities are downloaded and maintained by the CLI. The CLI also provides utilities for downloading an offline bundle to run Codenvy while disconnected from the network. Reader [Install of AIO]() section below for additional information.

Software Requirements:
* Docker 11.1+
* Docker Compose 1.8+. 
* Bash

Docker for Mac and Windows have compose pre-installed. See: [Install Docker Compose on Linux](https://docs.docker.com/compose/install/). The Docker Toolbox for Windows installs [Git Bash for Windows](https://git-for-windows.github.io/), which is needed to run the CLI, a cross-platform set of bash scripts.

Given the nature of the development and release cycle it is important that you have the latest version of docker installed because any issue that you encounter might have already been fixed with a newer docker release.

Install the most recent version of the Docker Engine for your platform using the [official Docker releases](http://docs.docker.com/engine/installation/), including support for Mac and Windows!  If you are on Linux, you can also install using:
```bash
wget -qO- https://get.docker.com/ | sh
```

Sometimes Fedora and RHEL/CentOS users will encounter unexpected issues. Try disabling selinux with `setenforce 0` and check if resolves the issue. If using the latest docker version and/or disabling selinux does not fix the issue then please file a issue request on the [issues](https://github.com/codenvy/codenvy/issues) page. If you are a licensed customer of Codenvy, you can get prioritized support with support@codenvy.com.


Install the most recent version of Docker Compose for your platform using the [official Docker releases](https://github.com/docker/compose/releases). With Windows and Mac, this comes with Docker Toolbox. If you are on Linux, you can also install using:
```bash
curl -L "https://github.com/docker/compose/releases/download/1.8.1/docker-compose-$(uname -s)-$(uname -m)" > /usr/local/bin/docker-compose
```
If you run the CLI and you get issues (or no output), we do advanced logging and include all error messages in ~/.codenvy/cli/cli.log. It will have more information to tell you what happened.

In your issue report please make sure you provide the following information:
- The host distribution and release version
- Output of the `docker version` command
- Output of the `docker info` command
- The `codenvy <command>` you used to run Codenvy

Proxy:
Read the [Proxy]() section under configuration heading for additional information in configuring the codenvy.

#### Install of AIO

Linux:
```
sudo curl -sL https://raw.githubusercontent.com/codenvy/codenvy/hackathon/codenvy.sh > /usr/local/bin/codenvy
sudo curl -sL https://raw.githubusercontent.com/codenvy/codenvy/hackathon/cli.sh > /usr/local/bin/cli.sh
chmod +x /usr/local/bin/codenvy
chmod +x /usr/local/bin/cli.sh
```

Mac:
```
curl -sL https://raw.githubusercontent.com/codenvy/codenvy/hackathon/codenvy.sh > /usr/local/bin/codenvy
curl -sL https://raw.githubusercontent.com/codenvy/codenvy/hackathon/cli.sh > /usr/local/bin/cli.sh
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

Windows:
```
curl -sL https://raw.githubusercontent.com/codenvy/codenvy/hackathon/codenvy.sh > codenvy.sh
curl -sL https://raw.githubusercontent.com/codenvy/codenvy/hackathon/codenvy.bat > codenvy.bat
curl -sL https://raw.githubusercontent.com/codenvy/codenvy/hackathon/cli.sh > cli.sh
set PATH=<path-to-cli>;%PATH%
```

You can verify the CLI is working:
```
codenvy help
```

The CLI is self-updating. If you modify the `cli.sh` companion script or change your `CODENVY_VERSION` then an updated CLI will be downloaded. The CLI installs its core subsystems into `~/.codenvy/cli`.

#### Usage
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


#### Upgrading
#### Hosting
We use an internal utility, `codenvy/che-ip`, to determine the default value for `CODENVY_HOST`, which is your server's IP address. This works well on desktops, but usually fails on hosted servers. If you are hosting Codenvy at a cloud service like DigitalOcean, set `CODENVY_HOST` to the server's IP address or its DNS.
#### HTTPS
#### Add-node
 #### Ports for nodes as well
#### Licensing
## Installation: Production (incl. Proxies, reference the main config sections)
## Installation: Offline
We support the ability to install and run Codenvy while disconnected from the Internet. This is helpful for certain restricted environments, regulated datacenters, or offshore installations. 

#### Downloading images
While connected to the Internet and with access to DockerHub, download Codenvy's Docker images as a set of files with `codenvy offline`. Codenvy will download all dependent images and save them to `offline/*.tar` with each image saved as its own file. `CODENVY_VERSION` environment variable is used to determine which images to download unless you already have a Codenvy installation, then the value of `instance/codenvy.ver` will be used. There is about 1GB of data that will be saved.

#### Transferring
Save `codenvy.sh`, `cli.sh`, and if on Windows, `codenvy.bat`.

Out of the box, Codenvy has configured a few dozen stacks for popular programming languages and frameworks. These stacks use "recipes" which contain links to Docker images that are needed to create workspaces from these stacks. These workspace runtime images are not saved as part of `codenvy offline`. There are many of these images and they consume a lot of disk space. Most users do not require all of these stacks and most replace default stacks with custom stacks using their own Docker images. If you'd like to get the images that are associated with Codenvy's stacks:
```
docker save <codenvy-stack-image-name> > offline/<base-image-name>.tar
```
The list of images that Codenvy manages is sourced from Eclipse Che's [Dockerfiles repository](https://github.com/eclipse/che-dockerfiles/tree/master/recipes). Each folder is named the same way that our images are stored.  The `alpine_jdk8` folder represents the `codenvy/alpine_jdk8` Docker image, which you would save with `docker save codenvy/alpine_jdk8 > offline/alpine_jdk8.tar`.

#### Usage
Extract your files to an offline computer with Docker already configured. Install the CLI files to a directory on your path and ensure that they have execution permissions. Execute the CLI in the directory that has the `offline` sub-folder which contains your tar files. Then start Codenvy in `--offline` mode:
```
codenvy start --offline
```
When invoked with the `offline` parameter, the Codenvy CLI performs a preboot sequence, which loads all saved `offline/*.tar` images including any Codenvy stack images you saved. The preboot sequence takes place before any CLI configuration, which itself depends upon Docker. The `codenvy start`, `codenvy download`, and `codenvy init` commands support `--offline` mode which triggers this preboot seequence.

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
All configuration is done with environment variables. Environment variables are stored in `CODENVY_CONFIG/codenvy.env`, a file that is generated during the `codenvy init` phase. If you rerun `codenvy init` in the same `CODENVY_CONFIG`, the process will abort unless you pass `--force` or `--pull`. You can have multiple `CODENVY_CONFIG` folders in order to keep profiles of configuration.

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

#### Proxies (Config: Proxies : Take section out of https://codenvy.readme.io/v5.0/docs/configuration-docker)
We support installation and operation behind a proxy. You will be operating a clustered system that is managed by Docker, and itself is managing a cluster of workspaces each with their own runtime(s). There are three proxy configurations:
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

`_NO_PROXY` variable setting is required if you use a fake local DNS. Java and other internal utilities will avoid accessing a proxy for internal communications when this value is set.

#### Logs and Data
#### Licensing
## Config: Networking
#### Hostname
#### HTTP/S
By default Codenvy runs over HTTP as this is simplest to install. You can switch to HTTPS at any time. 

There are two requirements for configuring HTTP/S:
1. You must bind Codenvy to a valid DNS name. The HTTP mode of Codenvy allows us to operate over IP addresses. HTTP/S requires certificates that are bound to a DNS entries that you purchase from a DNS provider.
2. A valid SSL certificate.

TODO: [https://codenvy.readme.io/v5.0/docs/security](https://codenvy.readme.io/v5.0/docs/security)

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

## Config: Workspaces 
#### Workspace Requirements
Currently, Codenvy's workspaces launch a tiny rsync-agent that allows the centralized Codenvy server to backup project source code from within each workspace to the central servers. When workspaces are shut off or restarted, the project files are automatically rsync'd back into the workspace. rsync runs at workspace start, stop, and on a scheduler. This allows us to preserve the integrity of your source code if the workspace's runtime containers were to have a failure during operation.

We install rsync into each user's workspace to run as a background service. In this version of Codenvy, your user workspaces requires SSH and rsync to be installed in the base image. If you are connected to the Internet, we install rsync and SSH automtaically. However, if you are doing an offline installation, then your workspace base images need to have this software included.

Some base images, like ubuntu, support this, but others like alpine, do not. If you create custom workspace recipes from Composefiles or Dockerfiles to run within Codenvy, these images must inherit from a base image that has rsync and SSH or you must ensure that these services are installed. If you do not have these services installed, the workspace will not start and provide an error to the user that may cause them to scratch their head.

In the non-container installation version of Codenvy, this requirement does not exist since we install these dependencies onto each host node that is added into the Codenvy cluster. We will be working to package up the rsync agent as a container that is deployed outside of your workspace's runtime. The container will have the dependencies and then this requirement will be removed.

#### Private Registries
#### Mirroring DockerHub
#### Private Repositories
#### Permissions
#### Workspace Limits
You can place limits on how users interact with the system to control overall system resource usage. You can define how many workspaces created, RAM consumed, idle timeout, and a variety of other parameters. See "Workspace Limits" in `CODENVY_CONFIG/codenvy.env`.

## Config: Authentication
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
## Config: Scaling
#### Add-node
#### Remove-node
## Managing
#### Upgrades
#### Upgrading
#### Runbook
#### Monitoring
#### Backup (Backup)
#### Migration
#### Disaster Recovery#### DR page (http://codenvy.readme.io/v5.0/docs/disaster-recovery)
## Reference
#### CLI
#### API
## Architecture
#### Scalability Model
#### Deployment Model
#### Service Architecture













#### Hostname
If you install Codenvy with the defaults, Codenvy is reachable at http://codenvy.onprem. You can change the hostname by using the CLI with codenvy config --hostname=<hostname> . 

## LDAP
Codenvy is compatible with an InetOrgPerson.schema. For other schemas please contact us at info@codenvy.com.

#### LDAP AUTHENTICATION
User authentication is implemented as follows:

1. Search for for user DN according to the provided name. It can be performed in two ways: either by a given DN format, or based on a user search query.
2. To verify the user's password two functions can be used: ldap bind or ldap compare.
3. If username and password match, the LDAP entry is taken and transformed to obtain UserID (this is where synchronization configuration mechanism is applied).
4. Checks if the user with a given ID already exists in the Codenvy database. If it doesn't user is authenticated.

TODO: Add table.

#### Authentication Configuration
TODO: Add table.

#### Connection Configuration
TODO: Add table.

#### SSL Configuration
TODO: Add table.

#### SASL Configuration
TODO: Add table.

#### Synchronizer Terminology
- LDAP storage - third party directory server considered as primary users storage.
- LDAP cache - a storage in Codenvy database, which basically is a mirror of LDAP storage.
- Synchronized user - a user who is present in LDAP cache.
- Synchronization candidate - a user present in LDAP storage matching all the filters and groups, the user who is going to be synchronized.
- Codenvy User - entity in Codenvy API. A user is stored in Codenvy database (PosgreSQL).

#### Synchronization Strategy
The data in the LDAP cache is considered to be consistent as long as the synchronizer does its job. Synchronization itself is unidirectional, requiring only a READ restricted connection to LDAP server.

- If the synchronizer can't retrieve users from LDAP storage, it fails.
- If the synchronizer can't store/update a user in LDAP cache it prints a warning with a reason and continues synchronization.
- If synchronization candidate is missing from LDAP cache, an appropriate User and Profile will be created.
- If synchronization candidate is present in LDAP cache, the User and Profile will be refreshed with data from LDAP storage(replacing the entity in the LDAP cache).
- If LDAP cache contains synchronized users who are missing from LDAP storage those users will be removed by the next synchronization iteration.

There are 2 possible strategies for synchronization:

1. Synchronization period is configured and synchronization is periodical.
2. Synchronization period is set to -1 then synchronization is executed once per server start after the configured initial delay. 

Synchronization can also be triggered by a REST API call:

POST <host>/api/sync/ldap

This won't change the execution of a periodical synchronization, but it is guaranteed that 2 parallel synchronizations won't be executed.

#### Synchronization Configuration
TODO: Add table.

#### Synchronization User Selection Configuration
TODO: Add table.

#### Synchronization Group Configuration
TODO: Add table.

#### Synchronization Data Configuration
TODO: Add table.

#### ACTIVE DIRECTORY EXAMPLE
TODO: Add content.

## Logs and User Data
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

### Microsoft Windows and NTFS
Due to differences in file system types between NTFS and what is commonly used in the Linux world, there is no convenient way to directly host mount Postgres database data from within the container onto your host. We store your database data in a Docker named volume inside your boot2docker or Docker for Windows VM. Your data is persisted permanently. If the underlying VM is destroyed, then the data will be lost.

If you need to backup your Postgres data, run the following command:
`TODO - postgres backup commands`

## Scaling
Codenvy workspaces can run on different physical nodes that are part of a Codenvy cluster managed by Docker Swarm. This is an essential part of managing large development teams, as workspaces are both RAM and CPU intensive operations, and developers do not like to share their computing power when they have a compilation that they want done. So you will want to allocate enough physical nodes to smartly handle the right number of concurrently *running* workspaces, each of which will have a RAM block.

#### Minimum Installation
The Codenvy services running on a server require 2 GB storage and 4 GB RAM for their internal overhead. This applies to both single server, single server with multiple machine servers, and distributed server implementations. The RAM, CPU and storage resources to support your users are additive.

#### Concurrent Workspaces
You need to have machine RAM to support the number of concurrent workspaces open by users within your system. A single user may have multiple workspaces open, but generally use a single workspace. The default workspace size is 1GB RAM, but this can be changed by users. Also, within a single workspace, a user is able to launch additional environments for their projects, which consume additional RAM, though this is uncommon.

For example, at Codenvy, we regularly have 75 concurrently running workspaces, each sized at 16 GB RAM, for a total expectation of 1200 GB of RAM. Our machine nodes have 128 GB RAM per node and we routinely run 10-14 machine nodes to support everyone.

Note that compilation events are usually CPU-heavy and most compilation activities initiated by developers are sent to a single CPU. With quad-core machine nodes, we end up with capacity to support 40-52 concurrent builds in workspaces so that there is no thrashing or blocking. Your team may need more.

You can add as many physical nodes inot a Codenvy cluster, and Codenvy will schedule workspaces for placement on those nodes. You can use the `codenvy add-node` command which generates a utility for you to run on each node that should be added to the cluster. You can also run `codenvy remove-node` to automate the removal of the node from the cluster and the movement of any remaining workspaces onto another node. 

The additional physical nodes must have Docker pre-configured similar to how you have Docker configured on the master node, including any configurations that you add for proxies or an alternative key-value store like Consul. Codenvy generates an automated script that can be run on each new node which prepares the node by installing some dependencies, adding the Codenvy SSH key, and registering itself within the Codenvy cluster.

We have two temporary limitations in in the Docker version of Codenvy that does not exist in the production system that we are currently shipping. First, if you `codenvy remove-node`, we trigger a system-wide restart. Your workspaces and users are not affected. This limitation will be removed shortly. Second, we do a single-node deployment of etcd, which is used as a distributed key-value store. If your users are creating workspaces that use Docker compose syntax, then it is possible that separate containers for a single workspace will be scheduled onto different physical nodes. With our single node implementation of etcd, those containers will not be part of the same network and cannot communicate with one another. Your users will yell at you. To work around this problem, you can configure etcd, zookeeper, or Consul for each of your nodes into a cluster, and then activate "overlay" networking mode within the `codenvy.env` file. We are working to automate distributed etcd configuration so you don't have to worry about this.

## Development Mode
For Codenvy developers that are building and customizing Codenvy from its source repository, there is a development that maps the runtime containers to your source repository. If you are developing in the `http://github.com/codenvy/codenvy` repository, you can turn on development mode to allow puppet configuration files and your local Codenvy assembly to be mounted into the appropriate containers. Dev mode is activated by setting environment variables and restarting (if Codenvy is running) or starting Codenvy (if this is the first run):
```
CODENVY_DEVELOPMENT_MODE="on"
CODENVY_DEVELOPMENT_REPO=<path-codenvy-repo>
```
You must run Codenvy from the root of the Codenvy repository. By running in the repository, the local `codenvy.sh` and `cli.sh` scripts will override any installed CLI packages. Additionally, two containers will have host mounted files from the local repository. During the `codenvy config` phase, the repository's `/modules` and `/manifests` will be mounted into the puppet configurator.  During the `codenvy start` phase, a local assembly from `assembly/onpremises-ide-packaging-tomcat-codenvy-allinone/target/onpremises-ide-packaging-tomcat-codenvy-allinone` is mounted into the `codenvy/codenvy` runtime container.

## CLI Reference
The Codenvy CLI is a self-updating utility. Once installed on your system, it will update itself when you perform a new invocation, by checking for the appropriate version that matches `CODENVY_VERSION`. The CLI saves its version-specific progarms in `~/.codenvy/cli`. The CLI also logs command execution into `~/.codenvy/cli/cli.logs`.  

The CLI is configured to hide most error conditions from the output screen. If you believe that Codenvy or the CLI is starting with errors, the `cli.logs` file will have all of the traces and error output from your executions.

```
Usage: codenvy [COMMAND] [OPTIONS]
    help                                 This help message
    version                              Installed version and upgrade paths
    init [--pull|--force|--offline]      Initializes a directory with a codenvy configuration 
    start [--pull|--force|--offline]     Starts codenvy services
    stop                                 Stops codenvy services
    restart [--pull|--force]             Restart codenvy services
    destroy                              Stops services, and deletes codenvy instance data
    rmi [--force]                        Removes the Docker images for CODENVY_VERSION, forcing a repull
    config                               Generates a codenvy config from vars; run on any start / restart
    add-node                             Adds a physical node to serve workspaces intto the codenvy cluster 
    remove-node <ip>                     Removes the physical node from the codenvy cluster
    upgrade                              Upgrades Codenvy to a new version with data migrations and bakcups
    download [--pull|--force|--offline]  Pulls Docker images CODENVY_VERSION, or installed, codenvy.ver
    backup                               Backups codenvy configuration and data to CODENVY_BACKUP_FOLDER
    restore                              Restores codenvy configuration and data from CODENVY_BACKUP_FOLDER
    offline                              Saves codenvy Docker images into TAR files for offline install
    info [ --all                         Run all debugging tests
           --debug                       Displays system information
           --network ]                   Test connectivity between ${CHE_MINI_PRODUCT_NAME} sub-systems
```

### `codenvy init`
Initializes an empty directory with a Codenvy configuration and instance folder where user data and runtime configuration will be stored. Uses the values you set to `CODENVY_CONFIG` and `CODENVY_INSTANCE` to set these values, then they are set to `$PWD/config` and `$PWD/instance`. The `CODENVY_CONFIG` folder will get a `codenvy.env` file, which is the file you use to configure how Codenvy is configured and run. Other files in this folder are used by Codenvy's configuration system to structure the runtime microservices. 

These variables can be set in your local environment shell before running and they will be respected during initialization and inserted as defaults into `CODENVY_CONFIG/codenvy.ver`:

| Variable | Description |
|----------|-------------|
| `CODENVY_VERSION` | The version of Codenvy to install. You can get a list available with `codenvy version`. We always have `nightly` and `latest` available. |
| `CODENVY_HOST` | The IP address or DNS name of the Codenvy service. We use `codenvy/che-ip` to attempt discovery if not set. |
| `CODENVY_CONFIG` | The folder where a Codenvy config will be placed. The default is `$pwd/config/`. |
| `CODENVY_INSTANCE` | The folder where your Codenvy instance and user data will be placed. The default is `$pwd/instance`. |
| `CODENVY_DEVELOPMENT_MODE` | If `on`, then will mount `CODENVY_DEVELOPMENT_REPO`, overriding the files in Codenvy config and containers. |
| `CODENVY_DEVELOPMENT_REPO` | The location of the `http://github.com/codenvy/codenvy` local clone. |

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
| `--offline` | Loads Docker images from `offline/*.tar` folder during a pre-boot mode of the CLI. Used if you are performing an installation or start while disconnected from the Internet. |

### `codenvy config`
Generates a Codenvy instance configuration using the templates and environment variables stored in `CODENVY_CONFIG` and places the configuration in `CODENVY_INSTANCE`. Uses puppet to generate the configuration files for Codenvy, haproxy, swarm, socat, nginx, and postgres which are mounted when Codenvy services are started. This command is executed on every `start` or `restart`.

If you have set `CODENVY_VERSION` environment variable and it does not match the version that is in `CODENVY_INSTANCE/codenvy.ver`, then the configuration will abort to prevent you from running a configuration for a different version than what is currently installed.

This command respects `--no-force`, `--pull`, `--force`, and `--offline`.

### `codenvy start`
Starts Codenvy and its services using `docker-compose`. If the system cannot find a valid `CODENVY_CONFIG` and `CODENVY_INSTANCE` it will perform a `codenvy init`. Every `start` and `restart` will run a `codenvy config` to generate a new configuration set using the latest configuration. The starting sequence will perform pre-flight testing to see if any ports required by Codenvy are currently used by other services and post-flight checks to verify access to key APIs.  

### `codenvy stop`
Stops all of the Codenvy service containers and removes them.

### `codenvy restart`
Performs a `codenvy stop` followed by a `codenvy start`, respecting `--pull`, `--force`, and `--offline`.

### `codenvy destroy`
Deletes `CODENVY_CONFIG` and `CODENVY_INSTANCE`, including destroying all user workspaces, projects, data, and user database. If you provide `--force` then the confirmation warning will be skipped.

### `codenvy offline`
Saves all of the Docker images that Codenvy requires for `CODENVY_VERSION` into `offline/*.tar` files. Each image is saved as its own file. If the `offline` folder is available on a machine that is disconnected from the Internet and you start Codenvy with `--offline`, the CLI pre-boot sequence will load all of the Docker images in the `offline/` folder.

### `codenvy rmi`
Deletes the Docker images from the local registry that Codenvy has downloaded for `CODENVY_VERSION`.

### `codenvy download`
Used to download Docker images that will be stored in your Docker images repository. This command downloads images that are used by the CLI as utilities, for Codenvy to do initialization and configuration, and for the runtime images that Codenvy needs when it starts.  This command respects `--offline`, `--pull`, `--force`, and `--no-force` (default).  This command is invoked by `codenvy init`, `codenvy config`, and `codenvy start`.

This command is invoked by `codenvy init` before initialization to download the images for a particular `CODENVY_VERSION`. This command uses the singleton `codenvy/version` container which contains the master list of versions and upgrade paths available. The version manifest is saved in `~/.codenvy/manifests`.

### `codenvy version`
Provides information on the current version, the available versions that are hosted in Codenvy's repositories, and if you have a `CODENVY_INSTANCE`, then also the available upgrade paths. `codenvy upgrade` enforces upgrade sequences and will prevent you from upgrading one version to another version where data migrations cannot be guaranteed.

The version manifest is installed when you first perform a `codenvy download`, which is triggered by most services if you have not yet started or initiated the system. The version manifest is saved in `~/.codenvy/manifests`.

### `codenvy upgrade`
Manages the sequence of upgrading Codenvy from one version to another. Run `codenvy version` to get a list of available versions that you can upgrade to.

Do *not* upgrade by wiping your Codenvy images and setting a new `CODENVY_VERSION`. There is a possibility that you will corrupt your system. We have multiple checks that will stop you from starting Codenvy if the configured `CODENVY_VERSION` differs from the one that is in `CODENVY_INSTANCE/codenvy.ver`.  In some releases, we change the underlying database schema model, and we need to run internal migration scripts that transforms the old data model into the new format. The `codenvy upgrade` function ensures that you are upgrading to a supported version where a clean data migration for your existing database can be completed.

### `codenvy info`
Displays system state and debugging information. `--network` runs a test to take your `CODENVY_HOST` value to test for networking connectivity simulating browser > Codenvy and Codenvy > workspace connectivity.

### `codenvy backup`
Tars both your `CODENVY_CONFIG` and `CODENVY_INSTANCE` into files. These files are restoration-ready.

### `codenvy restore`
Restores `CODENVY_CONFIG` and `CODENVY_INSTANCE` to their previous state. You do not need to worry about having the right Docker images. The normal start / stop / restart cycle ensures that the proper Docker images are available or downloaded, if not found.

This command will destroy your existing `CODENVY_CONFIG` and `CODENVY_INSTANCE` folders, so use with caution, or set these values to different folders when performing a restore.

### `codenvy add-node`
Adds a new physical node into the Codenvy cluster. That node must have Docker pre-configured similar to how you have Docker configured on the master node, including any configurations that you add for proxies or an alternative key-value store like Consul. Codenvy generates an automated script that can be run on each new node which prepares the node by installing some dependencies, adding the Codenvy SSH key, and registering itself within the Codenvy cluster.

### `codenvy remove-node`
Takes a single parameter, `ip`, which is the external IP address of the remote physical node to be removed from the Codenvy cluster. This utility does not remove any software from the remote node, but it does ensure that workspace runtimes are not executing on that node. 

## Architecture
![Architecture](https://cloud.githubusercontent.com/assets/5337267/19623944/f2366c74-989d-11e6-970b-db0ff41f618a.png)

## Codenvy.env
The default `CODENVY_CONFIG/codenvy.env` that is installed during `codenvy init` has the set of configurable properties available for system admins. Each variable is documented with an explanation and usually commented out. If you need to set a variable, uncomment it and configure it with your value. You can then run `codenvy config` to apply this configuration to your system. Any `codenvy start` will reapply this configuration.
