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
package com.codenvy.ide.factory.client.welcome.preferences;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Vitaliy Guliy
 */
@Singleton
public class ShowWelcomePreferencePageViewImpl implements ShowWelcomePreferencePageView {

    interface ShowWelcomePreferencePageViewImplUiBinder extends UiBinder<FlowPanel, ShowWelcomePreferencePageViewImpl> {}

    private ActionDelegate delegate;

    private Widget widget;

    @UiField
    CheckBox showWelcome;

    @Inject
    public ShowWelcomePreferencePageViewImpl(ShowWelcomePreferencePageViewImplUiBinder uiBinder) {
        widget = uiBinder.createAndBindUi(this);

        showWelcome.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
                if (delegate != null) {
                    delegate.onDirtyChanged();
                }
            }
        });
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public HasValue<Boolean> welcomeField() {
        return showWelcome;
    }

}
