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

import com.codenvy.organization.shared.model.Member;

import org.eclipse.che.api.user.server.model.impl.UserImpl;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.List;
import java.util.Objects;

/**
 * Data object for {@link Member}.
 *
 * @author Sergii Leschenko
 */
@Entity(name = "Member")
@NamedQueries(
        {
                @NamedQuery(name = "Member.getMember",
                            query = "SELECT m " +
                                    "FROM Member m " +
                                    "WHERE m.userId = :userId AND m.organizationId = :organizationId"),
                @NamedQuery(name = "Member.getByOrganization",
                            query = "SELECT m " +
                                    "FROM Member m " +
                                    "WHERE m.organizationId = :organizationId"),
                @NamedQuery(name = "Member.getByUser",
                            query = "SELECT m " +
                                    "FROM Member m " +
                                    "WHERE m.userId = :userId"),
                @NamedQuery(name = "Member.getOrganizations",
                            query = "SELECT org " +
                                    "FROM Member m, m.organization org " +
                                    "WHERE m.userId = :userId")
        }
)
public class MemberImpl implements Member {
    @Id
    private String userId;

    @Id
    private String organizationId;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "id",
                insertable = false, updatable = false)
    private UserImpl user;

    @ManyToOne
    @JoinColumn(name = "organizationId", referencedColumnName = "id",
                insertable = false, updatable = false)
    private OrganizationImpl organization;


    @ElementCollection
    private List<String> actions;

    public MemberImpl() {
    }

    public MemberImpl(String userId, String organizationId, List<String> actions) {
        this.userId = userId;
        this.organizationId = organizationId;
        this.actions = actions;
    }

    public String getUserId() {
        return userId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public List<String> getActions() {
        return actions;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MemberImpl)) return false;
        final MemberImpl other = (MemberImpl)obj;
        return Objects.equals(userId, other.userId)
               && Objects.equals(organizationId, other.organizationId)
               && actions.equals(other.actions);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(userId);
        hash = 31 * hash + Objects.hashCode(organizationId);
        hash = 31 * hash + actions.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "MemberImpl{" +
               "userId='" + userId + '\'' +
               ", organizationId='" + organizationId + '\'' +
               ", actions=" + actions +
               '}';
    }
}
