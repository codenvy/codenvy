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
package com.codenvy.ide.factory.client.action;

import com.codenvy.ide.factory.client.FactoryLocalizationConstant;
import com.codenvy.ide.factory.client.FactoryResources;
import com.codenvy.ide.factory.client.configure.CreateFactoryPresenter;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.factory.FactoryServiceClient;

import javax.validation.constraints.NotNull;
import java.util.Collections;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * @author Anton Korneta
 */
@Singleton
public class CreateFactoryAction extends AbstractPerspectiveAction {

    private final CreateFactoryPresenter      presenter;
    private final FactoryServiceClient        factoryService;
    private final FactoryLocalizationConstant locale;

    @Inject
    public CreateFactoryAction(CreateFactoryPresenter presenter,
                               FactoryResources resources,
                               FactoryLocalizationConstant locale,
                               FactoryServiceClient factoryService) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID), locale.createFactoryActionTitle(), null, null, null);
        this.presenter = presenter;
        this.factoryService = factoryService;
        this.locale = locale;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        presenter.showDialog();
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
    }
}
