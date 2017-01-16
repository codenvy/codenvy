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
package com.codenvy.ide.factory.client.welcome;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Frame;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;

/**
 * @author Vitaliy Guliy
 */
@Singleton
public class GreetingPartViewImpl extends BaseView<GreetingPartView.ActionDelegate> implements GreetingPartView {

    private Frame frame;

    @Inject
    public GreetingPartViewImpl(PartStackUIResources resources) {
        super(resources);

        frame = new Frame();
        frame.setWidth("100%");
        frame.setHeight("100%");
        frame.getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
        frame.getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);

        frame.getElement().setAttribute("id", "greetingFrame");
        frame.getElement().setAttribute("tabindex", "0");

        setContentWidget(frame);

        frame.addLoadHandler(new LoadHandler() {
            @Override
            public void onLoad(LoadEvent event) {
                frame.getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
            }
        });

        handleFrameEvents(frame.getElement());
    }

    /**
     * Adds handlers to the greeting frame and window to catch mouse clicking on the frame.
     *
     * @param frame
     *         native frame object
     */
    private native void handleFrameEvents(final JavaScriptObject frame) /*-{
        var instance = this;
        frame["hovered"] = false;

        frame.addEventListener('mouseover', function (e) {
            frame["hovered"] = true;
        }, false);

        frame.addEventListener('mouseout', function (e) {
            frame["hovered"] = false;
        }, false);

        $wnd.addEventListener('blur', function (e) {
            if (frame["hovered"] == true) {
                instance.@com.codenvy.ide.factory.client.welcome.GreetingPartViewImpl::activatePart()();
            }
        }, false);
    }-*/;

    @Override
    public void showGreeting(String url) {
        frame.getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);

        if (url == null || url.trim().isEmpty()) {
            frame.setUrl("about:blank");
        } else {
            frame.setUrl(url);
        }
    }

    /**
     * Ensures the view is activated when clicking the mouse.
     */
    private void activatePart() {
        if (!isFocused()) {
            setFocus(true);
            if (delegate != null) {
                delegate.onActivate();
            }
        }
    }

    @Override
    protected void focusView() {
        frame.getElement().focus();
    }

}
