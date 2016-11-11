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
| `CODENVY_HOST` | The IP address or DNS name of the Codenvy service. We use `eclipse/che-ip` to attempt discovery if not set. |
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
