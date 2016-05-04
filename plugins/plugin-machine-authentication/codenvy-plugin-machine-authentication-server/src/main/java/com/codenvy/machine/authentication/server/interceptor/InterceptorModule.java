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
package com.codenvy.machine.authentication.server.interceptor;

import com.google.inject.AbstractModule;

import org.eclipse.che.api.workspace.server.WorkspaceManager;

import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.eclipse.che.inject.Matchers.names;


/**
 * Guice interceptor bindings.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
public class InterceptorModule extends AbstractModule {

    @Override
    protected void configure() {
        final MachineTokenInterceptor tokenInterceptor = new MachineTokenInterceptor();
        requestInjection(tokenInterceptor);
        bindInterceptor(subclassesOf(WorkspaceManager.class), names("startWorkspace"), tokenInterceptor);
        bindInterceptor(subclassesOf(WorkspaceManager.class), names("recoverWorkspace"), tokenInterceptor);
        bindInterceptor(subclassesOf(WorkspaceManager.class), names("stopWorkspace"), tokenInterceptor);
    }
}
