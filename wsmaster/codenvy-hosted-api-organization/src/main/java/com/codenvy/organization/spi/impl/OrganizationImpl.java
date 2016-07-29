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

import com.codenvy.organization.shared.model.Organization;
import com.codenvy.organization.spi.jpa.OrganizationEntityListener;

import org.eclipse.che.account.spi.AccountImpl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.Objects;

/**
 * Data object for {@link Organization}.
 *
 * @author Sergii Leschenko
 */
@Entity(name = "Organization")
@EntityListeners(OrganizationEntityListener.class)
@NamedQueries(
        {
                @NamedQuery(name = "Organization.getByName",
                            query = "SELECT o " +
                                    "FROM Organization o " +
                                    "WHERE o.name = :name"),
                @NamedQuery(name = "Organizations.getByParent",
                            query = "SELECT o " +
                                    "FROM Organization o " +
                                    "WHERE o.parent = :parent")
        }
)
public class OrganizationImpl extends AccountImpl implements Organization {
    public static final String ORGANIZATIONAL_ACCOUNT = "organizational";

    @Column
    private String parent;

    @ManyToOne
    @JoinColumn(name = "parent", insertable = false, updatable = false)
    OrganizationImpl parentObj;

    public OrganizationImpl() {}

    public OrganizationImpl(Organization organization) {
        this.id = organization.getId();
        this.name = organization.getName();
        this.parent = organization.getParent();
    }

    public OrganizationImpl(String id, String name, String parent) {
        this.id = id;
        this.name = name;
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return ORGANIZATIONAL_ACCOUNT;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrganizationImpl)) return false;
        if (!super.equals(o)) return false;
        OrganizationImpl that = (OrganizationImpl)o;
        return Objects.equals(id, that.id)
               && Objects.equals(name, that.name)
               && Objects.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        return 31 * hash + Objects.hashCode(parent);
    }

    @Override
    public String toString() {
        return "OrganizationImpl{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", parent='" + parent + '\'' +
               '}';
    }
}
