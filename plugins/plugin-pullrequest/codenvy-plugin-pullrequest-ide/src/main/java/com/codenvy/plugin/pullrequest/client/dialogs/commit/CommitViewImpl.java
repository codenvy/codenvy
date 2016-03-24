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
package com.codenvy.plugin.pullrequest.client.dialogs.commit;

import com.codenvy.plugin.pullrequest.client.ContributeMessages;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.inject.Inject;

import org.eclipse.che.ide.ui.window.Window;

import javax.validation.constraints.NotNull;

/**
 * UI for {@link CommitView}.
 *
 * @author Kevin Pollet
 */
public class CommitViewImpl extends Window implements CommitView {

    /** The UI binder for this component. */
    private static final CommitViewUiBinder UI_BINDER = GWT.create(CommitViewUiBinder.class);

    private final Button ok;

    @UiField(provided = true)
    ContributeMessages messages;

    @UiField
    TextArea commitDescription;

    @UiField
    CheckBox includeUntracked;

    private ActionDelegate delegate;

    @Inject
    public CommitViewImpl(final ContributeMessages messages) {
        this.messages = messages;

        setWidget(UI_BINDER.createAndBindUi(this));
        setTitle(messages.commitDialogTitle());

        ok = createButton(messages.commitDialogButtonOkText(), "commit-dialog-ok", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onOk();
            }
        });
        ok.addStyleName(resources.windowCss().button());

        final Button continueWithoutCommitting =
                createButton(messages.commitDialogButtonContinueText(), "commit-dialog-continue-without-committing",
                             new ClickHandler() {
                                 @Override
                                 public void onClick(ClickEvent event) {
                                     delegate.onContinue();
                                 }
                             });

        final Button cancel = createButton(messages.commitDialogButtonCancelText(), "commit-dialog-cancel",
                                           new ClickHandler() {
                                               @Override
                                               public void onClick(ClickEvent event) {
                                                   delegate.onCancel();
                                               }
                                           });

        addButtonToFooter(ok);
        addButtonToFooter(continueWithoutCommitting);
        addButtonToFooter(cancel);
    }

    @Override
    public void show(@NotNull String commitDescription) {
        this.commitDescription.setText(commitDescription);
        new Timer() {
            @Override
            public void run() {
                CommitViewImpl.this.commitDescription.setFocus(true);
            }
        }.schedule(300);
        super.show();
    }

    @Override
    public void close() {
        hide();
    }

    @NotNull
    @Override
    public String getCommitDescription() {
        return commitDescription.getText();
    }

    @Override
    public void setOkButtonEnabled(final boolean enabled) {
        ok.setEnabled(enabled);
    }

    @Override
    public boolean isIncludeUntracked() {
        return includeUntracked.getValue();
    }

    @Override
    public void setDelegate(final ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void onClose() {
        delegate.onCancel();
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("commitDescription")
    void onCommitDescriptionChanged(final KeyUpEvent event) {
        delegate.onCommitDescriptionChanged();
    }
}
