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
import com.codenvy.plugin.pullrequest.client.workflow.Step;
import com.google.gwt.event.shared.GwtEvent;

import javax.validation.constraints.NotNull;

/**
 * Event sent when a step is done or in error.
 *
 * @author Kevin Pollet
 */
public class StepEvent extends GwtEvent<StepHandler> {
    public static final Type<StepHandler> TYPE = new Type<>();

    private final Step    step;
    private final boolean success;
    private final String  message;
    private final Context context;

    public StepEvent(final Context context, final Step step, final boolean success) {
        this(context, step, success, null);
    }

    public StepEvent(final Context context, final Step step, final boolean success, final String message) {
        this.step = step;
        this.success = success;
        this.message = message;
        this.context = context;
    }

    @Override
    public Type<StepHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(@NotNull final StepHandler handler) {
        if (success) {
            handler.onStepDone(this);

        } else {
            handler.onStepError(this);
        }
    }

    public Step getStep() {
        return step;
    }

    public String getMessage() {
        return message;
    }

    public Context getContext() {
        return context;
    }
}
