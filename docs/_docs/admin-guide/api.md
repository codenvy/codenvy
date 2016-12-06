---
title: API
excerpt: "RESTful API for managing Codenvy installations."
layout: docs
overview: true
permalink: /docs/api/
---
Codenvy runs an installation manager service within a Tomcat server on your installation. The internal puppet services work to keep this server alive.  The service is self-updating and will update itself each time a new Codenvy version is installed. The APIs are available at:

```text  
http://<hostname>/im-api-docs-ui/
```

After Codenvy is installed, you can access this URL to display the list of available APIs. You must be logged in as an admin to gain access to these URLs.
![Installation-Manager-API.png](/docs/images/Installation-Manager-API.png)
