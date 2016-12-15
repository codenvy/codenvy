---
title: Import
excerpt: ""
layout: docs
permalink: /docs/import/
---
There are several ways to import a project:
* `IDE > Import Project`
* `Dashboard > Create Project`
* Import via Git
* Import from a ZIP file
* Copy a project to Che's projects folder on the local file system
* Open a terminal in the IDE and create files in the `/projects` directory

Private repositories require SSH keys that need to be generated and uploaded to a code hosting server. It is done automatically for GitHub at `Help > Preferences`. Click the GitHub icon and follow instructions.
# From the User Dashboard  
In the user dashboard click the "+" button in the upper-right to enter the New Project creation wizard.
* In the "Select Source" section click the "Import from existing location" radio button
* Enter the URL of your git repo
* Enter your credentials to connect Codenvy to your GitHub account
* Enter the URL of your source code ZIP file

In the "Select Workspace" section select a [ready-to-go stack](../../docs/stacks#section-ready-to-go-stacks) that matches your project's language and runtime or create a [custom stack](../../docs/stacks#custom-stacks-for-che) based on the Eclipse Che open standard.

To import from an SVN repo create a workspace by selecting "Workspaces" in the left-hand nav and hitting the "+" sign in the upper-right. Once your workspace is started use the instructions in the "From the IDE" section below to import from SVN.
# From the IDE  
If starting in the IDE make sure the workspace you're in is compatible with the project you're going to import (e.g. if you're importing a node.js project you'll need a node.js workspace).
* In the IDE menu select *Workspace > Import Project* (you can import multiple projects into a single workspace).
* Select the appropriate source control option on the left
* Fill in the parameters on the right

Once your code is imported you'll have to configure your project.


# Project Configuration  
Codenvy will perform a project "estimation" when a project is being imported in an attempt for it to estimate the project type. The project type will cause the IDE and workspace to inherit special behaviors, such as Java intellisense for maven, or Bower plug-in for JavaScript. You can manually set the project type in `Project > Configuration`.
