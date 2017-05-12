Dependencies
=======

[package.json](https://github.com/codenvy/codenvy/blob/master/site/package.json) contains detailed information on both runtime and development dependencies.

To run site locally and to be able to build project for deployment you will need:
Install environment on Ubuntu 14.04
=======
```
java
maven
npm 3.3.6
nodejs >=5.0.0
gulp >=3.9.1
jekyll >= 3.3.1
Ruby version >= 2.1.9
```

Running locally
=======

To build the site on your machine

```
mvn clean install

```

Build populates target/dist/ with 2 sets of bundles :
- stage (for staging),
- prod (for production with minified CSS, JS),

The sets contain all the static content needed to run the site.
