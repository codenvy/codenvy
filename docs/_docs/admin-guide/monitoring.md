---
title: Monitoring
excerpt: "Monitoring tools shipped with Codenvy On-Prem"
layout: docs
overview: true
permalink: /docs/monitoring/
---
[Zabbix](http://www.zabbix.com/) and [Sysdig](http://www.sysdig.org/) the two monitoring tools that are part of Codenvy installation package.

Both Zabbix and Sysdig are not installed by default, and can be enabled in the administration dashboard by changing to `true`:
```text  
install_monitoring_tools = "false"
```
