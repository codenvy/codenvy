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

import com.google.gwt.event.shared.GwtEvent;

/**
 * {@link GwtEvent} class for paste events.
 */
public class PasteEvent extends GwtEvent<PasteHandler> {
    /**
     * The type of the event.
     */
    public static Type<PasteHandler> TYPE = new Type<PasteHandler>();

    @Override
    public Type<PasteHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final PasteHandler handler) {
        handler.onPaste(this);
    }
}
