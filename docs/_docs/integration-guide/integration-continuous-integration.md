---
title: Continuous Integration
excerpt: ""
layout: docs
permalink: /docs/integration-guide/continuous-integration/
---
Codenvy integrates with CI systems through [Factories](../../integration-guide/workspace-automation). Factories can be added to CI jobs to generate developer workspaces pre-configured with the context of the CI job. For example, a failed CI build email can be customized to include a link to a Codenvy Factory that will generate a workspace already tied to the repo, branch and commit ID that broke the build, simplifying diagnosis.

Additionally, any change a developer makes to the repo will trigger any CI jobs already setup for that repo.

If you'd like to speak to us about custom integrations between Codenvy and CI systems, [please contact us](https://codenvy.com/contact/questions/).

# Integrating Codenvy and Jenkins

## Configuring the Integration

### Set up Plugins  
Go to **Manage Jenkins - Manage Plugins** and install GitHub and Email Extension Template Plugins.
![plugins.png](../../../assets/imgs/plugins.png)

### Create a Jenkins Job  
(Skip this step if you have your Jenkins job already set up.)
Set up a Jenkins Job that matches your project requirements (JDK, Maven, Node.js etc). You may need to install additional plugins that your project requires.

### Configure the Jenkins Job's Post Build Actions  
Once a Jenkins job is set up you need to make sure that a message is sent out when a job succeeds or fails. You should use a **[.jelly template](https://gist.githubusercontent.com/stour/219f30ae3c6aa260ffd5/raw/f83feec8ee08142fe1fca2d1c8c1f9edc52a0e34/html-factory.jelly)** as the default message template. Download it and save to `/var/lib/jenkins/email-templates/html-factory.jelly` on the instance where Jenkins runs.

In your Jenkins job configuration, define the message content as:

`${JELLY_SCRIPT,template="html-factory"}`
![postbuild.png](../../../assets/imgs/postbuild.png)

### Create a Codenvy Factory  
You need to create a Codenvy Factory that uses your target project as source. This Factory will be modified by the plugin and injected into Jenkins job description. See: [Factories](../../integration-guide/workspace-automation).

### Configure Connector, Webhook and Credentials Props  
SSH into your Codenvy instance, navigate to `/home/codenvy` and create 3 `.properties` files:

- first file:  

```text  
jenkins1=jenkins,factory7nfrelk0v8b77fek,http://userName:password@jenkins.codenvy-dev.com:8080,EvgenTestn

[connector-name],[factory-ID],[$protocol://$userName:$password@$jenkinsURL],[jenkins-job-name]\
```   

- second file:  

```text  
username=yourCodenvyUsername
password=yourCodenvyPassword
# username and password should be for the user that created a base Factory\
```   

- third file:  

```text  
webhook1=github,https://github.com/orgName/web-java-spring,factory7nfrelk0v8b77fekn
[webhook-name],[GitHub-URL],[Factory-id]\
```   

### Configure GitHub Webhooks  
In your GitHub repo settings, configure the following webhook:

`http(s)://$codenvyURL/api/github-webhook`


## Test Integration  
Either make a mistake in your code and push changes to a remote repository or configure Jenkins job to purposely fail. You should receive a message from Jenkins with a Factory URL in its body.

Also, your Jenkins job should be updated as follows, i.e. its description will have a Factory URL:
![jenkinsjob.png](../../../assets/imgs/jenkinsjob.png)
