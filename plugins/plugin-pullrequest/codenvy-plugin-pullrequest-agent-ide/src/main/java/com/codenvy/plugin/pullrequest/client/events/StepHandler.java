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

import com.google.gwt.event.shared.EventHandler;

import javax.validation.constraints.NotNull;

/**
 * Handler for step event.
 *
 * @author Kevin Pollet
 */
public interface StepHandler extends EventHandler {
    /**
     * Called when a step is successfully done.
     *
     * @param event
     *         the step event.
     */
    void onStepDone(@NotNull StepEvent event);

    /**
     * Called when a step is in error.
     *
     * @param event
     *         the step event.
     */
    void onStepError(@NotNull StepEvent event);
}
