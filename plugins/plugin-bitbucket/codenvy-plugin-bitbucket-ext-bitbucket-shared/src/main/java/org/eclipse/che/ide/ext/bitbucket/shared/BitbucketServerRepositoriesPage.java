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
 * Represents a repositories page in the Bitbucket Server rest API.
 *
 * @author Igor Vinokur
 */
@DTO
public interface BitbucketServerRepositoriesPage extends BitbucketServerPage {

    List<BitbucketServerRepository> getValues();

    void setValues(List<BitbucketServerRepository> values);

    BitbucketServerRepositoriesPage withValues(List<BitbucketServerRepository> values);

    int getNextPageStart();

    void setNextPageStart(int nextPageStart);

    BitbucketServerRepositoriesPage withNextPageStart(int nextPageStart);
}
