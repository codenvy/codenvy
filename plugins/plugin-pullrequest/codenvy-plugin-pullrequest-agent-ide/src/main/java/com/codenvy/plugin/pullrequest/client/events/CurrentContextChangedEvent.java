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
package com.codenvy.plugin.pullrequest.client.events;

import com.codenvy.plugin.pullrequest.client.workflow.Context;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Sent when current plugin context is changed to an existing one.
 *
 * <p>Note that if context is just created then this event won't be fired.
 *
 * @author Yevhenii Voevodin
 */
public class CurrentContextChangedEvent extends GwtEvent<CurrentContextChangedHandler> {

    public static final Type<CurrentContextChangedHandler> TYPE = new Type<>();

    private final Context context;

    public CurrentContextChangedEvent(final Context context) {
        this.context = context;
    }

    @Override
    public Type<CurrentContextChangedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CurrentContextChangedHandler handler) {
        handler.onContextChanged(context);
    }
}
