/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package org.eclipse.che.ide.ext.bitbucket.server.rest;

import org.eclipse.che.ide.ext.bitbucket.server.BitbucketException;

import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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
