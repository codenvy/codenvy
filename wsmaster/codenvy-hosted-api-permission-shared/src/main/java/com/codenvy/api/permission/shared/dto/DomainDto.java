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
package com.codenvy.api.permission.shared.dto;

import com.codenvy.api.permission.shared.model.PermissionsDomain;
import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/** @author Sergii Leschenko */
@DTO
public interface DomainDto extends PermissionsDomain {
  @Override
  String getId();

  void setId(String id);

  DomainDto withId(String id);

  @Override
  List<String> getAllowedActions();

  void setAllowedActions(List<String> allowedActions);

  DomainDto withAllowedActions(List<String> allowedActions);

  @Override
  Boolean isInstanceRequired();

  void setInstanceRequired(Boolean isInstanceRequired);

  DomainDto withInstanceRequired(Boolean isInstanceRequired);
}
