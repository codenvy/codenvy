---
title: Factories
excerpt: "Automation for generating workspaces using URLs, simplify contributions for your project with an on-demand developer workspace ready in seconds, and launch walk-me style guided tours in workspaces created by Factories"
layout: docs
permalink: /docs/workspace-automation/
---
# Factories  

A Factory is a template used to generate new or open existing workspaces with a consumer-friendly URL. Factories can be used to clone existing workspaces or with recipes to repeatedly generate consistent workspaces.
### Try  


| Description   | Factory URL   | Configuration   
| --- | --- | ---
| Clone a public Codenvy workspace   | https://codenvy.com/f?id=s38eam174ji42vty   | [View](https://codenvy.readme.io/v4.0/docs/factories?#section-clone-a-public-codenvy-workspace)   


### Definitions  


| Term   | Definition   
| --- | ---
| **Factory**   | A URL that, when clicked, generates a new or loads an existing workspace, and then onboards the user into that workspace.   
| **Factory Configuration**   | A JSON object that defines the rules and behavior for how the Factory should work.   
| **Workspace Configuration**   | A JSON object that defines the contents and structure of a workspace. A workspace configuration is used within a Factory Configuration to define the workspace to be generated.   
| **Owner**   | The user that authored a Factory. An owner can choose to have the resources consumed by workspaces generated from the Factory taken from the owner's account or the acceptor's.   
| **Acceptor**   | Any user that clicks on a Factory URL to open or generate a workspace. The acceptor is made a member or owner of the workspace depending upon the configuration.   
| **Named Workspace**   | A workspace bound to a user and is destroyed when a workspace administrator  destroys it.   
| **Temporary Workspace**   | A workspace that has a limited lifespan. Codenvy destroys temp workspaces when they are idle.   


### Use  
Factories can be invoked in mutliple ways. You can replace the `codenvy.com` domain with the hostname of any Codenvy on premises installation..

| Approach   | Example   
| --- | ---
| https://codenvy.com/f?id=s38eam174ji42vty\nhttps://codenvy.com/factory?id=s38eam174ji42vty   | Example   
|    | https://codenvy.com/f?user=tylerjewell&name=starwars\nhttps://codenvy.com/factory?user=tylerjewell&name=starwars\nhttps://codenvy.com/factory/tylerjewell/starwars   
|    | http://beta.codenvy.com/f?url=https://github.com/eclipse/che\nhttp://beta.codenvy.com/f?url=https://github.com/eclipse/che/commits/language-server\nhttp://beta.codenvy.com/f?url=https://gitlab.com/benoitf/simple-project   
|    | https://codenvy.com/ws/eclipseche/tylerjewell/factory   

Once the Factory is executed, it either loads an existing workspace or generates a new one, depending upon the Factory configuration.  The name of the workspace is determined within the Factory configuration and its name becomes part of the URL access.

| Generated Workspace   | Example   
| --- | ---
| https://codenvy.com/ws/car   | https://codenvy.com/ws/car/tylerjewell   
| https://codenvy.com/ws/car-1/tylerjewell   | https://codenvy.com/f?user=tylerjewell&name=starwars\nhttps://codenvy.com/factory?user=tylerjewell&name=starwars\nhttps://codenvy.com/factory/tylerjewell/starwars   
|    | http://beta.codenvy.com/f?url=https://github.com/eclipse/che\nhttp://beta.codenvy.com/f?url=https://github.com/eclipse/che/commits/language-server\nhttp://beta.codenvy.com/f?url=https://gitlab.com/benoitf/simple-project   


### Create  
You can create Factories that are saved with a unique hash code in the dashboard. Navigate to the `Factories` page and hit the `+` button in the top-right. You can create a Factory with a pretty name in the dashboard or by invoking a URL within your workspace.  If you generate a Factory template using your workspace URL, your Factory inherits the existing definition of your workspace.

| Create New Factory   | Example   
| --- | ---
| https://codenvy.com/ws/eclipseche/tylerjewell/factory/starwars?save   | Create a new Factory from the dashboard\n`Dashboard > Factories > Create`   
| https://codenvy.com/f?id=s38eam174ji42vty   | Create on-demand URL Factory by specifying the remote URL In that case the configuration may be stored inside the repository.   
| http://beta.codenvy.com/f?url=https://github.com/eclipse/che   | http://beta.codenvy.com/f?url=https://github.com/eclipse/che\nhttp://beta.codenvy.com/f?url=https://github.com/eclipse/che/commits/language-server\nhttp://beta.codenvy.com/f?url=https://gitlab.com/benoitf/simple-project   

You can also author a Factory from scratch using a `factory.json` file and then generating a Factory URL using our CLI or API.
### URL Factories  
URL factories are working with github and gitlab repositories. By using URL factories, the project referenced by the URL is imported.

URL can include a branch or a subfolder. Here is an example of url parameters:
- ?url=https://github.com/eclipse/che che will be imported with master branch
- ?url=https://github.com/eclipse/che/tree/4.2.x che is imported by using 4.2.x branch
- ?url=https://github.com/eclipse/che/tree/4.2.x/dashboard subfolder dashboard is imported by using 4.2.x branch
###### URL
}  

#### URL Factories customization

By default, imported project will be configured as blank project type and runtime will be a default image allowing to start the workspace agent.
There are 2 ways of customizing the runtime and configuration

##### Customizing only the runtime
by providing a `.codenvy.dockerfile` inside the repository, Codenvy URL factory will use this dockerfile for the workspace agent runtime.

##### Customizing the project and runtime
by providing a `.codenvy.json` file inside the repository, Codenvy URL factory will configure the project and runtime by using this configuration file.
###### .codenvy.json
The json is using the json configuration described below. The main difference is that you can skip the project source configuration as Codenvy factory is able to specify it automatically.  


###### override
When a `.codenvy.json` file is stored inside the repository, `.codenvy.dockerfile` content is ignored as the workspace runtime configuration is defined inside the JSON file.  


### Configure  
A Factory configuration is a JSON snippet either stored within Codenvy or as a `factory.json` file. You can create Factories within the IDE using our URL syntax, within the dashboard, or on the command line with the API and CLI.
```json  
factory : {
  v         : 4.0,            // Version of configuration format
  workspace : {},             // Identical to workspace:{} object for Eclipse Che
  policies  : {},             // (Optional) Restrictions that limit behaviors
  ide       : {},             // (Optional) Trigger IDE UI actions tied to workspace events
  creator   : {},             // (Optional) Identifying information of author
  button    : {}              // (Optional) Style dynamic button for Factory URL
}\
```
The `factory.workspace` is identical to the `workspace:{}` object for Eclipse Che and contains the structure of the workspace. Learn more about [the workspace JSON object](https://eclipse-che.readme.io/docs/workspace).

Codenvy maintains object compatibility with the workspace definition from Eclipse Che. You can export Eclipse Che workspaces and then reuse the workspace definition within a Factory. Codenvy workspaces are composed of 0..n projects, 0..n environments which contain machine stacks to run the code, 0..n commands to perform against the code, and a type.

The `factory.policies`, `factory.ide`, `factory.creator`, and `factory.button` objects are unique to Factories. They provide meta information to the automation engine that alter the presentation of the Factory URL or the behavior of the provisioning.

A mixin adds additional behaviors to a project as a set of new project type attributes.  Mixins are reusable across any project type. You define the mixins to add to a project by specifying an array of strings, with each string containing the identifier for the mixin.  For example, `"mixins" : [ "tour", "pullrequest" ]`.


| Mixin ID   | Description   
| --- | ---
| `pullrequest`   | Enables pull request workflow where Codenvy handles local & remote branching, forking, and pull request issuance. Pull requests generated from within Codenvy have another Factory placed into the comments of pull requests that a PR reviewer can consume. Adds contribution panel to the IDE. If this mixin is set, then it uses attribute values for `project.attributes.local_branch` and `project.attributes.contribute_to_branch`.   

The `pullrequest` mixin requires additional configuration from the `attributes` object of the project.  If present, Codenvy will use the project attributes as defined in the Factory. If not provided, Codenvy will set defaults for the attributes.

Learn more about other mixins, on [`project : {}` object for Eclipse Che](https://eclipse-che.readme.io/v4.4/docs/workspace#section-projects-object)

##### Pullrequest Attributes
Project attributes alter the behavior of the IDE or workspace.

Different Eclipse Che and Codenvy plug-ins can add their own attributes to affect the behavior for the system.  Attribute configuration is always optional and if not provided within a Factory definition, the system will set itself.

| Known Attribute   | Description   
| --- | ---
| `local_branch`   | `contribute_to_branch`   
| Used in conjunction with the `pullrequest` mixin. If provided, the local branch for the project is set with this value. If not provided, then the local branch is set with the value of `project.source.parameters.branch` (the name of the branch from the remote).  If `local_branch` and `project.source.parameters.branch` are both not provided, then the local branch is set to the name of the checked out branch.   | Name of the branch that a pull request will be contributed to. Default is the value of `project.source.parameters.branch`, which is the name of the branch this project was cloned from.   

 Here is a snippet that demonstrates full configuration of the contribution mixin.
```json  
factory.workspace.project : {
  "mixins"     : [ "pullrequest" ],

  "attributes" : {
    "local_branch"         : [ "timing" ],
    "contribute_to_branch" : [ "master" ]
  },

  "source" : {
    "type"       : "git\n    "location"   : "https://github.com/codenvy/che.git\n    "parameters" : {
      "keepVcs" : "true"
    }
  }
}
```
#### factory.policies Object
Policies are a way to send instructions to the automation engine about the number of workspaces to create and their meta data such as lifespan, resource allocation, and chargeback location.
```json  
factory.policies : {
  type      : [named | temp],  // Default = named        
  location  : [acceptor |      // Default = acceptor. Where workspace lives
               owner],    
  resources : {},              // Resource grant for newly created workspace
  referer   : STRING,          // Works only for clients from referer
  since     : EPOCHTIME,       // Factory works only after this date
  until     : EPOCHTIME,       // Factory works only before this date
  create     : [perClick |      // Create one workpace per click, user or account
               perUser]     
}\
```

```json  
factory.policies.resources : {
  ram : INTEGER                // RAM in MB for workspace default environment
}\
```
##### Longevity
`factory.policies.type` sets a newly created worskpace to be either named or temporary. A named workspace is given a persistent name and is only destroyed when a workspace administrator or owner destroys the workspace. It is essentially, persistent.  A temporary workspace will be destroyed by Codenvy based upon an internally configured policy. The default temporary workspace policy is that the workspace will be destroyed when idle for 10 minutes.  

##### Location and Chargeback
`factory.policies.location` determines the user that will be charged the resources consumed by any workspace created by this Factory.  If `acceptor`, the person who clicks on the Factory will be charged for resource consumption in any chargeback model. If `owner` the person who authors the Factory will be charged for any resource consumption.

It is important to note that all workspaces are internally assigned to the user who clicked on the Factory that generated the workspace.  The workspace is not physically owned by the `owner`.  Codenvy just allocates the resource billing to the `owner`. The user that represents `owner` in this configuration can disable resources and effectively, disable the workspace for the user that originally clicked on the Factory.

##### Authentication
Any user that clicks on a Factory URL must have a Codenvy account and be authenticated if the Factory configuration has `type : named` or `location : acceptor`. Both of these properties require Codenvy to understand either a permanent location or a user name in order to generate the workspace. Any user that is not authenticated will be presented a login screen after they click on the Factory URL.  Users without an account can create one using the same dialog.  

##### Anonymous Factories
If a Factory is configured as `type : temp` and `location : owner`, then authentication is not required. Codenvy generates a temporary workspace and a temporary user.  This allows Factory authors to generate hack workspaces for temporary usage that simplify access for those people who do not want to take the time to create an account.

##### Limitations
You can use `since : EPOCHTIME`, `until : EPOCHTIME` and `referer` as a way to prevent the Factory from executing under certain conditions.  `since` and `until` represent a valid time window that will allow the Factory to activate. For example, instructors who want to create an exercise that can only be accessed for two hours could set these properties.  The `referer` will check the hostname of the acceptor and only allow the Factory to execute if there is a match.

##### Multiplicity
How many workspaces should be created?  If `count : perClick` then every click of the Factory URL will generate a different workspace, each with its own identifier, name and resources.  If `count : perUser`, then exactly one workspace will be generated for each unique user that clicks on the Factory URL. If the workspace has previously been generated, we will reopen the existing workspace and place the user into it.

#### factory.ide Object
```json  
factory.ide.{event} : {        // {event}: onAppLoaded, onProjectsLoaded, onAppClosed
  actions : [{}]               // List of IDE actions to execute when event triggered
}

factory.ide.{event}.actions : [{
  id         : String,         // Action for IDE to perform when event triggered
  properties : {}              // Properties to customize action behavior
}]\
```
You can instruct the Factory to invoke a series of IDE actions based upon events in the lifecycle of the workspace.
* `onAppLoaded` - triggered when the IDE is loaded
* `onProjectsLoaded` - triggered when the workspace and all projects have been activated
* `onAppClosed` - triggered when the IDE is closed

This is an example that associates a variety of actions with all of the events.
```json  
"ide" : {  
  "onProjectsLoaded" : {            // Actions triggered when a project is opened
    "actions" : [{  
      "id" : "openFile\            // Opens a file in editor. Open addl files by repeating
      "properties" : {              // Specifies which file to open (include project name)
        "file" : "/my-project/pom.xml"
      }
    }, {  
      "id" : "findReplace\         // Find and replace values in source code
      "properties" : {  
        "in"          : "(pom\\.xm.*)|(test\\..*)\  // Which files?
        "find"        : "GROUP_ID\                  // What to replace?
        "replace"     : "Codenvy\                   // Replace with?
        "replaceMode" : "text_multipass"
      }
    }, {  
      "id" : "runCommand\          // Launch command after IDE opens
      "properties" : {    
        "name" : "MCI"
      }
    }
  ]},

  "onAppLoaded" : {                 // Actions to be triggered after IDE is loaded
    "actions" : [{  
      "id" : "openWelcomePage\     // Show a custom welcome panel and message
      "properties" : {  
        "authenticatedContentUrl"    : "http://media.npr.org/images/picture-show-flickr-promo.jpg\n        "authenticatedIconUrl"       : "https://codenvy.com/wp-content/uploads/2014/01/icon-android.png\n        "authenticatedTitle"         : "Welcome, John\n        "authenticatedNotification"  : "We are glad you are back!\n        "nonAuthenticatedContentUrl" : "http://media.npr.org/images/picture-show-flickr-promo.jpg\n        "nonAuthenticatedIconUrl"    : "https://codenvy.com/wp-content/uploads/2014/01/icon-android.png\n        "nonAuthenticatedTitle"      : "Welcome, Anonymous"
      }
    }
  ]},

  "onAppClosed" : {                 // Actions to be triggered when IDE is closed
    "actions" : [{  
      "id" : "warnOnClose\         // Show warning when closing browser tab
    }]
  }
}
```
Each event type has a set of actions that can be triggered. There is no ordering of actions executed when you provide a list; Codenvy will asynchronously invoke multiple actions if appropriate. Some actions can be configured in how they perform and will have an associated `properties : {}` object.

##### onProjectsLoaded Event

| Action   | Properties?   | Description   
| --- | --- | ---
| `runCommand`   | Yes   | Specify the name of the command to invoke after the IDE is loaded. Specify the commands in the `factory.workspace.commands : []` array.   
| `openFile`   | Yes   | Open project files as a tab in the editor.   
| `findReplace`   | Yes   | Find and replace text in source files with regex.   

##### onAppLoaded Event

| Action   | Properties?   | Description   
| --- | --- | ---
| `openWelcomePage`   | Yes   | Customize the content of the welcome panel when the workspace is loaded.   

##### onAppClosed Event

| Action   | Properties?   | Description   
| --- | --- | ---
| `warnOnClose`   | No   | Opens a warning popup when the user closes the browser tab wtih a project that has uncommitted changes. Requires `project.parameters.keepVcs` to be `true`.   

###### Action: Open File
This action will open a file as a tab in the editor. You can provide this action multiple times to have multiple files open. The file property is a relative reference to a file in the project’s source tree. The `file` parameter is the relative path within the workspace to the file that should be opened by the editor. Note that projects are located in the workspaces `/projects` folder.
```json  
{  
  "id" : "openFile\n  "properties" : {  
    "file" : "/my-project/pom.xml"
  }
}
```
###### Action: Find / Replace Values After Project Cloning
If you create a project from a factory, you can have Codenvy perform a find / replace on values in the imported source code after it is imported into the project tree. This essentially lets you parameterize your source code.

Parameterizing source code allows you to create projects whose source code will be different for each click on the Factory. The most common use of this is inserting a developer-specific key into the source code. Each developer has their own key which is known to you. That key is inserted as the replacement variable and then inserted into the source code when that user invokes the Factory URL.

Parameterization works by replacing templated variables in the source code with values specified in the Factory object. The `findReplace` action is triggered by the `factory.ide.onProjectsLoaded : {}` event. It is an array of JSON objects, so you can perform multiple parameterizations on your source tree.
```json  
"onProjectOpened" : {                          
  "actions" : [{  
      "id" : "findReplace\            
      "properties" : {  
        "in"          : "(pom\\.xm.*)|(test\\..*)\  // Which files?
        "find"        : "GROUP_ID\                  // What to find?
        "replace"     : "Codenvy\                   // Replace with?
        "replaceMode" : "test_multipass"
      }
    }]
}
```
####### Regex Format
The `in` parameter specifies which files in the source tree to perform the find / replace function on. The value is a path format provided as a regular expression. Visit regex reference page for more details.

####### Replacement Mode
The `replaceMode` property indicates which replacement algorithm should be applied:
* `variable_singlepass`: Variables that start with ‘$’ and enclosed with curly brackets {} will be searched. For example, to replace variable `VAR_1_NAME` in the resulting code, put `${VAR_1_NAME}` variable in the source code. `${VAR_1_NAME}` will become `VAR_1_NAME`.
* `text_multipass`: Plain text will be searched. This is slower since all text must be searched.

It is possible to combine two replacement methods. Priority is given to singlepass. If no replacement method is specified, `variable_singlepass` mode will be used.

#### factory.creator Object
This object has meta information that you can embed within the Factory. These attributes do not affect the automation behavior or the behavior of the generated workspace.
```json  
factory.creator : {
  name      : STRING,          // Name of author of this configuration file
  email     : STRING,          // Email address of author
  created   : EPOCHTIME,       // Set by the system
  userId    : STRING           // Set by the system
}\
```
#### factory.button Object
```json  
### Defines a visual button or logo that can front the Factory URL
factory.button : {
  type       : [logo |         // Sets whether button contains user's logo
                nologo],
  attributes : {}              // Properties of the button
}

factory.button.attributes : {
  color   : [gray | white],    // Background color. Ignored if type = logo
  counter : [false | true],    // Adds counter for clicks, updated daily
  logo    : URL,               // Button image URL. Ignored if type = nologo
  style   : [horizontal |      // Counter direction. Ignored if type = logo
             vertical]      
}\
```
We provide three button types with animations that execute when you hover over them.  You can save the button configuration with the Factory by filling in the `factory.button` object with the Factory configuration.  You can also use our button formats directly within Markdown and HTML using a JavaScript snippet.

We have three types of buttons.

| Button Type   | Sample   
| --- | ---
| White: 76 x 20 pixels   | Dark: 76 x 20 pixels   
| Logo: 104 x 104 image of your logo with our hover animation layered on top   | [![alt](https://codenvy.com/factory/resources/factory-white.png)]()   
| [![alt](https://codenvy.com/factory/resources/factory-dark.png)]()   | [![alt](https://files.readme.io/zolQfjkhT4KKn3P1xBdc_logo.PNG)]()   


###### Button Animation
Button animations do not work withing Markdown documents, unfortunately.  Our docs are built using a Markdown editor, so these samples will not give you the hover animation.  

##### JavaScript Button: Configuration From Factory
You can save your button configuration within Codenvy when you generate the Factory configuration. You do this by filling in the `factory.button` fields as part of your `factory.json`. Your button configuration is saved within Codenvy and any logo image is uploaded and hosted on our servers. You can then reference this Factory button directly using JavaScript.
```html  
<script
    type="text/javascript"
    src="https://codenvy.com/factory/resources/factory.js?FACTORY_HASH_CODE"
        >
</script>
```
##### JavaScript Button: Configuration Within JavaScript
You can reuse our buttons and animations without first having created a Factory.  
```html  
<script
    type    = "text/javascript"
    src     = "https://codenvy.com/factory/resources/factory.js"
    style   = ["gray" | "white" | "advanced"]
    counter = ["horizontal" | "vertical"]
    url     = URL_TO_ACTIVATE_WHEN_BUTTON_CLICKED
    logo    = URL_TO_YOUR_LOGO
        >
</script>
```
The `gray` style will cause the small dark animation button to load. The `white` does the small white animation button to load. Use `advanced` if you want to have your logo image loaded with our hover animation on top of it.  The `counter` will add a click counter to the button that is updated daily with the counts stored server side. The `style` attribute can be `white`, `dark` or `advanced`, which displays your logo.

##### Markdown Button
```markdown  
[![alt](https://codenvy.com/factory/resources/factory-white.png)](URL)
[![alt](https://codenvy.com/factory/resources/factory-dark.png)](URL)\
```
##### Button Examples
```html  
<script
    type    = "text/javascript"
    src     = "https://codenvy.com/factory/resources/factory.js"
    style   = "advanced"
    counter = "horizontal"
    url     = "https://codenvy.com"
    logo    = "http://bit.ly/1CjqxdR"
        >
</script>
```

```markdown  
[![alt](https://codenvy.com/factory/resources/factory-white.png)](https://codenvy.com/f?id=s38eam174ji42vty)\
```

## Factories Badged Repos  

A Codenvy Factory can be added to any git repository to provide an on-demand developer workspace ready in seconds. Anyone can get access to the project's code to edit, build, debug and contribute without installing software.

The Codenvy workspace will include the project's source files, the runtime stack, any commands that contributors will need to execute (build, debug, etc...), and a built-in browser IDE. It can even be setup to include the URL of a special Pull Request Factory in the GitHub pull request section when contributions are made.

Our Factory creation wizard makes it simple to create a Factory for any git repo - the process takes about 5 minutes. Once complete you can copy the Factory markdown code to get a "Developer Workspace" button to your README.MD.

Try a [Java Factory](http://beta.codenvy.com/f?id=5use7stej9bi9mxd) now
Or [see it on a GitHub repo](https://github.com/codenvy-demos/spring-petclinic).
## Creating a Workspace Factory  
Start in the Codenvy dashboard (you'll need to have [an account](https://codenvy.com/site/login#)):
1. Click the "Factories" entry in the left-hand navigation menu.
2. Click the "+" button in the top-right of the Factories page.

### Add One or More Projects to the Workspace
3. In the "Select Source" section, enter the git repo URL or connect to your GitHub account to browse all your repos (once the Factory is created you can add [additional projects](http://codenvy.readme.io/docs/factories#section-factory-workspace-object) if required). Note that projects with different languages can be added to the same workspace.

When you hit the "Next" button a Factory is created and you can begin configuring it. The easiest way to configure a Factory is with our wizard. However, if you'd prefer to craft the `factory.json` file yourself we have [documentation](http://codenvy.readme.io/docs/factories#configure) to help you.

### Configure A Workspace Runtime
4. In the "Configure Stacks" section you can choose: one of several common stacks; from a broader set of stacks in the Stack Library; to [add your own Dockerfile](https://eclipse-che.readme.io/docs/stacks#custom-workspace-recipes) for a custom stack.
![selectjava.png](../../docs/imgs/selectjava.png)

##### Built-In Terminal and SSH
For custom runtimes, we suggest using a Codenvy workspace recipe as a base. This ensures users have access to a terminal with full root access and can connect over SSH from their favorite desktop editor or IDE.  

5. After selecting the runtime you can set the RAM allocated to each Factory-generated workspace. If you're an open source project lead you can contact us to [get free 4GB workspaces](http://codenvy.readme.io/v4.0/docs/factories-on-your-repo#free-workspaces-for-oss-projects) for all your contributors.

### Add Commands to Build, Run or Debug
6. Add commands to simplify building and running your project for users - the syntax you enter will be executed in your runtime container when a user runs the command. For more information see our [command docs](https://eclipse-che.readme.io/docs/commands).
7. Add a [preview URL](https://eclipse-che.readme.io/docs/previews) to a command to allow users to see the running app with live changes in their browser.
8. Optionally, you can add actions that execute automatically after your workspace loads - these can also be added directly to the `factory.json` after you complete the wizard. See our [docs](http://codenvy.readme.io/docs/factories#section-factory-ide-object) for guidance.
## Badging a Repo  
Once your Factory is created it can be shared with the URL, however, for public projects it's easier to badge your repository. In the Factory page, click on the "copy markdown" button then paste the code into the README.MD file. View the readme of [this repo](https://github.com/codenvy-demos/spring-petclinic/blob/master/readme.md) to see an example.
## Adding Pull Request Automation and Post-Load Actions  
Once you have your Factory you can further customize it by editing the `factory.json`:
* [Add a simplified pull request panel](http://codenvy.readme.io/docs/factories#section-mixins) and other advanced functionality with "mixins."
* [Modify the policies](http://codenvy.readme.io/docs/factories#section-factory-policies-object) of the Factory.
* [Execute actions](http://codenvy.readme.io/docs/factories#section-factory-ide-object) when the browser IDE opens, closes or when all projects in the workspace are loaded.
## Getting Free Workspaces for an OSS Project  

# Guided Tours
---
title: Custom Guided Tours
excerpt: "Launch walk-me style guided tours in workspaces created by Factories"
layout: docs
overview: true
permalink: /docs/tour/
---
You can create a custom step by step walk through with popup windows that overlay the IDE in workspaces generated from your Factory.  You can define the style, structure, and placement of the steps and associate each step with an IDE action.  You can also structure the steps so that each is mandatory or whether users can early-terminate the tour.
## Create  
1. Add the `tour` mixin to your Factory.
2. Add `tourConfig` to `factory.workspace.projects.attributes`.
3. Set the value of the `tourConfig` variable to contain a URL that loads the tour JSON configuration.

This is a Factory snippet that sets up the configuration for a custom tour.
```json  
"project" : {  
  "mixins"     : ["tour"],
  "attributes" : {  
    "tourConfig" : ["https://someurl.com/GuidedTour.json"]
  }
}
```

## Configure  
The tour configuration JSON is an array of objects.
```json  
{
  name  : STRING,       // Name the tour
  hasWelcomeStep : BOOL // If true, first step is unnumbered w/o arrows.
  steps : [{}]          // An array of objects, with each object representing a step.
}\
```
The `steps` array contains a list of objects. Each object contains a set of step parameters.
```json  
tour.steps : [{
  title            : STRING,           // (MANDATORY) Title text. HTML allowed.
  content          : STRING,           // (MANDATORY) Body text of the step. HTML allowed.
  element          : STRING,           // (MANDATORY) DOM element where step box is attached for display.
  placement        : [BOTTOM |         // Placement of the tooltip relative to the DOM element.
                      TOP    |
                      LEFT   |
                      RIGHT],  
  nextButtonLabel  : STRING,            // Label that goes onto 'next' button in this step.
  skipButton       : [true | false],    // Allow the skip button to display?
  skipButtonLabel  : STRING,            // Text to appear on the skipButton.
  xOffset          : INTEGER,           // Apply horizontal offset from DOM placement.
  arrowOffset      : INTEGER,           // Apply horizontal offset for arrow.
  width            : INTEGER,           // Width in pixels of the step box.
  hideArrow        : [true | false],    // Hide the arrow of this step.
  hideBubbleNumber : [true | false],    // Hides the bubble number of this step.
  actions          : [{}],              // IDE actions when user clicks next or done.
  overlays         : [{}]               // Add UI to the IDE behind your step boxes.
}]\
```
All of the attributes are optional.

A step creates a box with a series of choices for the user to perform. The location and the size of the box can vary on the screen.  The step box is anchored to a DOM element.  Each IDE object that you see on the screen has its own DOM object name. You can use the object inspector of your browser to understand the name of each IDE object.  You then place the name of this object into the `tour.steps.element` attribute.

The string values for labels and content accept some limited HTML.  You can use `b`, `em`, `i`, `h1`, `h2`, `h3`, `h4`, `h5`, `h6`, `hr`, `ul`, `ol`, `li`. You can also insert images using `![alt name](URL of image)`.

#### Actions
You can trigger IDE actions as part of a step.  The `actions` parameter is an array of key:value pairs where each key is `action`.

| Syntax   | Example   
| --- | ---
| Example   | Trigger an IDE action.\n`trigger <ide-action>`   
| `trigger runApp`   |    
| Opens a workspace file in the editor.\n`openfile <file>`   | Opens one of the panels to be displayed. This first property is the location of the panel and the second is the panel title.\n`openpanel LEFT.<panel>`\n`openpanel BOTTOM.<panel>`\n`openpanel RIGHT.<panel>`\n`openpanel EDIT.<panel>`   
| Opens a URL in another browser tab.\n`openurl <url>`   | <code style=\white-space:nowrap;\>openfile projects/hello.txt</code>   

#### Overlays
An overlay is UI that you can add into the IDE that appears while your step is active.
```json  
tour.steps.overlays : [{
  element         : STRING,   // DOM element where the overlay is attached for display
  url             : STRING,   // Displays an image in the overlay
  xOffset         : INTEGER,  // Horizontal offset
  yOffset         : INTEGER,  // Vertical offset
  width           : {},       // Width in either pixels or % consumption
  height          : {},       // Height in either pixels or % consumption
  zIndex          : INTEGER,  // zIndex for the overlay
  backgroundColor : STRING    // RGB color specified as "rgba(#,#,#,#)"
}]

tour.steps.overlays.width : {
  value : INTEGER,
  unit  : [% | pixels]
}

tour.steps.overlays.height : {
  value : INTEGER,
  unit  : [% | pixels]
}
```

## Simple Example  
This example has a single step. The step has a message from Pivotal and displays the Spring logo inside of the step box. The step box is attached to the IDE's `gwt-debug-MainToolbar/runApp-true` DOM element.  When the user presses the button in the tour, the tour will automatically trigger the `runApp` IDE action, which is the action for executing the run button.
```json  
{  
  "steps" : [{  
    "title"     : "Run the app\n    "content"   : "From pivotal.![](http://spring.io/img/spring-by-pivotal.png)\n    "element"   : "gwt-debug-MainToolbar/runApp-true\n    "placement" : "BOTTOM\n    "xOffset"   : "-20\n    "actions"   : [{  
        "action" : "trigger runApp"
    }]
  }]
}
```

```json  
{
  "name" : "Codenvy Getting Started Guided Tour v1.3\n  "hasWelcomeStep" : true,
  "steps": [
    {
      "title": "Getting Started with Codenvy\n      "content": "Codenvy's on-demand developer environments are replicable, collaborative and constraint-free.\n\nThis tour takes 3 minutes.\n      "element": "gwt-debug-MainToolbar/New\n      "placement": "BOTTOM\n      "xOffset": "500\n      "yOffset": "100\n      "width" : "400\n      "skipButtonLabel": "Skip\n      "nextButtonLabel": "Start!\n      "overlays": [
        {
          "zIndex": "5\n          "width": {"value": 100, "unit" : "%"},
          "height": {"value": 100, "unit" : "%"},
          "backgroundColor": "rgba(50,50,50,0.72)"
        },
        {
          "url": "https://raw.githubusercontent.com/codenvy/factories/factory-2.0/guided-tour/getting-started/images/3.9.0/execution-bar.png\n          "element": "gwt-debug-MainToolbar/runApp-true\n          "xOffset": "-220\n          "yOffset": "-1"
        },
        {
          "url": "https://raw.githubusercontent.com/codenvy/factories/factory-2.0/guided-tour/getting-started/images/help.png\n          "element": "gwt-debug-MainMenu/helpGroup-true"
        },
        {
          "url": "https://raw.githubusercontent.com/codenvy/factories/factory-2.0/guided-tour/getting-started/images/3.8.0/explorer.png\n          "element": "gwt-debug-tabButton-Datasource\n          "xOffset": "18\n          "yOffset": "-58"
        },
        {
          "url": "https://raw.githubusercontent.com/codenvy/factories/factory-2.0/guided-tour/getting-started/images/3.8.0/Builder-Runners.png\n          "element": "gwt-debug-tabButton-Builder\n          "xOffset": "0\n          "yOffset": "-1"
        },
        {
          "url": "https://raw.githubusercontent.com/codenvy/factories/factory-2.0/guided-tour/getting-started/images/3.9.0/ram.png\n          "element": "memory-widget-panel\n          "xOffset": "-264\n          "yOffset": "-2"
        },
        {
          "url": "https://raw.githubusercontent.com/codenvy/factories/factory-2.0/guided-tour/getting-started/images/3.9.0/visibility-permission.png\n          "element": "gwt-debug-permissions-panel-rights\n          "xOffset": "-344\n          "yOffset": "-4"
        }
      ]
    },
    {
      "title": "Build the project\n      "content": "Builders install dependencies, execute the build pipeline, and package artifacts. Builders for npm, maven, grunt and others are built-in.\n      "element": "gwt-debug-MainToolbar/runApp-true\n      "placement": "LEFT\n      "xOffset": "-187\n      "yOffset": "-10\n      "width": "300\n      "actions": [
        {
          "action": "trigger buildApp"
        }
      ]
    },
    {
      "title": "Run the app\n      "content": "Your project executes within a Runner, which binds a Docker machine with the stack for your app. You can have many machines and customize them using Dockerfiles.\n      "element": "gwt-debug-MainToolbar/runApp-true\n      "placement": "LEFT\n      "xOffset": "21\n      "yOffset": "-10\n      "width": "300\n      "actions": [
        {
          "action": "trigger runApp"
        }
      ]
    },
    {
      "title": "Preview the app\n      "content": "Each run creates an isolated Runner instance with its own RAM. Your project’s files and artifacts are copied into the Runner. Your application's URL appears after the machine is booted.\n      "element": "gwt-debug-Terminal-tab\n      "width": "450\n      "placement": "TOP\n      "xOffset": "100\n      "yOffset": "240\n      "actions": [
        {
          "action": "openfile readme.md"
        }
      ]
    },
    {
      "title": "Edit files\n      "content": "We embed CodeMirror and Orion editors with key bindings for emacs, vi, and sublime along with syntax highlighting, auto-complete, quick-fix, and code folding.\n      "element": "gwt-debug-projectExplorerTree-panel\n      "placement": "LEFT\n      "width" : "250\n      "xOffset": "270\n      "yOffset": "70"
    },
    {
      "title": "Connect version control\n      "content": "Your project may have a local git or subversion repo synchronized with a remote.\n      "element": "gwt-debug-MainMenu/git-true\n      "placement": "BOTTOM\n      "width" : "350\n      "xOffset": "-5\n      "yOffset": "-7"
    },
    {
      "title": "Temporary workspace\n      "content": "This project is in a temporary workspace. Your work will be lost if you are idle.\n      "element": "gwt-debug-temporary-workspace-used-toolbar-button\n      "placement": "TOP\n      "xOffset": "-20"
    },
    {
      "title": "Save your work\n      "content": "Persist will copy your project into a permanent workspace. Create a free account to get unlimited IDE time, private projects, and storage.\n      "element": "gwt-debug-MainMenu/git-true\n      "placement": "BOTTOM\n      "xOffset": "200\n      "yOffset": "100\n      "width" : "600\n      "hideArrow" : true,
      "hideBubbleNumber" : true,
      "skipButtonLabel": "End tour\n      "nextButtonLabel": "Signup\n      "actions": [
        {
          "action": "openurl https://codenvy.com/site/create-account"
        }
      ]
    }
  ]
}
```
