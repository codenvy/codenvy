---
title: Usage
excerpt: "Codenvy uses docker to run on Linux, Mac and Windows."
layout: docs
permalink: /docs/usage/
---
## Syntax
```
USAGE:
  docker run -it --rm <DOCKER_PARAMETERS> codenvy/cli:<version> [COMMAND]

MANDATORY DOCKER PARAMETERS:
  -v <LOCAL_PATH>:/data                Where user, instance, and log data saved

OPTIONAL DOCKER PARAMETERS:
  -e CODENVY_HOST=<YOUR_HOST>          IP address or hostname where Codenvy will serve its users
  -v <LOCAL_PATH>:/data/instance       Where instance, user, log data will be saved
  -v <LOCAL_PATH>:/data/backup         Where backup files will be saved
  -v <LOCAL_PATH>:/cli                 Where the CLI trace log is saved
  -v <LOCAL_PATH>:/repo                Codenvy git repo to activate dev mode
  -v <LOCAL_PATH>:/sync                Where remote ws files will be copied with sync command
  -v <LOCAL_PATH>:/unison              Where unison profile for optimzing sync command resides

COMMANDS:
  action <action-name>                 Start action on Codenvy instance
  add-node                             Adds a physical node to serve workspaces intto the Codenvy cluster
  backup                               Backups Codenvy configuration and data to /data/backup volume mount
  config                               Generates a Codenvy config from vars; run on any start / restart
  destroy                              Stops services, and deletes Codenvy instance data
  download                             Pulls Docker images for the current Codenvy version
  help                                 This message
  info                                 Displays info about Codenvy and the CLI
  init                                 Initializes a directory with a Codenvy install
  list-nodes                           Lists all physical nodes that are part of the Codenvy cluster
  offline                              Saves Codenvy Docker images into TAR files for offline install
  remove-node <ip>                     Removes the physical node from the Codenvy cluster
  restart                              Restart Codenvy services
  restore                              Restores Codenvy configuration and data from /data/backup mount
  rmi                                  Removes the Docker images for <version>, forcing a repull
  ssh <wksp-name> [machine-name]       SSH to a workspace if SSH agent enabled
  start                                Starts Codenvy services
  stop                                 Stops Codenvy services
  sync <wksp-name>                     Synchronize workspace with current working directory
  test <test-name>                     Start test on Codenvy instance
  upgrade                              Upgrades Codenvy from one version to another with migrations and backups
  version                              Installed version and upgrade paths
```

In these docs, when you see `codenvy [COMMAND]`, it is assumed that you run the CLI with the full `docker run ...` syntax. We short hand the docs for readability.

## Sample Start
For example, to start the nightly build of Codenvy with its data saved on Windows in C:\tmp:
`docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock -v /c/tmp:/data codenvy/cli:5.0.0-latest start`

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
## Versions
While we provide `nightly`, `latest`, and `5.0.0-latest` [redirection versions](https://github.com/codenvy/codenvy/tree/master/docs#nightly-and-latest) which are tags that simplify helping you retrieve a certain build, you should always run Codenvy with a specific version label to avoid [redirection caching issues](https://github.com/codenvy/codenvy/tree/master/docs#nightly-and-latest). So, running `docker run codenvy/cli` is great syntax for testing and getting started quickly, you should always run `docker run codenvy/cli:<version>` for production usage.

## Volume Mounts
If you volume mount a single local folder to `<your-local-path>:/codenvy`, then Codenvy creates `codenvy.env` (configuration), `/instance` (user data, projects, runtime logs, and database), and `/backup` (data backup).

However, if you do not want your `/instance`, and `/backup` folder to all be children of the same parent folder, you can set them individually with separate overrides.

```
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock
                    -v <local-path>:/data
                    -v <local-path>:/data/instance
                    -v <local-path>:/data/backup
                       codenvy/cli:<version> [COMMAND]    

```

## Hosting
If you are hosting Codenvy at a cloud service like DigitalOcean, set `CODENVY_HOST` to the server's IP address or its DNS. We use an internal utility, `eclipse/che-ip`, to determine the default value for `CODENVY_HOST`, which is your server's IP address. This works well on desktops, but usually fails on hosted servers requiring you to explicitly set this value.

```
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock
                    -v <local-path>:/data
                    -e CODENVY_HOST=<your-ip-or-host>
                       codenvy/cli:<version> [COMMAND]
```
