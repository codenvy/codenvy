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
