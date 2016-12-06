---
title: Installation - Azure Marketplace
excerpt: "Install Codenvy from the Azure marketplace entry"
layout: docs
overview: true
permalink: /docs/installation-azure-marketplace/
---
You can installation Codenvy Enterprise from VMs that are hosted on the [Microsoft Azure Portal](https://azure.microsoft.com/en-us/marketplace/partners/codenvy/codenvy-on-prem/).
# Get a Codenvy VM  
In order to ensure you have the most up-to-date version available you must first install Codenvy onto your VM.
1. Add the [Codenvy Enterprise VM](https://azure.microsoft.com/en-us/marketplace/partners/codenvy/codenvy-on-prem/) to your Azure account using Azure's Resource Manager deployment model.
2. We suggest using SSH public key authentication and deploying into a new resource group.
# Configure Azure VM  
1. From your [Azure homepage](https://portal.azure.com/) select "Resource groups" in the left-hand menu.
![ScreenShot2016-03-01at10.16.46AM.png](/docs/images/ScreenShot2016-03-01at10.16.46AM.png)
2. Choose the resource group into which you deployed the Codenvy VM.
3. In the Resources list click on the network security group.
![ScreenShot2016-02-29at2.31.25PM.png](/docs/images/ScreenShot2016-02-29at2.31.25PM.png)
4. Select "Inbound security rules" from the list.
5. Click on the "Docker" entry.
6. Change the "Destination port range" from `32768 TCP` to `	
32768-65535 TCP`.
7. Save your changes.
8. From the left-hand pane select "Virtual Machines" and the Codenvy VM.
9. Click the DNS name label in the VM details pane.
10. Click "Configuration" on the right-hand pane.
11. Create a DNS name label and hit "Save".
# Install Codenvy onto the VM  
1. SSH into the VM.
2. If you used password authentication with your VM you must execute [additional steps](http://codenvy.readme.io/docs/installation-other#section-pre-install-steps-for-password-authenticated-vms) before installing.
3. At the prompt:
```shell  
bash <(curl -L -s https://start.codenvy.com/install-codenvy)\
```
The installation takes ~15 minutes on a fast Internet connection. You can then access Codenvy at:
```http  
# Loads the login page
http://codenvy.onprem/\
```
The default admin user name is `admin` and password is `password`.  Please do not forget to change the password.
```http  
# Loads the embedded monitor to check service health
http://codenvy.onprem/monit/\
```
The default admin user name is `admin` and password is `zabbix`. You change this password in the Codenvy puppet configuration files.
```http  
# Loads the Swagger UI for browsing the built-in REST API
http://codenvy.onprem/api-docs-ui/\
```

```shell  
# Refresh your shell to add the CLI to your path
source ${HOME}/.bashrc

# Verify that the admin CLI is installed and working
codenvy im-version\
```
