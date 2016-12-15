---
title: Custom Assemblies
excerpt: "Build your own Codenvy binaries"
layout: docs
permalink: /docs/custom-assemblies/
---
You can create new assemblies for Codenvy. An assembly can contain Eclipse Che extensions, custom stacks, and templates that appear in the dashbuard and IDE.  You create the binary from Codenvy source code and then use the embedded Puppet system to update Codenvy with your new binary.

#### Get Codenvy Source Code

You will need a source code packet that matches the current version of Codenvy that you are running.
```shell  
# Get Codenvy version with the CLI:
codenvy im-version

# Download source code bundle
https://install.codenvycorp.com/codenvy/

# Or clone the source directly from our repository
git clone http://github.com/codenvy/codenvy
git checkout <tag-version>\
```
Navigate to `assembly` module (`onpremises` in pre RC-14 versions). This is a packaging module for On-Premises binaries, i.e. where all the plugins are packaged into a Tomcat. This is where you will add references to your extensions that Maven will pick up when re-compiling all the sub-modules.

#### Build a New Assembly
```shell  
cd assembly
mvn clean install

# This will generate a new binary in a zip file:
cd onpremises-ide-packaging-tomcat-codenvy-allinone\target\
onpremises-ide-packaging-tomcat-codenvy-allinone-${version}.zip\
```
#### Update Codenvy
Copy the generated ZIP file onto the Codenvy server.
```shell  
# On the Codenvy, replace the existing ZIP with the one you created:
cd /etc/puppet/files/servers/prod/aio/
rm onpremises-ide-packaging-tomcat-codenvy-allinone-${version}.zip
cp ~/onpremises-ide-packaging-tomcat-codenvy-allinone-${version}.zip .

# Tell Codenvy to update itself
$ puppet agent -t

# When completed, you should see "Finished catalogue run in X seconds"
```

# Add Custom Eclipse Che Extensions  
You can create bundles that include your own Eclipse Che extensions, stacks and templates. You will need to have build those extensions and compiled them into JAR and ZIP files. There are numerous docs for [writing extensions and customizing stacks](https://eclipse-che.readme.io/docs/introduction-1).

Maven's default enforcement rules require that every extension that you add be listed as a dependency in Codenvy's parent POM. You can skip enforcement by using `-Denforcer.skip=true` argument when performing a `mvn clean install`

Otherwise, you must add your extension as a dependency to Codenvy.  First, add the dependency in the `<dependencyManagement>` section of Codenvy's parent POM located in the root of the repostiory:
```xml  
<dependency>
	<groupId>{groupid-of-extension}</groupId>
	<artifactId>{artifactid-of-extension}</artifactId>
  <version>${project.version}</version>
</dependency>\
```
After adding a dependency, you need to sort maven with `mvn sortpom:sort`.

You do not have to provide the `<version>` tag. Maven will pull the version of your extension that matches the version of Codenvy.

Second, if your extension is an IDE extension, add your extension as a dependency to the IDE:
```xml  
<dependency>
	<groupId>{groupid-of-extension}</groupId>
	<artifactId>{artifactid-of-extension}</artifactId>
  <version>${project.version}</version>
</dependency>\
```

```xml  
<inherits name='{package-name-of-your-ide-extension}'/>\
```
Third, if your extension is a server-side workspace extension, add your extension as a dependency to the workspace master:
```xml  
<dependency>
	<groupId>{groupid-of-extension}</groupId>
	<artifactId>{artifactid-of-extension}</artifactId>
  <version>${project.version}</version>
</dependency>\
```
If you deployed your server-side extension as a REST service, it will then be available at `/api/{your-custom-path}`

Fourth, if your extension is a server-side workspace agent extension, which is designed to run inside workspaces, then add your extension as a dependency to the workspace agent:
```xml  
<dependency>
	<groupId>{groupid-of-extension}</groupId>
	<artifactId>{artifactid-of-extension}</artifactId>
  <version>${project.version}</version>
</dependency>\
```
