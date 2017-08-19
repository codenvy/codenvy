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
package com.codenvy.template.processor.exception;

/**
 * Should be thrown when unable to resolve HTML template e.g. by given path.
 *
 * @author Anton Korneta
 */
public class TemplateNotFoundException extends Exception {

  public TemplateNotFoundException(String message) {
    super(message);
  }
}
