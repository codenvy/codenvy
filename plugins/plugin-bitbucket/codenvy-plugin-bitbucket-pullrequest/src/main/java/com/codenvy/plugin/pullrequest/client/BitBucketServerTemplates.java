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
package com.codenvy.plugin.pullrequest.client;

import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.HostingServiceTemplates;

/**
 * Templates for Bitbucket Server constants.
 *
 * @author Igor Vinokur
 */
public interface BitBucketServerTemplates extends HostingServiceTemplates {
    @DefaultMessage("{0}/{1}.git")
    String sshUrlTemplate(String path, String repository);

    @DefaultMessage("{0}/{1}.git")
    String httpUrlTemplate(String url, String repository);

    @DefaultMessage("{0}/repos/{1}/pull-requests/{2}")
    String pullRequestUrlTemplate(String url, String repository, String pullRequestNumber);

    @DefaultMessage("[![Review]({0}//{1}/factory/resources/factory-review.svg)]({2})")
    String formattedReviewFactoryUrlTemplate(String protocol, String host, String reviewFactoryUrl);
}
