---
title: Configuration: Docker
excerpt: ""
layout: docs
overview: true
permalink: /docs/configuration-docker/
---
Codenvy workspaces are based upon a Docker image. You can either pull that image from a public registry, like Docker Hub, or a private registry which is managed by yourself.  Images in a registry can be publicly visible or private, which require user credentials to access. You can also set up a private registry to act as a mirror to Docker Hub.  And, if you are running Codenvy behind a proxy, you can configure the Docker daemon registry to operate behind a proxy.

# Proxy  
If you are installing Codenvy behind a proxy and you want your users to create workspaces powered by images hosted at Docker Hub, then you will need to configure the Docker daemon installed by Codenvy to operate over a proxy.

## During Installation
When you run the initial bootstrap script, you can pass in proxy information.
```shell  
# The '-proxy' curl option allows the 'install-codenvy' script to download.
# These options are passed to the 'install-codenvy' script to let it use a proxy.
--http-proxy=<url-to-proxy>
--https-proxy=<url-to-proxy>

# Or

# These options will be passed into Codenvy configuration to tell Codenvy 
# how its Docker daemon should be configured with a proxy. If --http-proxy and 
# --https-proxy are only set, then these values will inherit from them. 
# Use these specific parameters as overrides.
--http-proxy-for-docker-daemon=<url-to-proxy>
--https-proxy-for-docker-daemon=<url-to-proxy>\
```
## After Installation
You can reconfigure an existing Codenvy installation to add or remove a proxy.
```text  
# Modify the Codenvy configuration file to set a daemon
$http_proxy_for_docker_daemon=<url-to-proxy>

# In your shell run Puppet to have the changes take affect:
puppet agent -t\
```

# Private Docker Images  
When users create a workspace in Codenvy, they must select a Docker image to power the workspace. We provide ready-to-go stacks which reference images hosted at the public Docker Hub. You can provide your own images that are stored in a local private registry or at Docker Hub. The images may be publicly or privately visible, even if they are part of a private registry.

## Accessing Private Images
You can configure Codenvy to have a system-wide authenticated access to a Docker image registry. Modify the Codenvy properties with the following. If no server is provided, Docker will default to authenticating at Docker Hub.
```text  
# To add credentials please use following template:
# $docker_registry_credentials = "registry1.url=my-private-registry1.com:5000
# registry1.username=corp_user1
# registry1.password=corp_pass1
# registry2.url=my-private-registry2.com:5000
# registry2.username=corp_user2
# registry2.password=corp_pass2"
# 
# Please take a note that this is multiline variable separated by new line.
# You can add as many custom registries as you want. By default it is empty. 
# Group registries using the registry prefix.

# Example
$docker_registry_credentials = "registry1.url=my-private-registry.com:5000
registry1.username=myuser
registry1.password=mypass"

# In your shell run Puppet to have the changes take affect:
puppet agent -t
```

# Private Docker Registries  
When creating a workspace, a user must reference a Docker image. The default location for images is located at Docker Hub. However, you can install your own Docker registry and host custom images within your organization.

When users create their workspace, they must reference the custom image in your registry. Whether you provide a custom stack, or you have users reference a custom workspace recipe from the dashboard, to access a private registry, you must provide the domain of the private registry in the `FROM` syntax of any referenced Dockerfiles.
```text  
# Syntax
FROM <repository>/<image>:<tag>

# Where repository is the hostname:port of your registry:
FROM my.registry.url:9000/image:latest\
```

#### Custom Images
To get your custom image into a private registry, you will need to build it, tag it with the registry repository name, and push it into the registry. When tagging images into a private registry, they are always tagged with the fully qualified hostname of the registry that will host them. So it is not uncommon to see an image named `ops.codenvy.org:9000/myimage`.  


# Custom Dockerfiles  
Within Codenvy, your workspaces are powered by a set of runtime environments. The default runtime is Docker. You can provide custom Dockerfiles or images that you author, which are used to power the workspaces used by your users.

You can:
1. Create a custom ready-to-go stack, which has a reference to your custom image and registry. Or:
2. Users can create a custom recipe when creating a workspace that references your registry.

## Provide Users Your Own Stack With Custom Dockerfile Recipe
You can add a new stack in Codenvy using the [Stacks API](https://eclipse-che.readme.io/docs/stacks-1).
```curl  
# Authenticate on Codenvy
curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ "username": "your@email.com\ "password": "something" }' https://your.codenvy.install.com/api/auth/login

# Create a new stack
curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ "name": "Java-Node\ "tags": [ "Java\ "JDK\ "Maven\ "Tomcat\ "Node" ], "creator": "ide\ "source": { "type": "image\ "origin": "codenvy/debian_jdk8_node" }, "description": "Stack with JDK 8, Maven, Tomcat and Node.\ "scope": "general\ "workspaceConfig": { "name": "default\ "commands": [ { "name": "Java :: build\ "type": "mvn\ "attributes": {}, "commandLine": "mvn clean install -f ${current.project.path}" }, { "name": "Node :: install\ "type": "custom\ "attributes": {}, "commandLine": "cd ${current.project.path} && npm install" } ], "projects": [], "environments": [ { "name": "default\ "machineConfigs": [ { "name": "default\ "type": "docker\ "limits": { "ram": 0 }, "source": { "location": "stub\ "type": "dockerfile" }, "dev": false, "servers": [], "envVariables": {}, "links": [] } ] } ], "defaultEnv": "default\ "links": [] }, "components": [ { "name": "JDK\ "version": "1.8.0_45" }, { "name": "Maven\ "version": "3.2.2" }, { "name": "Tomcat\ "version": "8.0.24" }, { "name": "NodeJS\ "version": "2.9.0" } ] }' https://your.codenvy.install.com/api/stack
```
Once added the stack will be immediately available to the user in Codenvy. The API also provides convenient services to list, update or delete existing stacks.

## Users Create Custom Stack
When creating a workspace within Codenvy, the user can select custom stack in the user dashboard. Your users can paste Dockerfile syntax which will be used to create a Docker image that is then used to create a runtime container for your workspace. The Dockerfile can reference base images at DockerHub or at a private registry.

## Privileged Mode
By default, Codenvy workspaces powered by a Docker container are not configured with Docker privileged mode. Privileged mode is necessary if you want to enable certain features such as Docker in Docker. There are many security risks to activating this feature - please review the various issues with blogs posted online.  

```shell  
# Activate your Codenvy installation with priviliged mode using CLI
codenvy config machine_docker_privilege_mode true\
```

# Mirroring Docker Hub  
If you are running a private registry internally to your company, you can [optionally mirror Docker Hub](https://docs.docker.com/registry/mirror/). Your private registry will download and cache any images that your users reference from the public Docker Hub. Codenvy needs to be told that you have a mirror so that we can configure the Docker daemon to send image pulls to the mirror instead of Docker Hub. 

## During Installation
When you run the initial bootstrap script, you can pass in the mirror location.
```shell  
# If you have an internal Docker registry which is mirroring and caching Docker Hub
# you can have the Codenvy Docker daemon use this local mirror for grabbing global
# Docker repository images.
--docker-registry-mirror=<url-to-registry>\
```
## After Installation
You can reconfigure an existing Codenvy installation to add or remove a mirror.
```text  
$docker_registry_mirror=<url>

# In your shell run Puppet to have the changes take affect:
puppet agent -t\
```
