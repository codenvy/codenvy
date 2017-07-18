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
package com.codenvy.ide.hosted.client.notifier;

import com.codenvy.ide.hosted.client.HostedLocalizationConstant;
import com.codenvy.ide.hosted.client.HostedResources;
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
 * Implementation of {@link BadConnectionNotifierView}
 *
 * @author Anton Korneta
 */
public class BadConnectionNotifierViewImpl extends Window implements BadConnectionNotifierView {

    interface PromptToLoginViewImplUiBinder extends UiBinder<Widget, BadConnectionNotifierViewImpl> {
    }

    private final HostedResources resources;

    @UiField
    Label label;

    private ActionDelegate delegate;

    @Inject
    public BadConnectionNotifierViewImpl(final HostedLocalizationConstant localizationConstant,
                                         final PromptToLoginViewImplUiBinder uiBinder,
                                         final HostedResources resources) {
        this.resources = resources;
        this.setWidget(uiBinder.createAndBindUi(this));

        final Button okButton = createButton(localizationConstant.okButtonTitle(),
                                             "ok-button",
                                             new ClickHandler() {
                                                 @Override
                                                 public void onClick(ClickEvent event) {
                                                     delegate.onOkClicked();
                                                 }
                                             });

        okButton.addStyleName(this.resources.hostedCSS().blueButton());

        addButtonToFooter(okButton);
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
