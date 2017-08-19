/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.bitbucket.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Represents author of Bitbucket Server pull request.
 *
 * @author Igor Vinokur
 */
@DTO
public interface BitbucketServerAuthor {
  BitbucketServerUser getUser();

  void setUser(BitbucketServerUser user);

  BitbucketServerAuthor withUser(BitbucketServerUser user);
}
