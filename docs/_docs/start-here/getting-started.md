---
title: Getting Started
excerpt: ""
layout: docs
overview: true
permalink: /docs/getting-started/
---
With Docker 1.11+ on Windows, Mac, or Linux:
```
$ docker run codenvy/cli start
```
This command will give you additional instructions on how to run the Codenvy CLI while setting your hostname, configuring volume mounts, and testing your Docker setup.

| I am a...   | Consider going to... |
| --- | --- |
| **Developer** who will be using Codenvy for my projects. | [*The User Guide*](doc:environments). This will teach you how to configure and use workspaces so that they bend to your magical programming will. |
| **Developer** who will be creating custom extensions, plug-ins, and assemblies to package Codenvy and Eclipse Che into new tools. | [*The Developer Guide*](doc:rest-api). These documents provide all of our API interfaces, both internal and external. You will need to get comfortable with using the Eclipse Che SDK, and there is extensive additional SDK documentation that will make you an innovation beast. Also, if you are going to place your extensions into a Codenvy On-Prem, check out the additional [Plug-Ins](doc:plug-ins) administrator steps. |
| **DevOps** or **Developer Services** or **Team Lead** who will be integrating Codenvy into my agile tool chain to automate developer bootstrapping or to create a better agile experience for my developers. | [*The DevOps Guide*](doc:factories). We will give you amazing configuration fu (or `foo`, if you like) on how to define and integration on-demand workspaces into every tool. Wherever your developers go, you can create workspaces that embed into their tools that become part of their every day process. |
| **System administrator** who will be managing an organization of accounts at Codenvy SaaS or installing Codenvy On-Prem for my own organization. | [*The Admin Guide*](doc:architecture). With more flexibility than a Swiss Army Knife, you will install, configure, and operate a global Codenvy cloud servicing millions of developers. |

