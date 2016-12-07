---
title: Installation
excerpt: "Install Codenvy in a public cloud or on your own servers."
layout: docs
overview: true
permalink: /docs/installation/
---
The Codenvy CLI (a Docker image) is downloaded when you first execute `docker run codenvy/cli:<version>` command. The CLI downloads other images that run Codenvy and its supporting utilities. The CLI also provides utilities for downloading an offline bundle to run Codenvy while disconnected from the network.

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
2. When running the CLI, commands that use other Docker images have an optional `--pull` and `--force` command line option [which will instruct the CLI to check DockerHub](https://github.com/codenvy/codenvy/tree/master/docs#codenvy-init) for a newer version and pull it down. Using these flags will slow down performance, but ensures that your local cache is current.

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
