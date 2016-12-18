---
tag: [ "codenvy" ]
title: Introduction
excerpt: "White labeling is for embedding, distributing, or hosting Codenvy as part of another brand."
layout: docs
permalink: /:categories/introduction-white-labels/
---
A white label of Codenvy allows embedding of Codenvy into another brand or within another product. You may add plugins with custom functionality, remove some of the default plugins which aren't needed by your customer base, modify the user interface and wire it up to your own product systems.

**White labelling requires an OEM license.** Our standard license does not provide redistribution rights. Please [contact sales@codenvy.com](https://codenvy.com/contact/sales/) for more information and to request a white label license.

# Extending Codenvy 
White label licensees often wish to add new features to Codenvy. This can be done by creating Eclipse Che extensions and plugins which are 100% compatible with Codenvy. The Eclipse Che docs include an extensive section on [building extensions and plugins](https://eclipse-che.readme.io/docs/introduction-1).

# Setting Limits  
Limits are set in Codenvy's configuration file: `/etc/puppet/manifests/nodes/codenvy/codenvy.pp`. You can edit this file and save it. After it is saved, you must run puppet agent -t to have the changes take affect. Alternatively, Puppet will eventually pick up the changes as it is set to run every 30 minutes.

You can change the number of workspaces users are allowed to have by altering the following property in the `codenvy.pp` file:
`$limits_user_workspaces_count = “3”`

# Modifying the User Interface  
It's possible to customize nearly any portion of the user interface, however there are several customizations that are commonly requested.

## Changing the Codenvy Logo
All branding is located in the `dashboard/src/assets/branding` folder:
- `codenvy-logo.svg` is used in loading screens.
- `codenvy-logo-text.svg` is displayed in the left navbar's menu at the top.

To change the logo replace those two files with your organization's logo (match the size of the existing codenvy.svg files to avoid resizing issues). Then set the links to them in [product.json](dashboard/src/assets/branding/product.json) file:

```json  
"logoFile": "my-corp-logo.svg\n"logoTextFile": "my-corp-logo-text.svg"
```
The `product.json` file is also where you can change the Codenvy title and Favicon files.

## Changing the Help Links
The Help menu button in the footer can be changed in the `dashboard/src/assets/branding/product.json` file:

```json  
"helpPath": "https://codenvy.com/support/\n"helpTitle": "Help"
```

## Removing the Codenvy Loader
To completely remove the loader from the splash screen, delete the appropriate lines in:
- [index html, line 64](https://github.com/codenvy/codenvy/blob/master/dashboard/src/index.html#L64)
- [load-factory.html, line 66](https://github.com/codenvy/codenvy/blob/master/dashboard/src/app/factories/load-factory/load-factory.html#L66)

## Changing the Color of the Left Navbar
The colors of the navigation menu are configured in [navbar-theme.styl](https://github.com/codenvy/codenvy/blob/master/dashboard/src/app/navbar/navbar-theme.styl):
```json  
$onprem-navbar-backgroud-color #background color of the menu
$onprem-navbar-active-menu-background-color #background color of selected menu item
$onprem-navbar-border-right-color #color of the right border of selected item
$onprem-navbar-menu-icon-color #menu icon’s color
$onprem-navbar-active-menu-icon-color #selected menu item's color
$onprem-navbar-menu-text-color - #text color of menu item
$onprem-navbar-active-menu-text-color - #text color of selected menu item
$onprem-navbar-menu-number-color #color of number in brackets
$onprem-navbar-recent-workspaces-stopped-color - #recent workspaces items color in stopped state\
```

## Removing the "Make a Wish" Widget
To remove the "Make a Wish" widget in the footer, remove the `che-support-email` attribute in the `che-footer element` in the index.html:

```html  
#remove this section
<che-footer id="codenvyfooter" che-support-help-path="{{branding.helpPath}}" che-support-help-title="{{branding.helpTitle}}" che-support-email="{{branding.supportEmail}}" che-product-name="Codenvy" ng-show="waitingLoaded && !showIDE"></che-footer>
```
Alternatively you can leave the widget but change the link to point to your own organization's email by changing the `helpPath` and `supportEmail` attributes in the [product.json](dashboard/src/assets/branding/product.json) file.

# Creating Custom Assemblies  
<<<<<<< HEAD:docs/_docs/white-labels/white-labels-introduction-white-labels.md
See the [custom assemblies documentation](../../docs/custom-assemblies/) in this section.
=======
See the [custom assemblies documentation](../../docs/custom-assemblies/) in this section.
>>>>>>> master:docs/_docs/white-labels/introduction-white-labels.md
