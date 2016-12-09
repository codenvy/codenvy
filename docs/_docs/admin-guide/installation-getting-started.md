---
title: Getting Started
excerpt: "From zero to Codenvy delight in just a few steps."
layout: docs
overview: true
permalink: /docs/installation-getting-started/
---
Codenvy on-demand workspaces improve workflow and automate developer bootstrapping to supercharge the agile ethos. Codenvy uses Docker and Eclipse Che to make tooling, workspaces and environments consistent across teams.

This page is a step-by-step guide to starting with Codenvy Team or Enterprise.

If you are using Codenvy at codenvy.com, please see the DevOps and User Guides for managing teams within our hosted environment.
# How to Get Help  
**Issues:** Sometimes the unexpected happens. If it does, please post a question through [our support page](https://codenvy.com/support/).

**Essential Files:** If you experience an issue please attach [logs and configuration files](http://codenvy.readme.io/docs/installation-troubleshooting#essential-files).

**Documentation:** We put a lot of effort into our [docs](http://codenvy.readme.io/docs/introduction). If there are improvements or errors, we'd love that feedback.
# Installation Types  
**Vagrant:** The quickest way to try Codenvy on any operating system. 

**Server:** The best performance.

**Managed:** A private, hosted installation performed by Codenvy experts on nodes Codenvy purchases for you.

**Microsoft Azure Marketplace:** Codenvy installed on powerful Azure VMs.
# 1. Avoid These Common Installation Gotchas  
**Know Where Stuff Is:** We document [where stuff is downloaded](http://codenvy.readme.io/docs/installation-troubleshooting#where-we-install-stuff) to and where config files live.
 
**Windows/Mac:** VT-X/AMD-v must be enabled on your laptop. You can change this [in the BIOS](https://docs.fedoraproject.org/en-US/Fedora/13/html/Virtualization_Guide/sect-Virtualization-Troubleshooting-Enabling_Intel_VT_and_AMD_V_virtualization_hardware_extensions_in_BIOS.html).

**Windows:** If Vagrant fails to create a VirtualBox VM, the most common reason is [NDIS driver bugs](http://stackoverflow.com/questions/33725779/failed-to-open-create-the-internal-network-vagrant-on-windows10).

**Proxies:** Codenvy must download scripts and binaries from the Internet. Configure [Vagrant's proxy](https://eclipse-che.readme.io/docs/usage-vagrant#installing-behind-a-proxy) and also configure the proxy variable within the Vagrantfile.
# 2. Install Codenvy  
**Vagrant:** First install [VirtualBox](https://www.virtualbox.org/wiki/Downloads) and [Vagrant](https://www.vagrantup.com/downloads.html). Place [Codenvy's Vagrantfile in an empty directory](https://github.com/codenvy/codenvy/blob/master/Vagrantfile).
```text  
# In directory with Vagrantfile
vagrant up

# After installation, add a Codenvy entry to your hosts file.
# This is necessary, Codenvy will fail with http://192.168.56.110.
# Windows:    \Windows\System32\drivers\etc\hosts
# Mac\Linux:  /etc/hosts
192.168.56.110      codenvy.onprem

# Codenvy available at:
http://codenvy.onprem\
```
The Vagrant installer provides a base CentOS 7.2 image and performs a silent, suppressed installation of the latest released version of Codenvy. It takes ~8 minutes after the base VM image is downloaded.

**Sever:** You must first install a [clean CentOS 7.x or RHEL 7.x node](http://codenvy.readme.io/docs/installation#prerequisites). Then install [Che using the bootstrap installer](http://codenvy.readme.io/docs/installation#install).

**Managed: ** [Request a trial](https://codenvy.com/contact/trial/) and we will set up a managed installation for you.

**Microsoft Azure Marketplace:** [Initiate your own install at Microsoft](http://codenvy.readme.io/v4.0/docs/installation-other#microsoft-azure-marketplace).
# 3. Create Workspaces and Projects  
In Codenvy, a workspace is composed of one or more projects (source files) and environments (runtimes). Every workspace has at least one environment that it is bound to. The default environment of a workspace is called the development environment and your workspace projects are mounted or synchronized into that environment.

When creating a new project, you will create a workspace and choose its associated stack, which defines the runtime that the workspace uses.

It's easiest to start with a template:
1. In the User Dashboard, choose "New Project."
2. In the `Select Source` section choose `blank, template, or sample project`.
3. In the `Select Stack` section choose the technology you want to try.
4. In the `Configure Workspace` section choose 2GB of RAM.
5. In the `Select Template` section choose a sample application.
6. Hit the Create Project button.

The project will load in a Docker-based workspace in the IDE. You can edit files then build / run using the command toolbar.
![ScreenShot2016-04-28at10.52.51AM.png](/images/ScreenShot2016-04-28at10.52.51AM.png)
Eclipse Che (which is embedded inside Codenvy) provides [step-by-step-by-step tutorials](https://eclipse-che.readme.io/docs/get-started-with-java-and-che) for getting started with using different technologies such as Java, Wordpress, Node.js, Subversion, and many more.
# 4. Setup Workspace Automation  
Factories provide workspace automation for various developer bootstrapping and agile development workflows. Start using them by [creating a Factory](http://codenvy.readme.io/docs/factories#create) from an existing workspace. You can also [author Factories](http://codenvy.readme.io/docs/factories) to perform a range of functions related to branching, post-load actions, guided tours, and project setup. 
# 5. Connect Codenvy to Issue Management  
You can connect Factories to your agile tools, such as Jenkins, version control, Jira or Microsoft Visual Studio Team Services. This experience blends workspaces into your other tools - [what does this feel like](http://www.screencast.com/users/codenvy-brad/folders/Default/media/9ebfd758-d808-4ab9-9940-61d0c58775a2)? 

Add our plug-ins for issue management and continuous integration to automate portions of the linkage:
- [Extension for Microsoft Visual Studio Team Services](http://codenvy.readme.io/docs/issue-management#codenvy-extension-for-microsoft-visual-studio-team).
- [Plug-in for Atlassian JIRA](http://codenvy.readme.io/docs/issue-management#codenvy-plug-in-for-atlassian-jira).
# 6. Scale Codenvy  
**Add Users**: Get the [team on board](http://codenvy.readme.io/v4.0/docs/user)! The number of users you can add depends on the license you're using - [contact us](https://codenvy.com/contact/questions/) to get a trial or production license.

