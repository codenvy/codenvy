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

/**
 * Handler to be advised when a property of the context object is changed.
 *
 * @author Kevin Pollet
 */
public interface ContextPropertyChangeHandler extends EventHandler {
    /**
     * Called when a property of the context object changed.
     *
     * @param event
     *         the {@link ContextPropertyChangeEvent} event.
     */
    void onContextPropertyChange(ContextPropertyChangeEvent event);
}
