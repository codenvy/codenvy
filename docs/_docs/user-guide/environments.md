---
title: Environments
excerpt: ""
layout: docs
permalink: /docs/environments/
---
A workspace is composed of projects (source files) and environments (runtimes). Every workspace has at least one environment that it is bound to. The default environment of a workspace is called the development environment and your workspace projects are mounted or synchronized into that environment.

A workspace may have multiple environments, which provide developers the ability to place projects into different environments for testing, or perhaps to launch environments that provided hosted services, such as a shared database.

See [Eclipse Che: Environments](https://eclipse-che.readme.io/docs/environments) for a detailed explanation.

## Sharing Workspaces

A user with `setPermissions` privileges (See: [Permissions](doc:permissions)) can share a workspace, i.e. grant other users `read, use, run, configure or setPermissions` privileges.

Select a workspace in User Dashboard, navigate to `Share` tab and enter emails of users to share this workspace with (use comma or space as separator if there are multiple emails).
