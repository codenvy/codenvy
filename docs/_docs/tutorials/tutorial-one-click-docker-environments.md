---
title: One-Click Docker Environments
excerpt: "Create workspaces with production runtimes containing your source code and dev tools. Choose any architecture - microservices, multi-tier, multi-container, or shared server. We excel at complex topologies."
layout: tutorials
permalink: /tutorials/one-click-docker-environments/
---

# Integrate Your Repo  
You need to generate SSH key to be able to clone private repositories.

For GitHub SSH keys are generated and uploaded automatically at Profile > Preferences > SSH > VCS.

![github-button.png](../../assets/imgs/github-button.png)

For other Git hosting providers SSH key should be generated and manually saved to profile settings of git hosting settings. Find more details on how to do that in the [Eclipse Che Git docs](https://eclipse-che.readme.io/docs/git#section-other-git-hosting-providers).
# Integrate Your Registry  
Eclipse Che workspaces are based upon a Docker image. You can either pull that image from a public registry, like Docker Hub, or a private registry which is managed by yourself. Images in a registry can be publicly visible or private, which require user credentials to access. You can also set up a private registry to act as a mirror to Docker Hub.  And, if you are running Eclipse Che behind a proxy, you can configure the Docker daemon registry to operate behind a proxy.

## Proxy for Docker
If you are installing Eclipse Che behind a proxy and you want your users to create workspaces powered by images hosted at Docker Hub, then you will need to configure the Docker daemon used by Eclipse Che to [operate over a proxy](https://docs.docker.com/engine/admin/systemd/#http-proxy).

## Private Docker Images
When users create a workspace in Eclipse Che, they must select a Docker image to power the workspace. We provide ready-to-go stacks which reference images hosted at the public Docker Hub. You can provide your own images that are stored in a local private registry or at Docker Hub. The images may be publicly or privately visible, even if they are part of a private registry.

### Accessing Private Images
You can configure Che to access private images in a public or private registry. Modify the `che.properties` to configure your private registry:


```shell  
# Docker registry configuration.
# Note that you can configure many registries with different names.
docker.registry.auth.your_registry_name.url=https://index.docker.io/v1/
docker.registry.auth.your_registry_name.username=user-name
docker.registry.auth.your_registry_name.password=user-password
# You can add as many registries as you need, e.g.:
docker.registry.auth.registry1.url
docker.registry.auth.registry2.url
\
```  


Registries added in User Dashboard override registries added to `che.properties`.


## Private Docker Registries
When creating a workspace, a user must reference a Docker image. The default location for images is located at Docker Hub. However, you can install your own Docker registry and host custom images within your organization.

When users create their workspace, they must reference the custom image in your registry. Whether you provide a custom stack, or you have users reference a custom workspace recipe from the dashboard, to access a private registry, you must provide the domain of the private registry in the `FROM` syntax of any referenced Dockerfiles.

```shell
# Syntax
FROM <repository>/<image>:<tag>

# Where repository is the hostname:port of your registry:
FROM my.registry.url:9000/image:latest\
```

To add a private registry, perform steps documented above (either adding  a registry to global configuration or in User Dashboard).
#### Custom Images
To get your custom image into a private registry, you will need to build it, tag it with the registry repository name, and push it into the registry. When tagging images into a private registry, they are always tagged with the fully qualified hostname of the registry that will host them. So it is not uncommon to see an image named `ops.codenvy.org:9000/myimage`.  


# Test with a Default Stack  

# Build a Custom Stack  
## Create a Custom Environment Recipe  
### Single Machine Environment  

### Multi-Machine Environment  


## Create a Custom Stack  


## Add a Custom Code Template

# Add Workspace Automation (Factories)  
### Create the Factory  


### Insert the URL in Your Toolchain  

# Integrate Codenvy with Issue Management  
