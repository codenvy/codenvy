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

/**
 * Base fields of the paged response in the Bitbucket Server rest API.
 *
 * @author Igor Vinokur
 */
public interface BitbucketServerPage {
  int getSize();

  void setSize(int size);

  BitbucketServerPage withSize(int size);

  int getLimit();

  void setLimit(int limit);

  BitbucketServerPage withLimit(int limit);

  boolean isIsLastPage();

  void setIsLastPage(boolean isLastPage);

  BitbucketServerPage withIsLastPage(boolean isLastPage);

  int getStart();

  void setStart(int start);

  BitbucketServerPage withStart(int start);

  int getNextPageStart();

  void setNextPageStart(int nextPageStart);

  BitbucketServerPage withNextPageStart(int nextPageStart);
}
