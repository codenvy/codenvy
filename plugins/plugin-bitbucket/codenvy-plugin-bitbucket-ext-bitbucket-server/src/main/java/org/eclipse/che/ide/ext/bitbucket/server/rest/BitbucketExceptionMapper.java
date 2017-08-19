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
package org.eclipse.che.ide.ext.bitbucket.server.rest;

import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.eclipse.che.ide.ext.bitbucket.server.BitbucketException;

/**
 * The exception mapper.
 *
 * @author Kevin Pollet
 */
@Singleton
@Provider
public class BitbucketExceptionMapper implements ExceptionMapper<BitbucketException> {

  /** @see javax.ws.rs.ext.ExceptionMapper#toResponse(Throwable) */
  @Override
  public Response toResponse(final BitbucketException exception) {
    return Response.status(exception.getResponseStatus())
        .header("JAXRS-Body-Provided", "Error-Message")
        .entity(exception.getMessage())
        .type(exception.getContentType())
        .build();
  }
}
