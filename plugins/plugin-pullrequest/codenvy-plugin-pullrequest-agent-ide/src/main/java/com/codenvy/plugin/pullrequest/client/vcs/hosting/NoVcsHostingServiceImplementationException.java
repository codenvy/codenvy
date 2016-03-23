/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.plugin.pullrequest.client.vcs.hosting;

/**
 * Exception raised when there is no {@link VcsHostingService} implementation for the
 * current project.
 *
 * @author Kevin Pollet
 */
public class NoVcsHostingServiceImplementationException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an instance of {@link VcsHostingService}.
     */
    public NoVcsHostingServiceImplementationException() {
        super("No implementation of the VcsHostingService for the current project");
    }
}
