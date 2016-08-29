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
package com.codenvy.machine.interceptor;

import com.google.inject.AbstractModule;

import org.eclipse.che.plugin.docker.machine.DockerInstance;

import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.eclipse.che.inject.Matchers.names;

/**
 * Bind interceptors related to machines.
 *
 * @author Mykola Morhun
 */
public class MachineHostedInterceptorModule extends AbstractModule {

    @Override
    protected void configure() {
        final SnapshottingToRegistryInterceptor snapshottingToRegistryInterceptor = new SnapshottingToRegistryInterceptor();
        requestInjection(snapshottingToRegistryInterceptor);
        bindInterceptor(subclassesOf(DockerInstance.class),
                        names("createAndPushSnapshotToRemoteRegistry"),
                        snapshottingToRegistryInterceptor);
    }

}
