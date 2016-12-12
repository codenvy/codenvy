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
package com.codenvy.resource.api.license;

import com.codenvy.resource.api.ResourceAggregator;
import com.codenvy.resource.model.AccountLicense;
import com.codenvy.resource.model.ProvidedResources;
import com.codenvy.resource.model.Resource;
import com.codenvy.resource.spi.impl.AccountLicenseImpl;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Facade for Account License related operations.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class AccountLicenseManager {
    private final Set<ResourcesProvider> resourcesProviders;
    private final ResourceAggregator     resourceAggregator;

    @Inject
    public AccountLicenseManager(Set<ResourcesProvider> resourcesProviders,
                                 ResourceAggregator resourceAggregator) {
        this.resourcesProviders = resourcesProviders;
        this.resourceAggregator = resourceAggregator;
    }

    /**
     * Returns license which given account can use.
     *
     * @param accountId
     *         account id
     * @return license which can be used by given account
     * @throws NotFoundException
     *         when account with specified id was not found
     * @throws ServerException
     *         when some exception occurs
     */
    public AccountLicense getByAccount(String accountId) throws NotFoundException, ServerException {
        final List<ProvidedResources> resources = new ArrayList<>();
        for (ResourcesProvider resourcesProvider : resourcesProviders) {
            resources.addAll(resourcesProvider.getResources(accountId));
        }

        final List<Resource> allResources = resources.stream()
                                                     .flatMap(providedResources -> providedResources.getResources().stream())
                                                     .collect(Collectors.toList());

        return new AccountLicenseImpl(accountId,
                                      resources,
                                      new ArrayList<>(resourceAggregator.aggregateByType(allResources).values()));
    }
}
