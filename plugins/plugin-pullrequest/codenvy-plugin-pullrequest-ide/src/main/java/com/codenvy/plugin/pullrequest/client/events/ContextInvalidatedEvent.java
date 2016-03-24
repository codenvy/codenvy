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
import com.codenvy.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;

/**
 * This event is fired when context is invalidated.
 *
 * @author Yevhenii Voevodin
 * @see WorkflowExecutor#invalidateContext(ProjectConfigDto)
 */
public class ContextInvalidatedEvent extends GwtEvent<ContextInvalidatedHandler> {

    public static final Type<ContextInvalidatedHandler> TYPE = new Type<>();

    private final Context context;

    public ContextInvalidatedEvent(final Context context) {
        this.context = context;
    }

    @Override
    public Type<ContextInvalidatedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ContextInvalidatedHandler handler) {
        handler.onContextInvalidated(context);
    }
}
