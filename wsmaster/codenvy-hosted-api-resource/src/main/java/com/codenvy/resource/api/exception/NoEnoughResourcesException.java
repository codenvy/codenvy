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
package com.codenvy.resource.api.exception;

import com.codenvy.resource.model.Resource;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

/**
 * Thrown in case when account doesn't have enough resources to perform some operation.
 *
 * <p>It contains detailed information about resources so required, available, missing amounts
 * to provide ability to construct user friendly message.
 *
 * @author Sergii Leschenko
 */
public class NoEnoughResourcesException extends Exception {
    private static final String MESSAGE = "Account has %s resources to use, but operation requires %s. It requires more %s.";

    private String                   message;
    private List<? extends Resource> availableResources;
    private List<? extends Resource> requiredResources;
    private List<? extends Resource> missedResources;

    public NoEnoughResourcesException(Resource availableResource, Resource requiredResource, Resource missedResource) {
        this(singletonList(availableResource), singletonList(requiredResource), singletonList(missedResource));
    }

    public NoEnoughResourcesException(List<? extends Resource> availableResources,
                                      List<? extends Resource> requiredResources,
                                      List<? extends Resource> missedResources) {
        this.availableResources = availableResources;
        this.requiredResources = requiredResources;
        this.missedResources = missedResources;
    }

    @Override
    public String getMessage() {
        if (message == null) {
            message = String.format(MESSAGE, resourcesToString(availableResources), resourcesToString(requiredResources),
                                    resourcesToString(missedResources));
        }
        return message;
    }

    public List<? extends Resource> getRequiredResources() {
        return requiredResources;
    }

    public List<? extends Resource> getAvailableResources() {
        return availableResources;
    }

    public List<? extends Resource> getMissingResources() {
        return missedResources;
    }

    private String resourcesToString(List<? extends Resource> resources) {
        return '[' +
               resources.stream()
                        .map(resource -> resource.getAmount() + resource.getUnit() + " " + resource.getType())
                        .collect(Collectors.joining(", "))
               + ']';
    }
}
