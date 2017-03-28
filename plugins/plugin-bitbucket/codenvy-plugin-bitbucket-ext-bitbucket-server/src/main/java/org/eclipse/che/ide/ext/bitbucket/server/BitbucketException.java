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
