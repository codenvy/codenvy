/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.hosted.client.workspace;

import com.codenvy.ide.hosted.client.HostedLocalizationConstant;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.ui.window.Window;

import javax.inject.Inject;

/**
 * @author Mihail Kuznyetsov
 */
public class WorkspaceNotRunningViewImpl extends Window implements WorkspaceNotRunningView {
    interface WorkspaceNotRunningViewImplUiBinder extends UiBinder<Widget, WorkspaceNotRunningViewImpl> {
    }

    private ActionDelegate delegate;

    @UiField
    Label label;

    @Inject
    public WorkspaceNotRunningViewImpl(final HostedLocalizationConstant locale,
                                       WorkspaceNotRunningViewImplUiBinder uiBinder) {

        this.setWidget(uiBinder.createAndBindUi(this));
        setTitle(locale.workspaceNotRunningTitle());

        hideCrossButton();
        setHideOnEscapeEnabled(false);

        Button buttonDashboard = createButton(locale.openDashboardTitle(),
                                              "dashboard-button",
                                              new ClickHandler() {
                                                  @Override
                                                  public void onClick(ClickEvent event) {
                                                      delegate.onOpenDashboardButtonClicked();
                                                  }
                                              });
        Button buttonRestart = createButton(locale.restartWsButton(),
                                            "restart-button",
                                            new ClickHandler() {
                                                @Override
                                                public void onClick(ClickEvent event) {
                                                    delegate.onRestartButtonClicked();
                                                }
                                            });
        buttonDashboard.getElement().getStyle().setProperty("height", "24px");
        buttonDashboard.getElement().getStyle().setProperty("lineHeight", "24px");

        buttonRestart.getElement().getStyle().setProperty("backgroundImage",
                                                          "linear-gradient(#2f75a3 0px, #2f75a3 5%, #266c9e 5%, #266c9e 100%)");
        buttonRestart.getElement().getStyle().setProperty("height", "24px");
        buttonRestart.getElement().getStyle().setProperty("lineHeight", "24px");

        addButtonToFooter(buttonRestart);
        addButtonToFooter(buttonDashboard);
    }

    @Override
    public void setDelegate(WorkspaceNotRunningView.ActionDelegate delegate) {
        this.delegate = delegate;
    }
}
