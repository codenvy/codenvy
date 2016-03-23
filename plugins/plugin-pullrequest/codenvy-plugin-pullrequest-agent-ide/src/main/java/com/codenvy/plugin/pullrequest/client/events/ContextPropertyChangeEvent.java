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

import javax.validation.constraints.NotNull;

/**
 * @author Kevin Pollet
 */
public class ContextPropertyChangeEvent extends GwtEvent<ContextPropertyChangeHandler> {
    public static Type<ContextPropertyChangeHandler> TYPE = new Type<>();

    private final Context         context;
    private final ContextProperty contextProperty;

    public ContextPropertyChangeEvent(@NotNull final Context context, @NotNull final ContextProperty contextProperty) {
        this.context = context;
        this.contextProperty = contextProperty;
    }

    /**
     * Returns the context object.
     *
     * @return the context object.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Returns the property changed.
     *
     * @return the property changed.
     */
    public ContextProperty getContextProperty() {
        return contextProperty;
    }

    @Override
    public Type<ContextPropertyChangeHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final ContextPropertyChangeHandler handler) {
        handler.onContextPropertyChange(this);
    }

    public enum ContextProperty {
        PROJECT,
        ORIGIN_REPOSITORY_OWNER,
        ORIGIN_REPOSITORY_NAME,
        CONTRIBUTE_TO_BRANCH_NAME,
        WORK_BRANCH_NAME
    }
}
