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
package com.codenvy.organization.spi.impl;

import com.codenvy.organization.shared.model.OrganizationDistributedResources;
import com.codenvy.resource.model.Resource;
import com.codenvy.resource.spi.impl.ResourceImpl;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Data object for {@link OrganizationDistributedResources}.
 *
 * @author Sergii Leschenko
 */
@Entity(name = "OrganizationDistributedResources")
@NamedQueries(
        {
                @NamedQuery(name = "OrganizationDistributedResources.get",
                            query = "SELECT r " +
                                    "FROM OrganizationDistributedResources r " +
                                    "WHERE r.organizationId = :organizationId"),
                @NamedQuery(name = "OrganizationDistributedResources.getByParent",
                            query = "SELECT r " +
                                    "FROM OrganizationDistributedResources r " +
                                    "WHERE r.organization.parent = :parent"),
                @NamedQuery(name = "OrganizationDistributedResources.getCountByParent",
                            query = "SELECT COUNT(r) " +
                                    "FROM OrganizationDistributedResources r " +
                                    "WHERE r.organization.parent = :parent")
        }
)
@Table(name =  "organization_distributed_resources")
public class OrganizationDistributedResourcesImpl implements OrganizationDistributedResources {
    @Id
    @Column(name = "organization_id")
    private String organizationId;

    @PrimaryKeyJoinColumn
    private OrganizationImpl organization;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "organization_distributed_resources_resource",
               joinColumns = @JoinColumn(name = "organization_distributed_resources_id"),
               inverseJoinColumns = @JoinColumn(name = "resource_id"))
    private List<ResourceImpl> resources;

    public OrganizationDistributedResourcesImpl() {
    }

    public OrganizationDistributedResourcesImpl(OrganizationDistributedResources organizationDistributedResource) {
        this(organizationDistributedResource.getOrganizationId(),
             organizationDistributedResource.getResources());
    }

    public OrganizationDistributedResourcesImpl(String organizationId,
                                                List<? extends Resource> resources) {
        this.organizationId = organizationId;
        if (resources != null) {
            this.resources = resources.stream()
                                      .map(ResourceImpl::new)
                                      .collect(Collectors.toList());
        }
    }

    @Override
    public String getOrganizationId() {
        return organizationId;
    }

    @Override
    public List<ResourceImpl> getResources() {
        if (resources == null) {
            resources = new ArrayList<>();
        }
        return resources;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OrganizationDistributedResourcesImpl)) {
            return false;
        }
        final OrganizationDistributedResourcesImpl that = (OrganizationDistributedResourcesImpl)obj;
        return Objects.equals(organizationId, that.organizationId)
               && Objects.equals(organization, that.organization)
               && getResources().equals(that.getResources());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(organizationId);
        hash = 31 * hash + Objects.hashCode(organization);
        hash = 31 * hash + getResources().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "OrganizationDistributedResourcesImpl{" +
               "organizationId='" + organizationId + '\'' +
               ", organization=" + organization +
               ", resources=" + resources +
               '}';
    }
}
