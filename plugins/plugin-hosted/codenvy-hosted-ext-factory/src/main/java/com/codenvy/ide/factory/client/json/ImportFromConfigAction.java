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
package com.codenvy.ide.factory.client.json;

import com.codenvy.ide.factory.client.FactoryLocalizationConstant;
import com.codenvy.ide.factory.client.FactoryResources;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;

/**
 * @author Sergii Leschenko
 */
@Singleton
public class ImportFromConfigAction extends Action {

    private final ImportFromConfigPresenter presenter;

    @Inject
    public ImportFromConfigAction(final ImportFromConfigPresenter presenter,
                                  FactoryLocalizationConstant locale,
                                  WorkspaceServiceClient workspaceServiceClient,
                                  DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                  FactoryResources resources) {
        super(locale.importFromConfigurationName(), locale.importFromConfigurationDescription(), null, resources.importConfig());
        this.presenter = presenter;

//        workspaceServiceClient.getMembership(workspaceId, new AsyncRequestCallback<MemberDescriptor>(
//                dtoUnmarshallerFactory.newUnmarshaller(MemberDescriptor.class)) {
//            @Override
//            protected void onSuccess(MemberDescriptor result) {
//                //do nothing user has roles in this workspace and widget enabled by default
//            }
//
//            @Override
//            protected void onFailure(Throwable exception) {
//                //user hasn't roles in this current workspace
//                getTemplatePresentation().setEnabled(false);
//            }
//        });
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        presenter.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent event) {
    }
}
