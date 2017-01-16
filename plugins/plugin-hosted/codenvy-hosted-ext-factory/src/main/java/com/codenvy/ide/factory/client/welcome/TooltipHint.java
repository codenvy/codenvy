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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.UIObject;

/**
 * @author Vitaliy Guliy
 */
public class TooltipHint extends UIObject {

    private static TooltipHintUiBinder uiBinder = GWT.create(TooltipHintUiBinder.class);

    interface TooltipHintUiBinder extends UiBinder<Element, TooltipHint> {
    }

    @UiField
    TableCellElement messageElement;

    @UiField
    DivElement closeButton;

    private int opacity = 0;

    private int top   = 1;
    private int delta = 3;

    public TooltipHint(String text) {
        setElement(uiBinder.createAndBindUi(this));
        messageElement.setInnerHTML(SafeHtmlUtils.htmlEscape(text));

        DOM.sinkEvents((com.google.gwt.dom.client.Element)closeButton.cast(), Event.ONCLICK);
        DOM.setEventListener((com.google.gwt.dom.client.Element)closeButton.cast(), new EventListener() {
            public void onBrowserEvent(Event event) {
                close();
            }
        });

        getElement().getStyle().setProperty("opacity", "0");
        getElement().getStyle().setTop(top, Unit.PX);
        RootPanel.get().getElement().appendChild(getElement());

        getElement().getStyle().setZIndex(Integer.MAX_VALUE);

        new Timer() {
            @Override
            public void run() {
                opacity += 1;
                top += delta;
                getElement().getStyle().setTop(top, Unit.PX);

                if (opacity >= 10) {
                    getElement().getStyle().setProperty("opacity", "1");
                    cancel();
                } else {
                    getElement().getStyle().setProperty("opacity", "0." + opacity);
                }
            }
        }.scheduleRepeating(50);
    }

    private void close() {
        opacity = 10;

        // Hide animation
        new Timer() {
            @Override
            public void run() {
                opacity--;
                if (opacity <= 0) {
                    cancel();
                    getElement().getParentElement().removeChild(getElement());
                } else {
                    getElement().getStyle().setProperty("opacity", "0." + opacity);
                }
            }
        }.scheduleRepeating(50);
    }

}
