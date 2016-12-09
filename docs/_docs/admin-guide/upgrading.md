---
title: Upgrading
excerpt: "Upgrade Codenvy to a newer version."
layout: docs
overview: true
permalink: /docs/upgrading/
---
Codenvy runs an internal service, the Codenvy Installation Manager, which creates a bridge between Codenvy.com (where new versions may be hosted) and your local installation. If the installation manager is shut down or disconnected from the Internet, your Codenvy installation will continue to operate.

The Codenvy Installation Manager is available as a [RESTful service](doc:api) and as a [CLI](doc:cli).  Many commands are also available to users with admin rights in the browser dashboard. You will be presented with an Administration view in the dashboard. 
```shell  
# Run commands with the user that was used to install Codenvy

# Verify the CLI is installed and working
# Check for new versions hosted at Codenvy
codenvy version

# Download a new Codenvy binary and stage it for installation
codenvy download <version>

# Install the new version (will trigger a maintenance window)
codenvy install codenvy <version>

# If the binary is in another location
codenvy install --binaries=<path> codenvy <version>\
```

# Maintenance Windows  
Some updates trigger different kinds of maintenance windows:
1. Binary and Manifest Changes: Binaries deliver new features and manifest changes alter the system configuration.  Downtime is 1-3 minutes.

2. Binary, Manfiest and Module Changes: This is a major update and only happens between major versions. Downtime is up to 20 minutes.

