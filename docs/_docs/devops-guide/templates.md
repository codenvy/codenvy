---
title: Templates
excerpt: ""
layout: docs
overview: true
permalink: /docs/templates/
---
A code template can be used to instantiate a set of files into the project tree. The code template will appear in the user dashboard when a user attempts to create a new project. Different templates will be shown to the user depending upon the stack that they select using embedded filters. The template lets the initial code tree to be instantiated from a git / subversion URL or from a ZIP file stored on the same computer.

Many of the objects that are configured in a template are also defined as part of the `factory` object. See [Factories](doc:factories) for the reference guide on all properties.
```json  
  {
    "name"         : "web-java-spring\n    "displayName"  : "web-java-spring\n    "path": "/web-java-spring\n    "description"  : "A basic example using Spring servlets. The app returns values entered into a submit form.\n    "projectType"  : "maven\n    "mixins"       : [],
    "attributes"   : {
      "language"   : [ "java" ]
    },
    "modules"      : [],
    "problems"     : [],
    "source"       : {
      "type"       : "git\n      "location"   : "https://github.com/che-samples/web-java-spring.git\n      "parameters" : {}
    },
    "commands": [{
      "name"         : "build\n      "type"         : "mvn\n      "commandLine"  : "mvn -f ${current.project.path} clean install && cp ${current.project.path}/target/*.war $TOMCAT_HOME/webapps/ROOT.war\n      "attributes"   : {
        "previewUrl" : ""
      }
    },
    {
      "name"         : "run tomcat\n      "type"         : "custom\n      "commandLine"  : "$TOMCAT_HOME/bin/catalina.sh run\n      "attributes"   : {
        "previewUrl" : "http://${machine.hostname}:${machine.port.8080}"
      }
    },
    {
      "name"         : "stop tomcat\n      "type"         : "custom\n      "commandLine"  : "$TOMCAT_HOME/bin/catalina.sh run\n      "attributes"   : {
        "previewUrl" : ""
      }
    }],

    "links"          : [],
    "category"       : "Samples\n    "tags"           : [ "maven\ "spring\ "java" ]
  }
```
