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

import com.codenvy.resource.api.exception.NoEnoughResourcesException;

import java.util.Set;

/**
 * Represents some kind of resources which can be used by account.
 *
 * @author Sergii Leschenko
 */
public interface ResourceType {
    /**
     * Returns id of resource type.
     */
    String getId();

    /**
     * Returns description of resource type.
     */
    String getDescription();

    /**
     * Returns supported units.
     */
    Set<String> getSupportedUnits();

    /**
     * Returns default unit.
     */
    String getDefaultUnit();

    /**
     * Defines function for aggregating two resources of this type.
     *
     * @param resourceA
     *         resources A
     * @param resourceB
     *         resource B
     * @throws IllegalArgumentException
     *         if one of resources has unsupported type or unit
     */
    Resource aggregate(Resource resourceA, Resource resourceB);

    /**
     * Defines function for subtraction two resources of this type.
     *
     * @param total
     *         total resource
     * @param deduction
     *         resource that should be deducted from {@code total}
     * @throws IllegalArgumentException
     *         if one of resources has unsupported type or unit
     * @throws NoEnoughResourcesException
     *         when {@code total}'s amount is less than {@code deduction}'s amount
     */
    Resource deduct(Resource total, Resource deduction) throws NoEnoughResourcesException;
}
