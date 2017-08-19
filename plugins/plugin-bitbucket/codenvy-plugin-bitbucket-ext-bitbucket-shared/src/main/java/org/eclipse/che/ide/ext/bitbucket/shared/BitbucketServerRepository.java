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
 * Represents a Bitbucket Server repository.
 *
 * @author Igor Vinokur
 */
@DTO
public interface BitbucketServerRepository extends BitbucketRepository {

  String getName();

  void setName(String name);

  BitbucketServerRepository withName(String name);

  String getSlug();

  void setSlug(String slug);

  BitbucketServerRepository withSlug(String slug);

  BitbucketServerRepository getOrigin();

  void setOrigin(BitbucketServerRepository parent);

  BitbucketServerRepository withOrigin(BitbucketServerRepository parent);

  BitbucketServerProject getProject();

  void setProject(BitbucketServerProject project);

  BitbucketServerRepository withProject(BitbucketServerProject project);
}
