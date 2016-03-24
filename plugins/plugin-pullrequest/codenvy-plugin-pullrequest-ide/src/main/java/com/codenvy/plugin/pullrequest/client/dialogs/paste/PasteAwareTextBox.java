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
package com.codenvy.plugin.pullrequest.client.dialogs.paste;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextBox;

/**
 * {@link TextBox} that handles onpaste events.
 */
public class PasteAwareTextBox extends TextBox {

    public PasteAwareTextBox() {
        sinkEvents(Event.ONPASTE);
    }

    public PasteAwareTextBox(final Element element) {
        super(element);
        sinkEvents(Event.ONPASTE);
    }

    @Override
    public void onBrowserEvent(final Event event) {
        super.onBrowserEvent(event);
        switch (event.getTypeInt()) {
            case Event.ONPASTE:
                event.stopPropagation();
                delayedFireEvent();
                break;
            default:
                break;
        }
    }

    /**
     * Fires an event, after waiting the state of the textbox the be updated.
     */
    private void delayedFireEvent() {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                fireEvent(new PasteEvent());
            }
        });
    }

    /**
     * Adds a {@link PasteHandler} to the component.
     *
     * @param handler
     *         the handler to add
     * @return a registration object for removal
     */
    public HandlerRegistration addPasteHandler(final PasteHandler handler) {
        return addHandler(handler, PasteEvent.TYPE);
    }
}
