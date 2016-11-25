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

import com.codenvy.api.permission.server.model.impl.AbstractPermissions;
import com.codenvy.organization.api.permissions.OrganizationDomain;
import com.codenvy.organization.shared.model.Member;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.List;

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
                @NamedQuery(name = "Member.getCountByOrganizationId",
                            query = "SELECT COUNT(m) " +
                                    "FROM Member m " +
                                    "WHERE m.organizationId = :organizationId"),
                @NamedQuery(name = "Member.getByUser",
                            query = "SELECT m " +
                                    "FROM Member m " +
                                    "WHERE m.userId = :userId"),
                @NamedQuery(name = "Member.getOrganizations",
                            query = "SELECT org " +
                                    "FROM Member m, m.organization org " +
                                    "WHERE m.userId = :userId"),
                @NamedQuery(name = "Member.getOrganizationsCount",
                            query = "SELECT COUNT(m) " +
                                    "FROM Member m " +
                                    "WHERE m.userId = :userId ")
        }
)
@Table(name = "member")
public class MemberImpl extends AbstractPermissions implements Member {
    @Column(name = "organizationid")
    private String organizationId;

    @ManyToOne
    @JoinColumn(name = "organizationid", referencedColumnName = "id",
                insertable = false, updatable = false)
    private OrganizationImpl organization;

    public MemberImpl() {
    }

    public MemberImpl(String userId, String organizationId, List<String> actions) {
        super(userId, actions);
        this.organizationId = organizationId;
    }

    public MemberImpl(Member member) {
        this(member.getUserId(), member.getOrganizationId(), member.getActions());
    }

    @Override
    public String getInstanceId() {
        return organizationId;
    }

    @Override
    public String getDomainId() {
        return OrganizationDomain.DOMAIN_ID;
    }

    public String getOrganizationId() {
        return organizationId;
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
