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
package com.codenvy.plugin.webhooks.vsts.shared;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface GenericEvent {

    /**
     * Get event subscription id.
     *
     * @return {@link String} subscriptionId
     */
    String getSubscriptionId();

    void setSubscriptionId(final String subscriptionId);

    GenericEvent withSubscriptionId(final String subscriptionId);

    /**
     * Get event notification id.
     *
     * @return {@link int} notificationId
     */
    int getNotificationId();

    void setNotificationId(final int notificationId);

    GenericEvent withNotificationId(final int notificationId);

    /**
     * Get event id.
     *
     * @return {@link String} id
     */
    String getId();

    void setId(final String id);

    GenericEvent withId(final String id);

    /**
     * Get event event type.
     *
     * @return {@link String} eventType
     */
    String getEventType();

    void setEventType(final String eventType);

    GenericEvent withEventType(final String eventType);

    /**
     * Get event publisher id.
     *
     * @return {@link String} publisherId
     */
    String getPublisherId();

    void setPublisherId(final String publisherId);

    GenericEvent withPublisherId(final String publisherId);

    /**
     * Get event message.
     *
     * @return {@link Message} message
     */
    Message getMessage();

    void setMessage(final Message message);

    GenericEvent withMessage(final Message message);

    /**
     * Get event detailed message.
     *
     * @return {@link Message} detailedMessage
     */
    Message getDetailedMessage();

    void setDetailedMessage(final Message detailedMessage);

    GenericEvent withDetailedMessage(final Message detailedMessage);

    /**
     * Get event resource version.
     *
     * @return {@link String} resourceVersion
     */
    String getResourceVersion();

    void setResourceVersion(final String resourceVersion);

    GenericEvent withResourceVersion(final String resourceVersion);

    /**
     * Get event resource containers.
     *
     * @return {@link ResourceContainers} resourceContainers
     */
    ResourceContainers getResourceContainers();

    void setResourceContainers(final ResourceContainers resourceContainers);

    GenericEvent withResourceContainers(final ResourceContainers resourceContainers);

    /**
     * Get event created date.
     *
     * @return {@link String} createdDate
     */
    String getCreatedDate();

    void setCreatedDate(final String createdDate);

    GenericEvent withCreatedDate(final String createdDate);
}
