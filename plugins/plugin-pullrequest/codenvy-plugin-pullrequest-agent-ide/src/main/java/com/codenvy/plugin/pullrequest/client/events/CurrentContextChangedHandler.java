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
import com.google.gwt.event.shared.EventHandler;

/**
 * Handler for {@link CurrentContextChangedEvent}.
 *
 * @author Yevhenii Voevodin
 */
public interface CurrentContextChangedHandler extends EventHandler {

    /**
     * Called when the current context changed.
     *
     * @param context
     *         new context
     */
    void onContextChanged(final Context context);
}
