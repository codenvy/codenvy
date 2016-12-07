---
title: Custom Guided Tours
excerpt: "Launch walk-me style guided tours in workspaces created by Factories"
layout: docs
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
