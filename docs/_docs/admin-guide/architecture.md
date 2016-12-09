---
title: Architecture
excerpt: "Codenvy runs anywhere connected to your systems and tool chain. Install Codenvy in a public IaaS cloud, in your own datacenter, or at codenvy.com."
layout: docs
overview: true
permalink: /docs/architecture/
---
Codenvy is a scalable system designed to host high performance workspaces for millions of concurrent developers. A workspace is composed of projects (mapped to a file system) and environments (mapped to a set of machines running as processes). Codenvy scales each component separately to offer high density operations without excessive allocation of resources.
# Scalability Model  

![Slide1.PNG](/images/Slide1.PNG)

# Deployment Model  

![Slide2.PNG](/images/Slide2.PNG)

# Service Architecture  

![Slide3.PNG](/images/Slide3.PNG)

# Single Node Installation  
A single node installation installs all Codenvy services on a single physical node.

## Data Storage

| Project Data Long-Term Storage   | Data Type   
| --- | --- 
| Located At...   | Located At...   
| `/home/codenvy/codenvy-data/fs/`   | User Data   
| Workspace Snapshots   | Built-in PostgreSQL database.   
| Built-in Docker registry.   | Project Data Mounted in Workspace   

## Externally Available Ports (TCP unless otherwise specified)

| Port   | Service   | Notes   
| --- | --- | --- 
| 22   | **SSH**   | External access is required to install Codenvy. After install SSH can be limited to internal availability although doing this will prevent developers from using SSH to access a Codenvy workspace from their desktop IDE (Codenvy web IDE and it's built-in terminal will remain available).   
| 80, 81   | **HAProxy HTTP**   | Websocket access will need to opened for these ports.\n\nThese ports do not have to be opened for HTTPS installs.   
| 443, 444   | **HAProxy HTTPS**   | Websocket access will need to opened for these ports.\n\nThese ports do not have to be opened for HTTP installs.   
| 32768-65535   | **Docker**   | Docker uses the host's ephemeral port range to expose ports for services started in workspace containers. It is possible to limit this range - [contact Codenvy to discuss](https://codenvy.com/contact/questions/).   

## Internal Ports (tpc unless otherwise specified)
The following ports do not need internet accessibility.

| Port   | Service   | Notes   
| --- | --- | --- 
| 389   | **LDAP**   | 2375   
| **Docker**   | 3306   | **MySQL**   
| 5432   | **PostgreSQL**   | 7777   
| **Zabbix Apache HTTPD**   | Only required if monitoring is enabled.   | 8080   
| **Tomcat**   | 8140   | **Puppet Master**   
| **Zabbix Agent**   | 10500   | Only required if monitoring is enabled.   
| Only required if monitoring is enabled.   | **Zabbix Server**   | 10501   
| 23750   | **Docker Swarm**   | 27017   
| **MongoDB**   | 32001, 32101   | **Tomcat JMX**   
|  |  |  
|  |  |  
|  |  |  


# Single Node + Machine Nodes  
This is a scalable version of single node.  You can add additional machine nodes that run workspaces. These machine nodes use the main single node instance as its central coordinator. Adding machine nodes creates requirements for additional ports to open.
![aio.png](/images/aio.png)
## Data Storage

| Data Type   | Located On...   | Located At...   
| --- | --- | --- 
| Project Data Long-Term Storage   | User Data   | Workspace Snapshots   
| `/home/codenvy/codenvy-data/fs/`   | Built-in Docker registry.   | Built-in PostgreSQL database.   
| Project Data Mounted in Workspace   | When a workspace is started the project data is rsync'ed from `/home/codenvy/codenvy-data/fs/` to `/home/codenvy/codenvy-data/che-machines` and then mounted into the running workspace container.   | Machine Node   
| Master Node   | Master Node   | Master Node   

## Master Node
### Externally Available Ports (tpc unless otherwise specified)

| Port   | Service   | Notes   
| --- | --- | --- 
| 22   | **SSH**   | Notes   
| External access is required to install Codenvy. After install SSH can be limited to internal availability although doing this will prevent developers from using SSH to access a Codenvy workspace from their desktop IDE (Codenvy web IDE and it's built-in terminal will remain available).   |    | 80, 81   
| 443, 444   | **HAProxy HTTP**   | **HAProxy HTTPS**   
|    |    | Websocket access will need to opened for these ports.\n\nThese ports do not have to be opened for HTTPS installs.   

### Internal Ports (tpc unless otherwise specified)

| 389   | **LDAP**   | Port   
| --- | --- | --- 
| Service   | Notes   | Notes   
|    | 2375   | **Docker**   
| 5432   | **PostgreSQL**   | 7777   
| **Zabbix Apache HTTPD**   | Only required if monitoring is enabled.   | 8080   
| **Tomcat**   | 8140   | **Puppet Master**   
| 10500   | **Zabbix Agent**   | Only required if monitoring is enabled.   
| Only required if monitoring is enabled.   | 10501   | **Zabbix Server**   
| 23750   | **Docker Swarm**   | **MongoDB**   
| 27017   | 32001, 32101   | **Tomcat JMX**   
| 2379   | **Docker overlay network**   | 4789/udp   
| **Docker overlay network**   | 7946   | 7946/udp   
| **Docker overlay network**   | **Docker overlay network**   |  
|  |  |  
|  |  |  
|  |  |  
|  |  |  

## Machine Nodes (1 .. n)
### Externally Available Ports  (tpc unless otherwise specified)

| Port   | Service   | Notes   
| --- | --- | --- 
| 22   | **SSH**   | After install SSH can be limited to internal availability although doing this will prevent developers from using SSH to access a Codenvy workspace from their desktop IDE (Codenvy web IDE and it's built-in terminal will remain available).   
| 32768-65535   | **Docker**   | Docker uses the host's ephemeral port range to expose ports for services started in workspace containers. It is possible to limit this range - [contact Codenvy to discuss](https://codenvy.com/contact/questions/).   

### Internal Ports  (tpc unless otherwise specified)

| Port   | Service   | Notes   
| --- | --- | --- 
| 2375   | **Docker Swarm**   | **Zabbix Agent**   
| 10050   | 4789/udp   | **Docker overlay network**   
| 7946   | 7946/udp   | **Docker overlay network**   
| **Docker overlay network**   | Only required if monitoring is enabled.   | 8080   
| **Tomcat**   | 8140   | **Puppet Master**   

