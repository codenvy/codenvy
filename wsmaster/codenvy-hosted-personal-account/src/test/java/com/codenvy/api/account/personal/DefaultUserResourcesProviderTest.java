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
package com.codenvy.api.account.personal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.codenvy.resource.api.type.RamResourceType;
import com.codenvy.resource.api.type.RuntimeResourceType;
import com.codenvy.resource.api.type.TimeoutResourceType;
import com.codenvy.resource.api.type.WorkspaceResourceType;
import com.codenvy.resource.spi.impl.ResourceImpl;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for {@link DefaultUserResourcesProvider}
 *
 * @author Sergii Leschenko
 */
public class DefaultUserResourcesProviderTest {
  private DefaultUserResourcesProvider resourcesProvider;

  @BeforeMethod
  public void setUp() throws Exception {
    resourcesProvider = new DefaultUserResourcesProvider(20 * 60 * 1000, "2gb", 10, 5);
  }

  @Test
  public void shouldReturnPersonalAccountType() throws Exception {
    //when
    final String accountType = resourcesProvider.getAccountType();

    //then
    assertEquals(accountType, OnpremisesUserManager.PERSONAL_ACCOUNT);
  }

  @Test
  public void shouldProvideDefaultRamResourceForUser() throws Exception {
    //when
    final List<ResourceImpl> defaultResources = resourcesProvider.getResources("user123");

    //then
    assertEquals(defaultResources.size(), 4);
    assertTrue(
        defaultResources.contains(
            new ResourceImpl(RamResourceType.ID, 2048, RamResourceType.UNIT)));
    assertTrue(
        defaultResources.contains(
            new ResourceImpl(WorkspaceResourceType.ID, 10, WorkspaceResourceType.UNIT)));
    assertTrue(
        defaultResources.contains(
            new ResourceImpl(RuntimeResourceType.ID, 5, RuntimeResourceType.UNIT)));
    assertTrue(
        defaultResources.contains(
            new ResourceImpl(TimeoutResourceType.ID, 20, TimeoutResourceType.UNIT)));
  }
}
