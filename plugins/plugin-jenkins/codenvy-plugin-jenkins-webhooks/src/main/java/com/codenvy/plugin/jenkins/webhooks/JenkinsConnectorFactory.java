/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.jenkins.webhooks;

import com.google.inject.assistedinject.Assisted;

/**
 * Provides {@link JenkinsConnector} instances.
 *
 * @author Igor Vinokur
 */
public interface JenkinsConnectorFactory {
    JenkinsConnector create(@Assisted("url") String url, @Assisted("jobName") String jobName);
}
