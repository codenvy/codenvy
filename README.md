# Codenvy
TEST PR BUILDER
https://www.codenvy.com/.  
On-demand developer workspaces to improve agile workflow and automate developer bootstrapping.

![Eclipse Che](https://www.eclipse.org/che/images/hero-home.png "Eclipse Che")

### Workspaces With Runtimes
Workspaces are composed of projects and runtimes. Create portable and moavable workspaces that run anywhere, anytime in the cloud or on your desktop ... [Read More](https://www.eclipse.org/che/features/#new-workspace)

### Codenvy Developer
Portable, shareable developer workspaces for any programming language or framework.

### Codenvy Team
Workspaces integrated with issue managment, version control, and continuous integration.

### Codenvy Enterprise
Workspaces with user permissions, SSO, elasticity and resource management.

### Deployment Types
You can use Codenvy at our cloud, download it for installation, or get a private installation with our managed hosting services.

Follow the [step by step guide](http://codenvy.readme.io/docs/installation-getting-started) to install Codenvy.

### License
Codenvy is licensed under Codenvy Fair Source 5 for the first five users. You can purchase a commercial Codenvy license from sales.

### Dependencies
Codenvy requires CentOS / RHEL 7.1+ as the only initial dependency. Our installation scripts will install an installation manager based upon Java, which then coordinates the installation of Puppet. Puppet then configures the rest of the system with dependencies for Docker, Swarm, and a variety of other packages.

### Clone

```sh
git clone https://github.com/codenvy/codenvy.git
```
If master is unstable, checkout the latest tagged version.

### Build and Run
```sh
cd codenvy
mvn clean install

# A new assembly is placed in:
cd onpremises-ide-packaging-tomcat-codenvy-allinone\target\

# Assembly:
onpremises-ide-packaging-tomcat-codenvy-allinone-${version}.zip

# Update Codenvy's puppet with the new assembly:
cd /etc/puppet/files/servers/prod/aio/
rm onpremises-ide-packaging-tomcat-codenvy-allinone-${version}.zip
cp ~/onpremises-ide-packaging-tomcat-codenvy-allinone-${version}.zip .
puppet agent -t
```
Codenvy will perform a short maintenance window and will be available at the hostname configured by the admin.

### Engage
* **Customize:** [Runtimes, stacks, commands, assemblies, extensions, plug-ins](https://github.com/eclipse/che/blob/master/CUSTOMIZING.md).
* **Support:** [We love to help you](https://codenvy.com/support/).
* **Developers:** Plug-in developers can get API help at [che-dev@eclipse.org](email:che-dev@eclipse.org). 
* **Website:** [codenvy.com](https://codenvy.com).
