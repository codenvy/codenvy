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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.List;
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
                                    "WHERE o.account.name = :name"),
                @NamedQuery(name = "Organization.getByParent",
                            query = "SELECT o " +
                                    "FROM Organization o " +
                                    "WHERE o.parent = :parent "),
                @NamedQuery(name = "Organization.getSuborganizationsCount",
                            query = "SELECT COUNT(o) " +
                                    "FROM Organization o " +
                                    "WHERE o.parent = :parent "),
        }
)

@Table(indexes = {@Index(columnList = "parent")})
public class OrganizationImpl implements Organization {
    public static final String ORGANIZATIONAL_ACCOUNT = "organizational";

    @Id
    private String id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private AccountImpl account;

    @Column
    private String parent;

    @ManyToOne
    @JoinColumn(name = "parent", insertable = false, updatable = false)
    public OrganizationImpl parentObj;

    public OrganizationImpl() {}

    public OrganizationImpl(Organization organization) {
        this(organization.getId(),
             organization.getName(),
             organization.getParent());
    }

    public OrganizationImpl(String id, String name, String parent) {
        this.id = id;
        this.account = new AccountImpl(id, name, ORGANIZATIONAL_ACCOUNT);
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        if (account != null) {
            return account.getName();
        }
        return null;
    }

    public void setName(String name) {
        if (account != null) {
            account.setName(name);
        }
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrganizationImpl)) {
            return false;
        }
        OrganizationImpl that = (OrganizationImpl)o;
        return Objects.equals(id, that.id)
               && Objects.equals(getName(), that.getName())
               && Objects.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(id);
        hash = 31 * hash + Objects.hashCode(getName());
        hash = 31 * hash + Objects.hashCode(parent);
        return hash;
    }

    @Override
    public String toString() {
        return "OrganizationImpl{" +
               "id='" + id + '\'' +
               ", name='" + getName() + '\'' +
               ", parent='" + parent + '\'' +
               '}';
    }
}
