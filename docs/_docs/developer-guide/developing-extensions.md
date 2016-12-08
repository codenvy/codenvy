---
title: Developing Extensions
excerpt: ""
layout: docs
permalink: /docs/developing-extensions/
---
Extensions modify the behavior of the IDE, Codenvy server, or workspace. IDE extensions are client-side. Workspace and Codenvy extensions are server side. Extensions are authored in Java and packaged as JARs. Client-side extensions use GWT to generate cross-browser JavaScript UI.

Codenvy is based upon Eclipse Che, which is an open source cloud IDE, workspace server, and platform for creating developing tooling in the browser. Eclipse Che has a built-in plug-in framework. You can build plug-ins for Codenvy, package them into assemblies, and then upgrade your Codenvy On-Prem installation with these changes.  We will be supporting customization of SaaS workspaces later in 2016.
