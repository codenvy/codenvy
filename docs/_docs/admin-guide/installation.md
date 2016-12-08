---
title: Installation
excerpt: "Install Codenvy in a public cloud or on your own servers."
layout: docs
permalink: /docs/installation/
---
The Codenvy CLI (a Docker image) is downloaded when you first execute `docker run codenvy/cli:<version>`. The CLI downloads other images that run Codenvy and its supporting utilities. The CLI also provides utilities for downloading an offline bundle to run Codenvy while disconnected from the network.

## Nightly and Latest
Each version of Codenvy is available as a Docker image tagged with a label that matches the version, such as `codenvy/cli:5.0.0-M7`. You can see all versions available by running `docker run codenvy/cli version` or by [browsing DockerHub](https://hub.docker.com/r/codenvy/cli/tags/).

We maintain "redirection" labels which reference special versions of Codenvy:

| Variable | Description |
|----------|-------------|
| `latest` | The most recent stable release of Codenvy. |
| `5.0.0-latest` | The most recent stable release of Codenvy on the 5.x branch. |
| `nightly` | The nightly build of Codenvy. |

The software referenced by these labels can change over time. Since Docker will cache images locally, the `codenvy/cli:<version>` image that you are running locally may not be current with the one cached on DockerHub. Additionally, the `codenvy/cli:<version>` image that you are running references a manifest of Docker images that Codenvy depends upon, which can also change if you are using these special redirection tags.

In the case of 'latest' images, when you initialize an installation using the CLI, we encode your `/instance/codenvy.ver` file with the numbered version that latest references. If you begin using a CLI version that mismatches what was installed, you will be presented with an error.

To avoid issues that can appear from using 'nightly' or 'latest' redirectoins, you may:
1. Verify that you have the most recent version with `docker pull eclipse/cli:<version>`.
2. When running the CLI, commands that use other Docker images have an optional `--pull` and `--force` command line option [which will instruct the CLI to check DockerHub](/docs/cli#codenvy-init) for a newer version and pull it down. Using these flags will slow down performance, but ensures that your local cache is current.

If you are running Codenvy using a tagged version that is a not a redirection label, such as `5.0.0-M7`, then these caching issues will not happen, as the software installed is tagged and specific to that particular version, never changing over time.

## Linux:
There is nothing additional you need to install other than Docker.

## Mac:
There is nothing additional you need to install other than Docker.

## Windows:
There is nothing additional you need to install other than Docker.

## Verification:
You can verify that the CLI is working:
```
docker run codenvy/cli
```
The CLI is bound inside of Docker images that are tagged with different versions. If you were to run `codenvy/cli:5.0.0-latest` this will run the latest shipping release of Codenvy and the CLI. This list of all versions available can be seen by running `codenvy version` or browsing the list of [tags available in Docker Hub](https://hub.docker.com/r/codenvy/cli/tags/).

## Proxies
You can install and operate Codenvy behind a proxy:
1. Configure each physical node's Docker daemon with proxy access.
2. Optionally, override the default workspace proxy settings for users if you want to restrict their Internet access.

Before starting Codenvy, configure [Docker's daemon for proxy access](https://docs.docker.com/engine/admin/systemd/#/http-proxy). If you plan to scale Codenvy with multiple host nodes, each host node must have its Docker daemon configured for proxy access. If you have Docker for Windows or Docker for Mac installed on your desktop and installing Codenvy, these utilities have a GUI in their settings which let you set the proxy settings directly.

Please be mindful that your `HTTP_PROXY` and/or `HTTPS_PROXY` that you set in the Docker daemon must have a protocol and port number. Proxy configuration is quite finnicky, so please be mindful of providing a fully qualified proxy location.

If you setup `HTTP_PROXY` or `HTTPS_PROXY` in your Docker daemon, we will also automatially set up a `NO_PROXY` configuration that includes `localhost,127.0.0.1,CODENVY_HOST` where `CODENVY_HOST` is the DNS or IP address that you provided or auto-detected by Codenvy. We recommend that you always add a `NO_PROXY` entry which includes the external IP address of your node and if you are using DNS both the long and short form of the DNS name.

In the initialization phase, we will set our container's environment variables with your proxy settings and also update `codenvy.env` with:
```
CODENVY_HTTP_PROXY_FOR_CODENVY=<YOUR_PROXY_FROM_DOCKER>
CODENVY_HTTPS_PROXY_FOR_CODENVY=<YOUR_PROXY_FROM_DOCKER>
CODENVY_NO_PROXY_FOR_CODENVY=localhost,127.0.0.1,codenvy-swarm,<YOUR_CODENVY_HOST>
CODENVY_HTTP_PROXY_FOR_CODENVY_WORKSPACES=<YOUR_PROXY_FROM_DOCKER>
CODENVY_HTTPS_PROXY_FOR_CODENVY_WORKSPACES=<YOUR_PROXY_FROM_DOCKER>
CODENVY_NO_PROXY_FOR_CODENVY_WORKSPACES=localhost,127.0.0.1,<YOUR_CODENVY_HOST>
```

The last three entries are proxy configuration that will be automatically injected into workspaces created by your users. This gives your users access to the Internet from within their workspaces. You can comment out these entries to disable that access. However, if that access is turned off, then the default templates with source code will fail to be created in workspaces as those projects are cloned from GitHub.com. Your workspaces are still functional, we just prevent the template cloning.

## Offline Installation
We support the ability to install and run Codenvy while disconnected from the Internet. This is helpful for certain restricted environments, regulated datacenters, or offshore installations.

### Save Docker Images
While connected to the Internet, download Codenvy's Docker images:
```
docker run codenvy/cli offline
```
The CLI will download images and save them to `/codenvy/backup/*.tar` with each image saved as its own file. The `/backup` folder will be created as a subdirectory of the folder you volume mounted to `:/codenvy`. You can optionally save these files to a differnet location by volume mounting that folder to `:/backup`. The version tag of the CLI Docker image will be used to determine which versions of dependent images to download. There is about 1GB of data that will be saved.

### Save Codenvy CLI
```
docker save codenvy/cli:<version>
```

### Save Codenvy Stacks
Out of the box, Codenvy has configured a few dozen stacks for popular programming languages and frameworks. These stacks use "recipes" which contain links to Docker images that are needed to create workspaces from these stacks. These workspace runtime images are not saved as part of `codenvy offline`. There are many of these images and they consume a lot of disk space. Most users do not require all of these stacks and most replace default stacks with custom stacks using their own Docker images. If you'd like to get the images that are associated with Codenvy's stacks:
```
docker save <codenvy-stack-image-name> > backup/<base-image-name>.tar
```
The list of images that Codenvy manages is sourced from Eclipse Che's [Dockerfiles repository](https://github.com/eclipse/che-dockerfiles/tree/master/recipes). Each folder is named the same way that our images are stored.  The `alpine_jdk8` folder represents the `codenvy/alpine_jdk8` Docker image, which you would save with `docker save codenvy/alpine_jdk8 > backup/alpine_jdk8.tar`.

### Start Offline
Extract your files to an offline computer with Docker already configured. Install the CLI files to a directory on your path and ensure that they have execution permissions. Execute the CLI in the directory that has the `offline` sub-folder which contains your tar files. Then start Codenvy in `--offline` mode:
```
docker run codenvy/cli:<version> start --offline
```
When invoked with the `--offline` parameter, the Codenvy CLI performs a preboot sequence, which loads all saved `backup/*.tar` images including any Codenvy stack images you saved. The preboot sequence takes place before any CLI configuration, which itself depends upon Docker. The `codenvy start`, `codenvy download`, and `codenvy init` commands support `--offline` mode which triggers this preboot seequence.

## Uninstall
```
# Remove your Codevy configuration and destroy user projects and database
docker run codenvy/cli destroy

# Deletes Codenvy's images from your Docker registry
docker run codenvy/cli rmi
```
