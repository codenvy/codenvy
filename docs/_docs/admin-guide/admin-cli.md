---
title: CLI Reference
excerpt: "Manage your Codenvy installation on the command line."
layout: docs
permalink: /docs/admin-guide/cli/
---
The CLI will hide most error conditions from standard out. Internal stack traces and error output is redirected to `cli.log`, which is saved in the host folder where `:/data` is mounted.

## codenvy init  
Initializes an empty directory with a Codenvy configuration and instance folder where user data and runtime configuration will be stored. You must provide a `<path>:/data` volume mount, then Codenvy creates a `instance` and `backup` subfolder of `<path>`. You can optionally override the location of `instance` by volume mounting an additional local folder to `/data/instance`. You can optionally override the location of where backups are stored by volume mounting an additional local folder to `/data/backup`.  After initialization, a `codenvy.env` file is placed into the root of the path that you mounted to `/data`.

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

| Mode | Description |
|------|-------------|
| `--no-force` | Default behavior. Will download an image if not found locally. A local check of the image will see if an image of a matching name is in your local registry and then skip the pull if it is found. This mode does not check DockerHub for a newer version of the same image. |
| `--pull` | Will always perform a `docker pull` when an image is requested. If there is a newer version of the same tagged image at DockerHub, it will pull it, or use the one in local cache. This keeps your images up to date, but execution is slower. |
| `--force` | Performs a forced removal of the local image using `docker rmi` and then pulls it again (anew) from DockerHub. You can use this as a way to clean your local cache and ensure that all images are new. |
| `--offline` | Loads Docker images from `backup/*.tar` folder during a pre-boot mode of the CLI. Used if you are performing an installation or start while disconnected from the Internet. |

The initialization of a Codenvy installation requires the acceptance of our default Fair Source 3 license agreement, which allows for some access to the source code and [usage for up to three people](http://codenvy.com/legal). You can auto-accept the license agreement without prompting for a response for silent installation by passing the `--accept-license` command line option.

You can reinstall Codenvy on a folder that is already initialized and preserve your `/data/codenvy.env` values by passing the `--reinit` flag.

## codenvy config
Generates a Codenvy instance configuration thta is placed in `/data/instance`. This command uses puppet to generate configuration files for Codenvy, haproxy, swarm, socat, nginx, and postgres which are mounted when Codenvy services are started. This command is executed on every `start` or `restart`.

If you are using a `codenvy/cli:<version>` image and it does not match the version that is in `/instance/codenvy.ver`, then the configuration will abort to prevent you from running a configuration for a different version than what is currently installed.

This command respects `--no-force`, `--pull`, `--force`, and `--offline`.

## codenvy start
Starts Codenvy and its services using `docker-compose`. If the system cannot find a valid configuration it will perform a `codenvy init`. Every `start` and `restart` will run a `codenvy config` to generate a new configuration set using the latest configuration. The starting sequence will perform pre-flight testing to see if any ports required by Codenvy are currently used by other services and post-flight checks to verify access to key APIs.  

## codenvy stop
Stops all of the Codenvy service containers and removes them.

## codenvy restart
Performs a `codenvy stop` followed by a `codenvy start`, respecting `--pull`, `--force`, and `--offline`.

## codenvy destroy
Deletes `/docs`, `codenvy.env` and `/instance`, including destroying all user workspaces, projects, data, and user database. If you pass `--quiet` then the confirmation warning will be skipped. Passing `--cli` will also destroy the `cli.log`. By default this is left behind for traceability.

## codenvy offline
Saves all of the Docker images that Codenvy requires into `/backup/*.tar` files. Each image is saved as its own file. If the `backup` folder is available on a machine that is disconnected from the Internet and you start Codenvy with `--offline`, the CLI pre-boot sequence will load all of the Docker images in the `/backup/` folder.

`--list` option will list all of the core images and optional stack images that can be downloaded. The core system images and the CLI will always be saved, if an existing TAR file is not found. `--image:<image-name>` will download a single stack image and can be used multiple times on the command line. You can use `--all-stacks` or `--no-stacks` to download all or none of the optional stack images.

## codenvy rmi
Deletes the Docker images from the local registry that Codenvy has downloaded for this version.

## codenvy download
Used to download Docker images that will be stored in your Docker images repository. This command downloads images that are used by the CLI as utilities, for Codenvy to do initialization and configuration, and for the runtime images that Codenvy needs when it starts.  This command respects `--offline`, `--pull`, `--force`, and `--no-force` (default).  This command is invoked by `codenvy init`, `codenvy config`, and `codenvy start`.

This command is invoked by `codenvy init` before initialization to download the images for the version specified by `codenvy/cli:<version>`.

## codenvy version
Provides information on the current version and the available versions that are hosted in Codenvy's repositories. `codenvy upgrade` enforces upgrade sequences and will prevent you from upgrading one version to another version where data migrations cannot be guaranteed.

## codenvy upgrade
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

## codenvy info
Displays system state and debugging information. `--network` runs a test to take your `CODENVY_HOST` value to test for networking connectivity simulating browser > Codenvy and Codenvy > workspace connectivity.

## codenvy backup
Tars your `/instance` into files and places them into `/backup`. These files are restoration-ready.

## codenvy restore
Restores `/instance` to its previous state. You do not need to worry about having the right Docker images. The normal start / stop / restart cycle ensures that the proper Docker images are available or downloaded, if not found.

This command will destroy your existing `/instance` folder, so use with caution, or set these values to different folders when performing a restore.

## codenvy add-node
Adds a new physical node into the Codenvy cluster. That node must have Docker pre-configured similar to how you have Docker configured on the master node, including any configurations that you add for proxies or an alternative key-value store like Consul. Codenvy generates an automated script that can be run on each new node which prepares the node by installing some dependencies, adding the Codenvy SSH key, and registering itself within the Codenvy cluster.

## codenvy remove-node
Takes a single parameter, `ip`, which is the external IP address of the remote physical node to be removed from the Codenvy cluster. This utility does not remove any software from the remote node, but it does ensure that workspace runtimes are not executing on that node.
