---
title: Java Class Reference
excerpt: ""
layout: docs
permalink: /docs/dev-java-class-reference/
---
You can generate the JavaDoc for your installation from source. IDE and workspace extensions are written in Java. This is the JavaDoc for the internal class library.
```shell  
# You need the che-core library
git clone http://github.com/codenvy/che-core
git checkout {version-that-matches-your-install}
mvn javadoc:aggregate

# JavaDoc available at:
/che-core/target/site/apidocs/index.html\
```
