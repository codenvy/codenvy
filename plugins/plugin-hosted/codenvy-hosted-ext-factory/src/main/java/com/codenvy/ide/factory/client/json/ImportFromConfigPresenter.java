/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.ide.factory.client.json;

import com.codenvy.ide.factory.client.FactoryLocalizationConstant;
import com.codenvy.ide.factory.client.utils.FactoryProjectImporter;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.dto.DtoFactory;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;

/**
 * Imports project from factory.json file
 *
 * @author Sergii Leschenko
 */
public class ImportFromConfigPresenter implements ImportFromConfigView.ActionDelegate {
    private final FactoryLocalizationConstant factoryLocalization;
    private final ImportFromConfigView        view;
    private final NotificationManager         notificationManager;
    private final DtoFactory                  dtoFactory;
    private final FactoryProjectImporter      projectImporter;
    private final AsyncCallback<Void>         importerCallback;

    private StatusNotification notification;

    @Inject
    public ImportFromConfigPresenter(final FactoryLocalizationConstant factoryLocalization,
                                     FactoryProjectImporter projectImporter,
                                     ImportFromConfigView view,
                                     NotificationManager notificationManager,
                                     DtoFactory dtoFactory) {
        this.factoryLocalization = factoryLocalization;
        this.notificationManager = notificationManager;
        this.view = view;
        this.dtoFactory = dtoFactory;
        this.view.setDelegate(this);
        this.projectImporter = projectImporter;

        importerCallback = new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                notification.setContent(factoryLocalization.clonedSource(null));
                notification.setStatus(StatusNotification.Status.SUCCESS);
            }

            @Override
            public void onFailure(Throwable throwable) {
                notification.setContent(throwable.getMessage());
                notification.setStatus(StatusNotification.Status.FAIL);
            }
        };
    }

    /** Show dialog. */
    public void showDialog() {
        view.setEnabledImportButton(false);
        view.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.closeDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void onImportClicked() {
        view.closeDialog();
        FactoryDto factoryJson;
        try {
            factoryJson = dtoFactory.createDtoFromJson(view.getFileContent(), FactoryDto.class);
        } catch (JSONException jsonException) {
            notification.setStatus(StatusNotification.Status.FAIL);
            notification.setContent("Error parsing factory object.");
            return;
        }

        notification = notificationManager.notify(factoryLocalization.cloningSource(), null, StatusNotification.Status.PROGRESS, NOT_EMERGE_MODE);
        projectImporter.startImporting(factoryJson, importerCallback);
    }


    @Override
    public void onErrorReadingFile(String errorMessage) {
        view.setEnabledImportButton(false);
        notification.setStatus(StatusNotification.Status.FAIL);
        notification.setContent(errorMessage);
    }
}
