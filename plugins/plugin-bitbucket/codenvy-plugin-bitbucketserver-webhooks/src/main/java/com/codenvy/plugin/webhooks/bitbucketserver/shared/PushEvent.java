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
package com.codenvy.plugin.webhooks.bitbucketserver.shared;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Represents Bitbucket Server push event.
 *
 * @author Igor Vinokur
 */
@DTO
public interface PushEvent {

    Repository getRepository();

    void setRepository(Repository repository);

    PushEvent withRepository(Repository repository);

    List<RefChange> getRefChanges();

    void setRefChanges(List<RefChange> refChanges);

    PushEvent withRefChanges(List<RefChange> refChanges);

    Changesets getChangesets();

    void setChangesets(Changesets changesets);

    PushEvent withChangesets(Changesets changesets);
}
