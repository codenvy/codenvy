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
package com.codenvy.ide.hosted.client.login;

import com.codenvy.ide.hosted.client.HostedLocalizationConstant;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.ui.window.Window;

/**
 * Prompt window UI
 *
 * @author Max Shaposhnik
 * @author Sergii Leschenko
 */
public class PromptToLoginViewImpl extends Window implements PromptToLoginView {

    interface PromptToLoginViewImplUiBinder extends UiBinder<Widget, PromptToLoginViewImpl> {
    }

    @UiField
    Label label;

    private ActionDelegate delegate;

    @Inject
    public PromptToLoginViewImpl(HostedLocalizationConstant localizationConstant,
                                 PromptToLoginViewImplUiBinder uiBinder) {
        this.setWidget(uiBinder.createAndBindUi(this));

        Button buttonLogin = createButton(localizationConstant.loginButtonTitle(),
                                          "login-button",
                                          new ClickHandler() {
                                              @Override
                                              public void onClick(ClickEvent event) {
                                                  delegate.onLogin();
                                              }
                                          });

        Button buttonCreateAccount = createButton(localizationConstant.createAccountButtonTitle(),
                                                  "account-button",
                                                  new ClickHandler() {
                                                      @Override
                                                      public void onClick(ClickEvent event) {
                                                          delegate.onCreateAccount();
                                                      }
                                                  });

        buttonLogin.getElement().getStyle().setProperty("backgroundImage",
                                                        "linear-gradient(#2f75a3 0px, #2f75a3 5%, #266c9e 5%, #266c9e 100%)");
        buttonLogin.getElement().getStyle().setProperty("height", "24px");
        buttonLogin.getElement().getStyle().setProperty("lineHeight", "24px");

        buttonCreateAccount.getElement().getStyle().setProperty("backgroundImage",
                                                                "linear-gradient(#00874d 0px, #00874d 5%, #008046 5%, #008046 100%)");
        buttonCreateAccount.getElement().getStyle().setProperty("height", "24px");
        buttonCreateAccount.getElement().getStyle().setProperty("lineHeight", "24px");

        addButtonToFooter(buttonCreateAccount);
        addButtonToFooter(buttonLogin);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void onClose() {
    }

    @Override
    public void showDialog(String title, String message) {
        setTitle(title);
        label.setText(message);
        this.show();
    }

    @Override
    public void close() {
        this.hide();
    }

}
