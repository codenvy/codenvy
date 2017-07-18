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
package org.eclipse.che.ide.ext.bitbucket.server;

/**
 * If Bitbucket returns unexpected or error status for request.
 *
 * @author Kevin Pollet
 */
public class BitbucketException extends Exception {
    private final int    responseStatus;
    private final String contentType;

    /**
     * Constructs an instance of {@link org.eclipse.che.ide.ext.bitbucket.server.BitbucketException}.
     *
     * @param responseStatus
     *         HTTP status of response from Bitbucket server.
     * @param message
     *         the exception message.
     * @param contentType
     *         content type of response from Bitbucket server.
     */
    public BitbucketException(final int responseStatus, final String message, final String contentType) {
        super(message);
        this.responseStatus = responseStatus;
        this.contentType = contentType;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public String getContentType() {
        return contentType;
    }
}
