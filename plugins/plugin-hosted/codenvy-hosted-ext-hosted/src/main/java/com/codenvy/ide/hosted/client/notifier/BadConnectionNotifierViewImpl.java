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
