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

import com.codenvy.organization.api.OrganizationManager;
import com.codenvy.organization.shared.model.Organization;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.codenvy.resource.api.ResourcesProvider;
import com.codenvy.resource.api.ram.RamResourceType;
import com.codenvy.resource.model.FreeResourcesLimit;
import com.codenvy.resource.model.Resource;
import com.codenvy.resource.spi.impl.ProvidedResourcesImpl;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.lang.Size;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

/**
 * Provides free resources for account usage.
 *
 * Returns free resources limits if it is specified for given account
 * and default free resources limit in other case
 *
 * @author Sergii Leschenko
 */
@Singleton
public class FreeResourcesProvider implements ResourcesProvider {
    public static final String FREE_RESOURCES_PROVIDER = "free";

    private final FreeResourcesLimitManager freeResourcesLimitManager;
    private final AccountManager            accountManager;
    private final OrganizationManager       organizationManager;
    private final long                      ramPerUser;
    private final long                      ramPerOrganization;

    @Inject
    public FreeResourcesProvider(FreeResourcesLimitManager freeResourcesLimitManager,
                                 AccountManager accountManager,
                                 OrganizationManager organizationManager,
                                 @Named("limits.user.workspaces.ram") String ramPerUser,
                                 @Named("limits.organization.workspaces.ram") String ramPerOrganization) {
        this.freeResourcesLimitManager = freeResourcesLimitManager;
        this.accountManager = accountManager;
        this.organizationManager = organizationManager;
        this.ramPerUser = "-1".equals(ramPerUser) ? -1 : Size.parseSizeToMegabytes(ramPerUser);
        this.ramPerOrganization = "-1".equals(ramPerOrganization) ? -1 : Size.parseSizeToMegabytes(ramPerOrganization);
    }

    @Override
    public List<ProvidedResourcesImpl> getResources(String accountId) throws ServerException, NotFoundException {
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
        if (UserImpl.PERSONAL_ACCOUNT.equals(account.getType())) {
            defaultResources.add(new ResourceImpl(RamResourceType.ID, ramPerUser, RamResourceType.UNIT));
        } else if (OrganizationImpl.ORGANIZATIONAL_ACCOUNT.equals(account.getType())) {
            final Organization organization = organizationManager.getById(accountId);
            // only root organizations should have own resources
            // suborganization will use resources of its parent organization. Will be implemented soon
            if (organization.getParent() == null) {
                defaultResources.add(new ResourceImpl(RamResourceType.ID, ramPerOrganization, RamResourceType.UNIT));
            }
        }
        return defaultResources;
    }
}
