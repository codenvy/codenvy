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
package org.eclipse.che.ide.ext.bitbucket.shared;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Represents a repositories page in the Bitbucket Server rest API.
 *
 * @author Igor Vinokur
 */
@DTO
public interface BitbucketServerRepositoriesPage extends BitbucketServerPage {

    List<BitbucketServerRepository> getValues();

    void setValues(List<BitbucketServerRepository> values);

    BitbucketServerRepositoriesPage withValues(List<BitbucketServerRepository> values);

    int getNextPageStart();

    void setNextPageStart(int nextPageStart);

    BitbucketServerRepositoriesPage withNextPageStart(int nextPageStart);
}
