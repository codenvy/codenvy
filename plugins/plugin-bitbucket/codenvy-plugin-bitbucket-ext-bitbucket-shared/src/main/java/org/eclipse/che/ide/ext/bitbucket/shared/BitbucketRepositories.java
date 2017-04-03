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
package org.eclipse.che.ide.ext.bitbucket.shared;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Represents a list of {@link BitbucketRepository}.
 *
 * @author Kevin Pollet
 */
@DTO
public interface BitbucketRepositories {
    List<BitbucketRepository> getRepositories();

    void setRepositories(List<BitbucketRepository> repositories);

    BitbucketRepositories withRepositories(List<BitbucketRepository> repositories);
}
