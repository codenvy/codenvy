---
title: Factories - Badged Repos
excerpt: "Simplify contributions for your project with an on-demand developer workspace ready in seconds."
layout: docs
overview: true
permalink: /docs/factories-on-your-repo/
---
A Codenvy Factory can be added to any git repository to provide an on-demand developer workspace ready in seconds. Anyone can get access to the project's code to edit, build, debug and contribute without installing software.

The Codenvy workspace will include the project's source files, the runtime stack, any commands that contributors will need to execute (build, debug, etc...), and a built-in browser IDE. It can even be setup to include the URL of a special Pull Request Factory in the GitHub pull request section when contributions are made.

Our Factory creation wizard makes it simple to create a Factory for any git repo - the process takes about 5 minutes. Once complete you can copy the Factory markdown code to get a "Developer Workspace" button to your README.MD.

Try a [Java Factory](http://beta.codenvy.com/f?id=5use7stej9bi9mxd) now
Or [see it on a GitHub repo](https://github.com/codenvy-demos/spring-petclinic).
# Creating a Workspace Factory  
Start in the Codenvy dashboard (you'll need to have [an account](https://codenvy.com/site/login#)):
1. Click the "Factories" entry in the left-hand navigation menu.
2. Click the "+" button in the top-right of the Factories page.

## Add One or More Projects to the Workspace
3. In the "Select Source" section, enter the git repo URL or connect to your GitHub account to browse all your repos (once the Factory is created you can add [additional projects](http://codenvy.readme.io/docs/factories#section-factory-workspace-object) if required). Note that projects with different languages can be added to the same workspace.

When you hit the "Next" button a Factory is created and you can begin configuring it. The easiest way to configure a Factory is with our wizard. However, if you'd prefer to craft the `factory.json` file yourself we have [documentation](http://codenvy.readme.io/docs/factories#configure) to help you.

## Configure A Workspace Runtime
4. In the "Configure Stacks" section you can choose: one of several common stacks; from a broader set of stacks in the Stack Library; to [add your own Dockerfile](https://eclipse-che.readme.io/docs/stacks#custom-workspace-recipes) for a custom stack.
![selectjava.png](/images/selectjava.png)

#### Built-In Terminal and SSH
For custom runtimes, we suggest using a Codenvy workspace recipe as a base. This ensures users have access to a terminal with full root access and can connect over SSH from their favorite desktop editor or IDE.  

5. After selecting the runtime you can set the RAM allocated to each Factory-generated workspace. If you're an open source project lead you can contact us to [get free 4GB workspaces](http://codenvy.readme.io/v4.0/docs/factories-on-your-repo#free-workspaces-for-oss-projects) for all your contributors.

## Add Commands to Build, Run or Debug
6. Add commands to simplify building and running your project for users - the syntax you enter will be executed in your runtime container when a user runs the command. For more information see our [command docs](https://eclipse-che.readme.io/docs/commands).
7. Add a [preview URL](https://eclipse-che.readme.io/docs/previews) to a command to allow users to see the running app with live changes in their browser.
8. Optionally, you can add actions that execute automatically after your workspace loads - these can also be added directly to the `factory.json` after you complete the wizard. See our [docs](http://codenvy.readme.io/docs/factories#section-factory-ide-object) for guidance.
# Badging a Repo  
Once your Factory is created it can be shared with the URL, however, for public projects it's easier to badge your repository. In the Factory page, click on the "copy markdown" button then paste the code into the README.MD file. View the readme of [this repo](https://github.com/codenvy-demos/spring-petclinic/blob/master/readme.md) to see an example.
# Adding Pull Request Automation and Post-Load Actions  
Once you have your Factory you can further customize it by editing the `factory.json`:
* [Add a simplified pull request panel](http://codenvy.readme.io/docs/factories#section-mixins) and other advanced functionality with "mixins."
* [Modify the policies](http://codenvy.readme.io/docs/factories#section-factory-policies-object) of the Factory.
* [Execute actions](http://codenvy.readme.io/docs/factories#section-factory-ide-object) when the browser IDE opens, closes or when all projects in the workspace are loaded.
# Getting Free Workspaces for an OSS Project  
