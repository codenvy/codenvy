---
tag: [ "codenvy" ]
title: Version Control
excerpt: ""
layout: docs
permalink: /:categories/version-control/
---
{% include base.html %}

**Applies To**: Codenvy on-premises installs.

Codenvy is powered by the open Eclipse Che project. You will see references to Eclipse Che and its documenation below.
---

Public or private repositories are used to import projects into workspaces, to use the Git / Subversion menus, and to use and create [Factories]({{base}}/docs/integration-guide/workspace-automation/index.html). Some repository tasks such as git push and access to private repositories require setting up SSH or oAuth authentication mentioned below.

# Using SSH  
Setup of an SSH keypair is done inside each user's workspace. Refer to [Eclipse Che git docs]() for additional information. Setting up SSH keypairs inside a workspace must be done prior to being able to use private repository URLs.

Refer to specific [GitHub]() and [GitLab]() SSH examples on how to upload public keys to these repository providers.

For other git-based or SVN-based repository providers, please refer to their documentation for adding SSH public keys.

# Using oAuth  
## GitLab oAuth
Currently it's not possible for Codenvy to use oAuth integration with GitLab. Although GitLab supports oAuth for clone operations, pushes are not supported. You can track [this GitLab issue](https://gitlab.com/gitlab-org/gitlab-ce/issues/18106) in their issue management system.

## GitHub oAuth
### Setup oAuth at GitHub
1. Register an application in your GitHub account. Refer to [Setup oAuth at GitHub]() for additional information.
2. Update the `codenvy.pp` with secret, ID and callback:
```text  
oauth.github.clientid=yourClientID
oauth.github.clientsecret=yourClientSecret
oauth.github.authuri=https://github.com/login/oauth/authorize
oauth.github.tokenuri=https://github.com/login/oauth/access_token
oauth.github.redirecturis=http://$hostname/wsmaster/api/oauth/callback\
```
After the above steps, execute `puppet agent -t`.

### Using GitHub oAuth
With oAuth setup you can perform all git actions with the repo, including commits and pushes.

# Git and Subversion Workspace Clients
After importing a repository, you can perform the most common Git and SVN operations using interactive menus or through console commands.

**Note: Use of git menu remote push command will not work prior setting up SSH or oAuth authentication.**
![git-menu.png]({{base}}/assets/imgs/git-menu.png)

![svn-menu.png]({{base}}/assets/imgs/svn-menu.png)

# Built-In Pull Request Panel
Within the Codenvy IDE there is a pull request panel to simplify the creation of pull requests for GitHub, BitBucket or Microsoft VSTS (with git) repositories.
