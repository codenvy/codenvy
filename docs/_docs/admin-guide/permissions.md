---
title: Permissions
excerpt: ""
layout: docs
overview: true
permalink: /docs/permissions/
---
Every user in the system is granted a set of permissions that allow or prohibit access to REST API methods. Users and system admins can share permissions or transfer ownership.

Each Codenvy installation has one super user - `admin` with `manageCodenvy, manageUsers, setPermissions, readPermissions`. This super user can grant other users identical permissions, i.e. it is possible that there can be several users administering one Codenvy installation.

Each object has its own set of permissions that can repeat itself across different objects.

## System

| Permissions Set   
| --- 
| Details   

## Workspace

| SET_PERMISSIONS   | READ   
| --- | --- 
| RUN   | USE   
| CONFIGURE   | DELETE   
| Grant other users any of the below permissions   | Read only access to an object. Get a workspace configuration by its ID or name   
| Run a workspace with a selected/default environment   | Access to a running workspace and its resources (terminal, workspace agent)   
| Update workspace configuration (environments, RAM etc)   | Delete a workspace   
| Permissions Set   | Details   

## Recipe

| SET_PERMISSIONS   | READ   
| --- | --- 
| UPDATE   | DELETE   
| Grant other users any of the below permissions   | Get a recipe by its ID   
| Update a recipe   | Delete a recipe   
| Permissions Set   | Details   

## Stack

| SET_PERMISSIONS   | READ   
| --- | --- 
| UPDATE   | DELETE   
| Grant other users any of the below permissions   | Get a stack by its ID   
| Update a stack   | Delete a stack   
| Permissions Set   | Details   

# Access to Objects

When a user interacts with an object - workspace, stack, recipe or factory, the system reads API token and finds out user permissions for a specified object. Further behavior depends on whether or not a user has sufficient permissions for a requested action (read, modify, delete an object, etc).  A user either gets access to the object or 403 Unauthorized error is returned.

# API and Machine Tokens

There are two types of tokens: API token generated upon login and used in all requests to API and machine token that is generated for a workspace agent to communicate with API.

A machine token is generated in 2 cases:

* when a user starts a workspace (the system checks if a user has RUN permissions and then generates a token)
* when an invited user accesses a workspace (a system checks if a user has sufficient permissions to access a workspace and then generates a token)
![tokens.png](/images/tokens.png)
