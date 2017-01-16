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
package com.codenvy.ide.factory.client.action;

import com.codenvy.ide.factory.client.FactoryLocalizationConstant;
import com.codenvy.ide.factory.client.configure.CreateFactoryPresenter;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;

import javax.validation.constraints.NotNull;
import java.util.Collections;

/**
 * @author Anton Korneta
 */
@Singleton
public class CreateFactoryAction extends AbstractPerspectiveAction {

    private final CreateFactoryPresenter      presenter;

    @Inject
    public CreateFactoryAction(CreateFactoryPresenter presenter,
                               FactoryLocalizationConstant locale) {
        super(Collections.singletonList("Project Perspective"), locale.createFactoryActionTitle(), null, null, null);
        this.presenter = presenter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        presenter.showDialog();
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
    }
}
