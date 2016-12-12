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
package com.codenvy.resource.api.free;

import com.codenvy.resource.api.license.ResourcesProvider;
import com.codenvy.resource.model.FreeResourcesLimit;
import com.codenvy.resource.model.ProvidedResources;
import com.codenvy.resource.model.Resource;
import com.codenvy.resource.spi.impl.ProvidedResourcesImpl;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;

/**
 * Provides free resources for account usage.
 *
 * <p>Returns free resources limits if it is specified for given account
 * or default free resources limit in other case
 *
 * <p>Default resources should be provided by {@link DefaultResourcesProvider}
 * for different account types
 *
 * @author Sergii Leschenko
 */
@Singleton
public class FreeResourcesProvider implements ResourcesProvider {
    public static final String FREE_RESOURCES_PROVIDER = "free";

    private final FreeResourcesLimitManager             freeResourcesLimitManager;
    private final AccountManager                        accountManager;
    private final Map<String, DefaultResourcesProvider> defaultResourcesProviders;

    @Inject
    public FreeResourcesProvider(FreeResourcesLimitManager freeResourcesLimitManager,
                                 AccountManager accountManager,
                                 Set<DefaultResourcesProvider> defaultResourcesProviders) {
        this.freeResourcesLimitManager = freeResourcesLimitManager;
        this.accountManager = accountManager;
        this.defaultResourcesProviders = defaultResourcesProviders.stream()
                                                                  .collect(toMap(DefaultResourcesProvider::getAccountType,
                                                                                 Function.identity()));
    }

    @Override
    public List<ProvidedResources> getResources(String accountId) throws ServerException, NotFoundException {
        Map<String, ResourceImpl> freeResources = new HashMap<>();
        String limitId = null;
        try {
            FreeResourcesLimit resourcesLimit = freeResourcesLimitManager.get(accountId);
            for (Resource resource : resourcesLimit.getResources()) {
                freeResources.put(resource.getType(), new ResourceImpl(resource));
            }
            limitId = resourcesLimit.getAccountId();
        } catch (NotFoundException ignored) {
            // there is no resources limit for given account
        }

        // add default resources which are not specified by limit
        for (ResourceImpl resource : getDefaultResources(accountId)) {
            freeResources.putIfAbsent(resource.getType(), resource);
        }

        return singletonList(new ProvidedResourcesImpl(FREE_RESOURCES_PROVIDER,
                                                       limitId,
                                                       accountId,
                                                       -1L,
                                                       -1L,
                                                       freeResources.values()));
    }

    private List<ResourceImpl> getDefaultResources(String accountId) throws NotFoundException, ServerException {
        List<ResourceImpl> defaultResources = new ArrayList<>();
        final Account account = accountManager.getById(accountId);

        final DefaultResourcesProvider defaultResourcesProvider = defaultResourcesProviders.get(account.getType());
        if (defaultResourcesProvider != null) {
            defaultResources.addAll(defaultResourcesProvider.getResources(accountId));
        }

        return defaultResources;
    }
}
