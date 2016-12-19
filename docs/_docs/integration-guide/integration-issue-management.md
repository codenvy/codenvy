---
tag: [ "codenvy" ]
title: Issue Management
excerpt: ""
layout: docs
permalink: /:categories/issue-management/
---



[Codenvy Factories](../../integration-guide/workspace-automation) can be integrated with nearly any issue mangement system. However, we have developed two plug-ins that allow completely automated Factory creation and update based on changes to JIRA or Microsoft VSTS issue management.

* Codenvy Plug-In for Atlassian JIRA: [Documentation](#codenvy-plug-in-for-atlassian-jira) / [Video](https://www.youtube.com/watch?v=y4wdplYj6qs)
* Codenvy Extension for Microsoft VSTS: [Documentation](#codenvy-extension-for-microsoft-visual-studio-team-services) / [Video]()


# Codenvy Plug-in for Atlassian JIRA  
The Codenvy plug-in for Atlassian JIRA allows anyone to jump directly from an issue in JIRA to a custom, isolated workspace designed to let them edit, built, debug and review the issue.  [This video](https://www.youtube.com/watch?v=y4wdplYj6qs) shows the agile flow from JIRA to Codenvy.

The plug-in for JIRA consists of three parts:
1. **Issue event listener**: Automatically generates [Codenvy Factories](../../integration-guide/workspace-automation) for the develop and review issue fields when an issue in a Factory-enabled project is created.
2. **Custom issue fields**: Display the links to the develop and review workspaces in Codenvy.
3. **Plug-in administration page**: Defines the location of the associated Codenvy instance, username and password.

The Codenvy agile workflow with Atlassian JIRA requires:
- Codenvy with the VCS Factory Plug-In installed.
- Atlassian JIRA issue management system.
- Codenvy Plug-in for Atlassian JIRA
- GitHub or other git-based repo with webhooks for push and pull requests.
![ProductInteractionFlowswithContinousDevelopment(1).svg](../../assets/imgs/ProductInteractionFlowswithContinousDevelopment(1).svg)

The VCS Factory Plug-In updates Codenvy Factories in response to GitHub webhooks for push events and pull request events.


## Installing the Plug-In for JIRA   

The Codenvy plug-in for JIRA is available from the [Atlassian Marketplace](https://marketplace.atlassian.com/plugins/com.codenvy.jira.codenvy-jira-plugin/server/overview).


## Configuring the Plug-In for JIRA   
### Defining the Parent Factory  
1. In Codenvy create a new generic user account that will house the factories created by the plug-in for JIRA (e.g. `jira-user@some-email.com`).
2. Each project in JIRA that you want to be [Factory-enabled](../../integration-guide/workspace-automation) must have a "parent Factory" configured in Codenvy. The parent Factory needs to be named identically to the Key of the JIRA Project it is associated with.

### Connecting Codenvy to JIRA
1. Log into your JIRA instance as an admin.
2. Navigate to JIRA Administration > Add-Ons.
3. Click on Codenvy Administration at the bottom of the left-hand nav.
4. Enter the URL to your Codenvy instance, as well as the Codenvy username and password you created in the previous section.
5. Save your changes.

### Defining Factory-Enabled Projects
1. Log into your JIRA instance as an admin.
2. Navigate to JIRA Administration > Issues.
3. Choose "Custom fields" from the "Fields" section.
3. Add a new custom field by clicking the button on the right.
4. In the dialog box click the "Advanced" option on the left.
5. Choose "Codenvy Develop Field" and click "Next".
6. Type in your custom field name (e.g. "Develop") and click "Create".
7. Associate the field to the JIRA projects you want to be Factory-enabled and click "Update".
9. Create a second custom field.
10. Choose "Codenvy Review Field" and click "Next".
11. Type in your custom field name (e.g. "Review") and click "Create".
12. Associate the field to the JIRA projects you want to be Factory-enabled and click "Update".

### Configuring GitHub Webhooks
1. On your repository's GitHub page, go to Settings > Webhooks & services.
2. Click the "Add webhook" button.
3. In the Payload URL field enter: `http://{your-codenvy-url}/api/github-webhook`.
4. Content Type is application/json.
5. Leave the Secret field empty.
5. Select "Let me select individual events" radio button.
6. Check "Push" and "Pull request" checkboxes.

### Configuring VCS Factory Plug-in with GitHub
1. In `/home/codenvy` create a `github-webhooks.properties` file with the following content for your GitHub webhooks:
```  
# [webhook-name]=[webhook-type],[repository-url],[factory-id];[factory-id];...;[factory-id]
webhook1=github,https://github.com/myorg/myproject,gfn6lgml8wl47rbr
\
```
2. In the same directory, create a `credentials.properties` file and enter the username and password of your JIRA user (e.g. jira-plugin@some-email.com) as below:
```  
username=somebody@somemail.com
password=somepwd\
```

## Testing
At this point Codenvy will automatically generate custom develop and review workspaces for every issues that's created based on the parent Factory associated with the JIRA Project.

Log into JIRA and choose one of the project types that you have Factory-enabled.  Create a new issue. There will be two new labels "Develop" and "Review" each with a link that will load a Codenvy workspace.

## Using the Plug-In for JIRA  
When a developer is ready to start work on an issue they click the Develop link.  They are brought into a developer workspace that is isolated to them and includes project sources and the build / run / debug environments needed for the project.  The definition of the [project's parent Factory](../../integration-guide/workspace-automation) defines pre- and post-load behaviors.
![JIRAplugin-DevelopandReviewinIDE(1).svg](../../assets/imgs/JIRAplugin-DevelopandReviewinIDE(1).svg)
The git webhooks ensure that as the repository changes the JIRA issue Factory is kept up to date. For example, if a branch associated with the Factory is merged then the Factory will be updated to point to the commitID on the branch that was merged-to.

## Plug-In for JIRA User Data Usage and Privacy  
The plug-in requires the URL of Codenvy as well as a Codenvy user's username and password. The plug-in acts exclusively on the "Issue" object in JIRA. No data from that object is stored.

If you have security or privacy questions regarding this plug-in please contact us: https://codenvy.com/contact/questions/

# Codenvy Extension for Microsoft Visual Studio Team Services  
The Codenvy extension for Microsoft Visual Studio Team Services (VSTS) allows anyone to jump directly from an work item in VSTS to a custom, isolated workspace designed to let them edit, build, debug and review the code.  [This video](http://www.screencast.com/users/codenvy-brad/folders/Default/media/9ebfd758-d808-4ab9-9940-61d0c58775a2) shows the agile flow from Microsoft VSTS to Codenvy.

The plug-in processes events from VSTS issue management:
1. **Work Item created events**: Triggers the creation of a work item-specific [Codenvy Factory](../../integration-guide/workspace-automation) and adds it to the "Develop" and "Review" fields in the Codenvy section of the work item detail card.
2. **Git repo events**: Updates the branch and commitID information for the Codenvy Factory based on changes in the git repo.

The Codenvy agile workflow with Microsoft Visual Studio Team Services requires:
- A Codenvy installation with the VCS Factory Plug-In installed.
- A Microsoft Visual Studio Team Services installation with a git-based repo.
- The Codenvy Agile Plugin for VSTS
![AgileWorkflowArchitecture-MicrosoftVSTS-Phase1.png](../../assets/imgs/AgileWorkflowArchitecture-MicrosoftVSTS-Phase1.png)

## Installing the Extension for VSTS   
The Codenvy extension for Microsoft VSTS is available from the [Visual Studio Marketplace](https://marketplace.visualstudio.com/items?itemName=codenvy.codenvy-extension)

## Configuring the Extension for VSTS    
### Creating the Codenvy User for VSTS  
In Codenvy create a new generic user account that will house the factories created by the extension for Microsoft VSTS (e.g. `vsts-user@some-email.com`).

### Setting VSTS Webhooks
1. On VSTS go to:
```  
https://{your-account}.visualstudio.com/{your-collection}/{your-project-name}/_admin/_servicehooks\
```
2. Configure a new webhook for 'Work item created' events.
3. Set the webhook to `http://your-codenvy-url/api/github-webhook`
4. Configure a new webhook for 'Pull request updated' events.
5. Set the webhook to `http://your-codenvy-url/api/github-webhook`

### Setting Properties in Codenvy
1. In `/home/codenvy/credentials.properties`
Include the Codenvy User for VSTS username and password created [above](#section-creating-the-codenvy-user-for-vsts).
```  
username=somebody@somemail.com
password=somepw\
```
2. In `/home/codenvy/vsts-webhooks.properties`
For each VSTS webhook created include:
[webhook-name]=[webhook-type],[api-version],[project-api-url],[username],[password]
```  
# [webhook-name]=[webhook-type],[host],[account],[collection],[api-version],[username],[password]
webhook1=work-item-created,visualstudio,myaccount,DefaultCollection,2.2-preview.1,some-user,some-pw\
```

### VSTS Credentials
The VSTS credentials used must be secondary credentials. Read more about secondary credentials in the [Microsoft documentation](https://www.visualstudio.com/en-us/integrate/get-started/auth/overview).

### Defining the Parent Factory
Each project in JIRA that you want to be [Factory-enabled](../../integration-guide/workspace-automation) must have a "Parent Factory" configured in Codenvy.
1. Create the Parent Factory with the Codenvy VSTS user created [above](#section-creating-the-codenvy-user-for-vsts).
2. Name the Parent Factory identically to the Team Project in VSTS that it will be associated with.

## Testing
Test that Codenvy automatically generates custom develop and review workspaces for every work item that's created based on the parent Factory associated with the VSTS Project.

Log into Microsoft VSTS and choose one of the project types that you have Factory-enabled.  Create a new work item and open its detail card. There will be a new section called "Codenvy" with a link for Developer Workspace and one for Review Workspace.

## Using the Codenvy Extension for Microsoft VSTS  
When a developer is ready to start work on a work item they click the Develop link.  They are brought into a developer workspace that is isolated to them and includes project sources and the build / run / debug environments needed for the project.  The definition of the [project's parent Factory](../../integration-guide/workspace-automation) defines pre- and post-load behaviors.

![VSTSextension-DevelopandReviewinIDE.png](../../assets/imgs/VSTSextension-DevelopandReviewinIDE.png)

The git webhooks ensure that as the repository changes the Microsoft VSTS work item Factory is kept up to date. For example, if a branch associated with the Factory is merged then the Factory will be updated to point to the commitID on the branch that was merged-to.