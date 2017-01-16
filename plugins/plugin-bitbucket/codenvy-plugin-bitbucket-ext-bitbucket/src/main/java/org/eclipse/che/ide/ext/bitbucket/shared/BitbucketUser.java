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

/**
 * Represents a Bitbucket user.
 *
 * @author Kevin Pollet
 */
@DTO
public interface BitbucketUser {
    String getUsername();

    void setUsername(String username);

    BitbucketUser withUsername(String username);


    String getDisplayName();

    void setDisplayName(String displayName);

    BitbucketUser withDisplayName(String displayName);

    String getUuid();

    void setUuid(String uuid);

    BitbucketUser withUuid(String uuid);

    BitbucketUserLinks getLinks();

    void setLinks(BitbucketUserLinks links);

    BitbucketUser withLinks(BitbucketUserLinks links);

    @DTO
    interface BitbucketUserLinks {
        BitbucketLink getSelf();

        void setSelf(BitbucketLink self);

        BitbucketUserLinks withSelf(BitbucketLink self);
    }
}
