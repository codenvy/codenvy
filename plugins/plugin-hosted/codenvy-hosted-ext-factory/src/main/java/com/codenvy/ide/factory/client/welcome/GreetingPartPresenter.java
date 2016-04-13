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
package com.codenvy.ide.factory.client.welcome;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.HasView;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author Vitaliy Guliy
 * @author Sergii Leschenko
 */
@Singleton
public class GreetingPartPresenter extends BasePresenter implements GreetingPartView.ActionDelegate, HasView {
    private static final String DEFAULT_TITLE = "Greeting";

    private final WorkspaceAgent      workspaceAgent;
    private final GreetingPartView    view;

    private String title = DEFAULT_TITLE;

    @Inject
    public GreetingPartPresenter(GreetingPartView view,
                                 WorkspaceAgent workspaceAgent) {
        this.view = view;
        this.workspaceAgent = workspaceAgent;

        view.setDelegate(this);
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    @NotNull
    @Override
    public String getTitle() {
        return title != null ? title : DEFAULT_TITLE;
    }

    @Nullable
    @Override
    public ImageResource getTitleImage() {
        return null;
    }

    @Nullable
    @Override
    public String getTitleToolTip() {
        return "Greeting the user";
    }

    @Override
    public int getSize() {
        return 320;
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    public void showGreeting(Map<String, String> parameters) {
        showGreeting(parameters.get("greetingTitle"),
                     parameters.get("greetingContentUrl"),
                     parameters.get("greetingNotification"));
    }


    private void hideGreeting() {
        workspaceAgent.removePart(this);
    }

    /**
     * Opens Greeting part and displays the URL in Frame.
     */
    private void showGreeting(@NotNull String title, String greetingContentURL, final String notification) {
        this.title = title;
        workspaceAgent.openPart(this, PartStackType.TOOLING, Constraints.FIRST);
        new Timer() {
            @Override
            public void run() {
                workspaceAgent.setActivePart(GreetingPartPresenter.this);
            }
        }.schedule(3000);

        view.setTitle(title);
        view.showGreeting(greetingContentURL);

        if (notification != null) {
            new Timer() {
                @Override
                public void run() {
                    new TooltipHint(notification);
                }
            }.schedule(1000);
        }
    }

    @Override
    public View getView() {
        return view;
    }

}
