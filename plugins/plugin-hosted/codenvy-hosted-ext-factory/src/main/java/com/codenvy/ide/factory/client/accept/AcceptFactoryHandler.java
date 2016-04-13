/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.ide.factory.client.accept;

import com.codenvy.ide.factory.client.FactoryLocalizationConstant;
import com.codenvy.ide.factory.client.utils.FactoryProjectImporter;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.factory.shared.dto.Action;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.factory.shared.dto.Ide;
import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateEvent;
import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;

import javax.inject.Inject;

/**
 * @author Sergii Leschenko
 * @author Anton Korneta
 */
@Singleton
public class AcceptFactoryHandler {
    private final FactoryLocalizationConstant factoryLocalization;
    private final FactoryProjectImporter      factoryProjectImporter;
    private final EventBus                    eventBus;
    private final AppContext                  appContext;
    private final ActionManager               actionManager;
    private final NotificationManager         notificationManager;

    private StatusNotification notification;
    private boolean            isImportingStarted;

    @Inject
    public AcceptFactoryHandler(FactoryLocalizationConstant factoryLocalization,
                                FactoryProjectImporter factoryProjectImporter,
                                EventBus eventBus,
                                AppContext appContext,
                                ActionManager actionManager,
                                NotificationManager notificationManager) {
        this.factoryProjectImporter = factoryProjectImporter;
        this.factoryLocalization = factoryLocalization;
        this.eventBus = eventBus;
        this.appContext = appContext;
        this.actionManager = actionManager;
        this.notificationManager = notificationManager;
    }

    /**
     * Accepts factory if it is present in context of application
     */
    public void process() {
        final Factory factory;
        if ((factory = appContext.getFactory()) == null) {
            return;
        }
        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(final WsAgentStateEvent event) {
                if (isImportingStarted) {
                    return;
                }

                isImportingStarted = true;

                notification = notificationManager.notify(factoryLocalization.cloningSource(), StatusNotification.Status.PROGRESS, false);
                performOnAppLoadedActions(factory);
                startImporting(factory);
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {

            }
        });
    }

    private void startImporting(final Factory factory) {
        factoryProjectImporter.startImporting(factory,
                                              new AsyncCallback<Void>() {
                                                  @Override
                                                  public void onSuccess(Void result) {
                                                      notification.setStatus(StatusNotification.Status.SUCCESS);
                                                      notification.setContent(factoryLocalization.cloningSource());
                                                      performOnProjectsLoadedActions(factory);
                                                  }

                                                  @Override
                                                  public void onFailure(Throwable throwable) {
                                                      notification.setStatus(StatusNotification.Status.FAIL);
                                                      notification.setContent(throwable.getMessage());
                                                  }
                                              });
    }

    private void performOnAppLoadedActions(final Factory factory) {
        final Ide ide = factory.getIde();
        if (ide == null || ide.getOnAppLoaded() == null) {
            return;
        }
        for (Action action : ide.getOnAppLoaded().getActions()) {
            actionManager.performAction(action.getId(), action.getProperties());
        }
    }

    private void performOnProjectsLoadedActions(final Factory factory) {
        final Ide ide = factory.getIde();
        if (ide == null || ide.getOnProjectsLoaded() == null) {
            return;
        }
        for (Action action : ide.getOnProjectsLoaded().getActions()) {
            actionManager.performAction(action.getId(), action.getProperties());
        }
    }
}
