Code policies
---
* The default policy for method arguments is _NotNull_ policy,
which means that all defined method arguments are not null by default. If `null` value is appropriate one, then argument should be annotated with `@Nullable`. If method may return `null` value then this method should be either annotated with `@Nullable` or return an `Optional` describing the result.

* All the components related to the certain VCS hosting service
must be put into the dedicated service package.

    * [VSTS](src/main/java/com/codenvy/plugin/contribution/client/vsts)
    * [Github](src/main/java/com/codenvy/plugin/contribution/client/github)


* Components configuration must be defined in [ContributorGinModule](src/main/java/com/codenvy/plugin/contribution/client/inject/ContributorGinModule.java)


Packages description
---

* `com.codenvy.plugin.contribution.client.workflow` contains main contribution workflow components such as `ContributionWorkflow` or `WorkflowExecutor`.

* `com.codenvy.plugin.contribution.client.steps` contains all the contribution workflow steps. New steps should be put there even if those steps are related to the certain VCS hosting service.

* `com.codenvy.plugin.contribution.client.inject` contains configuration files and modules.

* `com.codenvy.plugin.contribution.client.utils` contains utility classes.

* `com.codenvy.plugin.contribution.client.events` contains events which may be thrown by plugin components and appropriate event handlers for each of the events.

* `com.codenvy.plugin.contribution.client.parts` contains contribution plugin views and presenters.

* `com.codenvy.plugin.contribution.client.dialogs` contains contribution plugin dialog views and presenters.

* `com.codenvy.plugin.contribution.client.vsts` contains VSTS hosting service related components.

* `com.codenvy.plugin.contribution.client.github` contains Github hosting service related components.

How to add a new workflow ?
---

* Implement `ContributionWorkflow` and `StagesProvider`.
* Put the implementations into the dedicated package.
* Bind the implementations in the `ContributorGinModule`.

Binding example:

```java
// binding worflow
GinMapBinder<String, ContributionWorkflow> workflowBinder
        = GinMapBinder.newMapBinder(binder(),
                                    String.class,
                                    ContributionWorkflow.class);
workflowBinder.addBinding(NewService.SERVICE_NAME)
              .to(NewServiceContributionWorkflow.class);

// binding stages provider
GinMapBinder<String, StagesProvider> stagesProvider
        = GinMapBinder.newMapBinder(binder(),
                                    String.class,
                                    StagesProvider.class);
stagesProvider.addBinding(NewService.SERVICE_NAME)
              .to(NewServiceStagesProvider.class);
```
