# Codenvy 
Cloud workspaces for development teams. One-click Docker environments to create workspaces [with production runtimes](https://codenvy.com/solutions/bootstrapping). [Team onboarding and collaboraiton with workspace automation](https://codenvy.com/product/next-generation) and permissions letting devs sync their desktop IDE or use our gorgeous Eclipse Che IDE. [Workspace platform for DevOps](https://codenvy.com/product/technology) to manage workspaces at scale with programmable and customizable infrastructure.
 
![Eclipse Che](https://www.eclipse.org/che/images/banner@2x.png "Eclipse Che")

### Getting Started
You can run Codenvy [in the public cloud](http://codenvy.io), a private cloud, or install it on any OS that has Docker 1.11+ installed. Codenvy has been tested on many flavors of Linux, MacOS, and Windows. A private Codenvy install allows you to configure LDAP, permissions, Eclipse Che extensions, Jira integration, Jenkins integration and integration to your private toolchain. The [docs will get you going](dockerfiles/init/docs/README.md). Quick start:

```
docker run codenvy/cli:nightly start
```

The `codenvy` repository is where we do development. Your license grants you access to the source code for customization, but you are not able to redistribute the source code or use it in commercial endeavors.

- [Submit bugs and feature requests](http://github.com/codenvy/codenvy/issues) and help us verify them
- Review [source code changes](http://github.com/codenvy/codenvy/pulls)
- [Review the docs](https://github.com/codenvy/codenvy/docs/README.md) and make improvements

### License
Codenvy [is free for 3 users](https://codenvy.com/legal/fair-source/). For additional users, [please purchase](https://codenvy.com/contact/download/) a [Codenvy enterprise license](https://codenvy.com/docs/terms-of-service.pdf) that includes premium support.

### Customiziing 
There are many ways to customize Codenvy out-of-the-box. Codenvy is customized using Eclipse Che including [stacks, templates, commands, IDE extensions, server-side extensions plugins, assemblies, RESTful APIs, and editors](https://github.com/eclipse/che/blob/master/CUSTOMIZING.md). 

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

# Run Codenvy with a custom assembly - volume moutn this codenvy repository
docker run -v /var/run/docker.sock:/var/run/docker.sock -v <path-to-repo>:/repo codenvy/cli start
```

### Engage
* **Support:** [We love to help you](https://codenvy.com/support/).
* **Roadmap:** We maintain [the roadmap](https://github.com/eclipse/che/wiki/Roadmap) on the wiki. 
* **Developers:** Plug-in developers can use [Eclipse Che](http://www.eclipse.org/che). 
