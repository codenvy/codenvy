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
package com.codenvy.resource.spi.impl;

import com.codenvy.resource.model.FreeResourcesLimit;
import com.codenvy.resource.model.Resource;

import org.eclipse.che.account.spi.AccountImpl;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Data object for {@link FreeResourcesLimit}.
 *
 * @author Sergii Leschenko
 */
@Entity(name = "FreeResourcesLimit")
@NamedQueries(
        {
                @NamedQuery(name = "FreeResourcesLimit.get",
                            query = "SELECT limit FROM FreeResourcesLimit limit WHERE limit.accountId= :accountId"),
                @NamedQuery(name = "FreeResourcesLimit.getAll",
                            query = "SELECT limit FROM FreeResourcesLimit limit"),
                @NamedQuery(name = "FreeResourcesLimit.getTotalCount",
                            query = "SELECT COUNT(limit) FROM FreeResourcesLimit limit")
        }
)
public class FreeResourcesLimitImpl implements FreeResourcesLimit {
    @Id
    private String accountId;

    @PrimaryKeyJoinColumn
    private AccountImpl account;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable
    private List<ResourceImpl> resources;

    public FreeResourcesLimitImpl() {
    }

    public FreeResourcesLimitImpl(FreeResourcesLimit freeResourcesLimit) {
        this(freeResourcesLimit.getAccountId(),
             freeResourcesLimit.getResources());
    }

    public FreeResourcesLimitImpl(String accountId, List<? extends Resource> resources) {
        this.accountId = accountId;
        if (resources != null) {
            this.resources = resources.stream()
                                      .map(ResourceImpl::new)
                                      .collect(Collectors.toList());
        }
    }

    @Override
    public String getAccountId() {
        return accountId;
    }

    @Override
    public List<ResourceImpl> getResources() {
        if (resources == null) {
            resources = new ArrayList<>();
        }
        return resources;
    }

    public void setResources(List<ResourceImpl> resources) {
        this.resources = resources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FreeResourcesLimitImpl)) return false;
        FreeResourcesLimitImpl that = (FreeResourcesLimitImpl)o;
        return Objects.equals(accountId, that.accountId) &&
               Objects.equals(getResources(), that.getResources());
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, getResources());
    }

    @Override
    public String toString() {
        return "FreeResourcesLimitImpl{" +
               "accountId='" + accountId + '\'' +
               ", resources=" + getResources() +
               '}';
    }
}
