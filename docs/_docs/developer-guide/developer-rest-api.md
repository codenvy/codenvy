---
title: REST API
excerpt: "APIs for the workspace master and agent using Swagger"
layout: docs
permalink: /docs/rest-api/
---
Codenvy has activated Swagger annotations for its embedded APIs. There are APIs that are hosted within the Che server, which we call workspace master APIs for managing workspaces. And there are APIs that are hosted within each workspace, launched and hosted by a workspace agent that is injected into each workspace machine when it boots.  
```http  
# Access the REST API
http://{codenvy-hostname}/swagger/

# On the default installation, this is located at:
http://codenvy/swagger/\
```
Each workspace has its own set of APIs. The workspace agent advertises its swagger configuration using a special URL. You access the workspace agent's APIs through the workspace master. The workspace master connects to the agent, grabs the swagger configuration, and executes it within the workspace master.  You can find the hostname and port number of your workspace in the IDE in the operations perspective. The operations view displays a table of servers that are executing within the currently active workspace. One of those servers will be labeled as the workspace agent and it will display the hostname and port number of the agent.
```text  
http://{workspace-master-host}/swagger/?url=http://{workspace-agent-host}/ide/ext/docs/swagger.json

# Example
http://codenvy/swagger/?url=http://192.168.99.100:32773/ide/ext/docs/swagger.json\
```
## Authentication

There are certain APIs within Codenvy that may require authenticated access. While Codenvy implements a single user profile, other Codenvy implementations may implement an authentication provider that will require having a valid token before making use of the API.

You can use the `auth` REST API to login to a Codenvy system, which will return a token. You then append the token to the end of any REST invocation that you then make.
```text  
# Append this to the end of the URL
?token=<token-id>

# Example:
http://${hostname}/api/workspace/config?token=371028AD9302dkc9347\
```
