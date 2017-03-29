/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.api.invite;

import com.codenvy.api.workspace.server.WorkspaceDomain;
import com.codenvy.organization.api.permissions.OrganizationDomain;
import com.codenvy.shared.invite.model.Invite;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data object for {@link Invite}.
 *
 * @author Sergii Leschenko
 */
@Entity(name = "Invite")
@Table(name = "codenvy_invite")
@NamedQueries(
        {
                @NamedQuery(name = "Invite.get",
                            query = "SELECT i " +
                                    "FROM Invite i " +
                                    "WHERE i.email = :email AND i.domainId = :domain " +
                                    "      AND (i.workspaceId = :instance OR i.organizationId = :instance)"),
                @NamedQuery(name = "Invite.getByEmail",
                            query = "SELECT i " +
                                    "FROM Invite i " +
                                    "WHERE i.email = :email"),
                @NamedQuery(name = "Invite.getByEmailCount",
                            query = "SELECT COUNT(i) " +
                                    "FROM Invite i " +
                                    "WHERE i.email = :email"),
                @NamedQuery(name = "Invite.getByInstance",
                            query = "SELECT i " +
                                    "FROM Invite i " +
                                    "WHERE i.domainId = :domain AND (i.workspaceId = :instance OR i.organizationId = :instance)"),
                @NamedQuery(name = "Invite.getByInstanceCount",
                            query = "SELECT COUNT(i) " +
                                    "FROM Invite i " +
                                    "WHERE i.domainId = :domain AND (i.workspaceId = :instance OR i.organizationId = :instance)")
        }
)
public class InviteImpl implements Invite {
    @Id
    @GeneratedValue
    @Column(name = "id")
    protected Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "domain_id")
    private String domainId;

    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "workspace_id")
    private String workspaceId;

    @ElementCollection
    @Column(name = "action", nullable = false, unique = true)
    @CollectionTable(name = "codenvy_invite_action",
                     indexes = @Index(columnList = "alias"),
                     joinColumns = @JoinColumn(name = "invite_id"))
    private List<String> actions;

    public InviteImpl() {
    }

    public InviteImpl(String email,
                      String domainId,
                      String instance,
                      List<String> actions) {
        this.email = email;
        this.domainId = domainId;
        if (domainId != null) {
            switch (domainId) {
                case OrganizationDomain.DOMAIN_ID:
                    this.organizationId = instance;
                    break;
                case WorkspaceDomain.DOMAIN_ID:
                    this.workspaceId = instance;
                    break;
                default:
                    throw new IllegalArgumentException("Specified unknown domain id");
            }
        }
        if (actions != null) {
            this.actions = new ArrayList<>(actions);
        }
    }

    public InviteImpl(Invite invite) {
        this(invite.getEmail(),
             invite.getDomainId(),
             invite.getInstanceId(),
             invite.getActions());
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getInstanceId() {
        switch (domainId) {
            case OrganizationDomain.DOMAIN_ID:
                return organizationId;
            case WorkspaceDomain.DOMAIN_ID:
                return workspaceId;
            default:
                return null;
        }
    }

    @Override
    public String getDomainId() {
        return domainId;
    }

    @Override
    public List<String> getActions() {
        if (actions == null) {
            actions = new ArrayList<>();
        }
        return actions;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof InviteImpl)) {
            return false;
        }
        final InviteImpl that = (InviteImpl)obj;
        return Objects.equals(id, that.id)
               && Objects.equals(email, that.email)
               && Objects.equals(domainId, that.domainId)
               && Objects.equals(organizationId, that.organizationId)
               && Objects.equals(workspaceId, that.workspaceId)
               && getActions().equals(that.getActions());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(id);
        hash = 31 * hash + Objects.hashCode(email);
        hash = 31 * hash + Objects.hashCode(domainId);
        hash = 31 * hash + Objects.hashCode(workspaceId);
        hash = 31 * hash + Objects.hashCode(organizationId);
        hash = 31 * hash + getActions().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "InviteImpl{" +
               "id='" + id + '\'' +
               ", email='" + email + '\'' +
               ", domain='" + domainId + '\'' +
               ", organizationId='" + organizationId + '\'' +
               ", workspaceId='" + workspaceId + '\'' +
               ", actions=" + getActions() +
               '}';
    }
}
