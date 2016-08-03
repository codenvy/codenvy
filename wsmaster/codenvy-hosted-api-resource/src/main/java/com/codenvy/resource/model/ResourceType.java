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
package com.codenvy.resource.model;

import org.eclipse.che.api.core.ConflictException;

/**
 * Represents some kind of resources which can be used by account
 *
 * @author Sergii Leschenko
 */
public interface ResourceType<T extends Resource> {
    /**
     * Returns id of resource type
     */
    String getId();

    /**
     * Returns description of resource type
     */
    String getDescription();

    /**
     * Defines function for aggregating two resources of this type
     *
     * @param resourceA
     *         resources A
     * @param resourceB
     *         resource B
     */
    T aggregate(T resourceA, T resourceB);

    /**
     * Defines function for subtraction two resources of this type
     *
     * @param total
     *         total resource
     * @param deduction
     *         resource that should be deducted from {@code total}
     * @throws ConflictException
     *         when {@code total}'s amount is less than {@code deduction}'s amount
     */
    T deduct(T total, T deduction) throws ConflictException;
}
