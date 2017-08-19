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
package com.codenvy.api.workspace.server.stack;

import static com.codenvy.api.workspace.server.stack.StackDomain.SEARCH;
import static java.lang.String.format;
import static java.util.Collections.singletonList;

import com.codenvy.api.workspace.server.spi.jpa.JpaStackPermissionsDao;
import java.nio.file.Path;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.stack.StackLoader;
import org.eclipse.che.core.db.DBInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads predefined stacks and allows you to search for them to any user.
 *
 * @author Anton Korneta
 */
@Singleton
public class OnPremisesStackLoader extends StackLoader {

  private static final Logger LOG = LoggerFactory.getLogger(OnPremisesStackLoader.class);

  private final JpaStackPermissionsDao permissionsDao;

  @Inject
  @SuppressWarnings("unused")
  public OnPremisesStackLoader(
      @Named("codenvy.predefined.stacks.reload_on_start") boolean reloadStacksOnStart,
      @Named(CHE_PREDEFINED_STACKS) Map<String, String> stacks2images,
      StackDao stackDao,
      DBInitializer dbInitializer,
      JpaStackPermissionsDao permissionsDao) {
    super(reloadStacksOnStart, stacks2images, stackDao, dbInitializer);
    this.permissionsDao = permissionsDao;
  }

  protected void loadStack(StackImpl stack, Path imagePath) {
    setIconData(stack, imagePath);
    try {
      try {
        stackDao.update(stack);
      } catch (NotFoundException ignored) {
        stackDao.create(stack);
      }
      permissionsDao.store(new StackPermissionsImpl("*", stack.getId(), singletonList(SEARCH)));
    } catch (ServerException | ConflictException ex) {
      LOG.warn(format("Failed to load stack with id '%s' ", stack.getId()), ex.getMessage());
    }
  }
}
