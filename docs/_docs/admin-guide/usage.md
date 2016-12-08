<<<<<<< HEAD
---
title: Usage
excerpt: "Codenvy uses docker to run on Linux, Mac and Windows."
layout: docs
permalink: /docs/usage/
---
## Syntax
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

## Sample Start
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
## Versions
While we provide `nightly`, `latest`, and `5.0.0-latest` [redirection versions](https://github.com/codenvy/codenvy/tree/master/docs#nightly-and-latest) which are tags that simplify helping you retrieve a certain build, you should always run Codenvy with a specific version label to avoid [redirection caching issues](https://github.com/codenvy/codenvy/tree/master/docs#nightly-and-latest). So, running `docker run codenvy/cli` is great syntax for testing and getting started quickly, you should always run `docker run codenvy/cli:<version>` for production usage.

## Volume Mounts
If you volume mount a single local folder to `<your-local-path>:/codenvy`, then Codenvy creates `/codenvy/codenvy.env` (configuration), `/codenvy/instance` (user data, projects, runtime logs, and database), and `/codenvy/backup` (data backup).

However, if you do not want your `/instance`, and `/backup` folder to all be children of the same parent folder, you can set them individually with separate overrides.

```
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock
                    -v <local-codenvy-folder>:/codenvy
                    -v <local-instance-path>:/codenvy/instance
                    -v <local-backup-path>:/codenvy/backup
                       codenvy/cli:<version> [COMMAND]    

```

## Hosting
If you are hosting Codenvy at a cloud service like DigitalOcean, set `CODENVY_HOST` to the server's IP address or its DNS. We use an internal utility, `eclipse/che-ip`, to determine the default value for `CODENVY_HOST`, which is your server's IP address. This works well on desktops, but usually fails on hosted servers requiring you to explicitly set this value.

```
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock
                    -v <local-path>:/codenvy
                    -e CODENVY_HOST=<your-ip-or-host>
                       codenvy/cli:<version> [COMMAND]
```
=======
---
title: Usage
excerpt: "Codenvy uses docker to run on Linux, Mac and Windows."
layout: docs
permalink: /docs/usage/
---
## Syntax
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

## Sample Start
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
## Versions
While we provide `nightly`, `latest`, and `5.0.0-latest` [redirection versions](https://github.com/codenvy/codenvy/tree/master/docs#nightly-and-latest) which are tags that simplify helping you retrieve a certain build, you should always run Codenvy with a specific version label to avoid [redirection caching issues](https://github.com/codenvy/codenvy/tree/master/docs#nightly-and-latest). So, running `docker run codenvy/cli` is great syntax for testing and getting started quickly, you should always run `docker run codenvy/cli:<version>` for production usage.

## Volume Mounts
If you volume mount a single local folder to `<your-local-path>:/codenvy`, then Codenvy creates `/codenvy/codenvy.env` (configuration), `/codenvy/instance` (user data, projects, runtime logs, and database), and `/codenvy/backup` (data backup).

However, if you do not want your `/instance`, and `/backup` folder to all be children of the same parent folder, you can set them individually with separate overrides.

```
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock
                    -v <local-codenvy-folder>:/codenvy
                    -v <local-instance-path>:/codenvy/instance
                    -v <local-backup-path>:/codenvy/backup
                       codenvy/cli:<version> [COMMAND]    

```

## Hosting
If you are hosting Codenvy at a cloud service like DigitalOcean, set `CODENVY_HOST` to the server's IP address or its DNS. We use an internal utility, `eclipse/che-ip`, to determine the default value for `CODENVY_HOST`, which is your server's IP address. This works well on desktops, but usually fails on hosted servers requiring you to explicitly set this value.

```
docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock
                    -v <local-path>:/codenvy
                    -e CODENVY_HOST=<your-ip-or-host>
                       codenvy/cli:<version> [COMMAND]
```
>>>>>>> refs/heads/jekyll-docs
