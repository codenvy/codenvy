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
package com.codenvy.api.workspace.server.model.impl;

import com.codenvy.api.workspace.server.jpa.WorkerPrimaryKey;
import com.codenvy.api.workspace.server.model.Worker;

import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Sergii Leschenko
 */
@Entity(name = "Worker")
@NamedQueries(
        {
                @NamedQuery(name = "Worker.getByWorkspaceId",
                            query = "SELECT worker " +
                                    "FROM Worker worker " +
                                    "WHERE worker.workspaceId = :workspaceId "),
                @NamedQuery(name = "Worker.getByUserId",
                            query = "SELECT worker " +
                                    "FROM Worker worker " +
                                    "WHERE worker.userId = :userId ")
        }
)
@IdClass(WorkerPrimaryKey.class)
public class WorkerImpl implements Worker {
    @Id
    private String userId;

    @OneToOne
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private UserImpl user;

    @Id
    private String workspaceId;

    @OneToOne
    @JoinColumn(name = "workspaceId", insertable = false, updatable = false)
    private WorkspaceImpl workspace;

    @ElementCollection
    @CollectionTable(indexes = @Index(columnList = "actions"))
    private List<String> actions;

    public WorkerImpl() {
    }

    public WorkerImpl(String workspaceId, String userId, List<String> actions) {
        this.userId = userId;
        this.workspaceId = workspaceId;
        this.actions = new ArrayList<>();
        if (actions != null) {
            this.actions.addAll(actions);
        }
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getWorkspaceId() {
        return workspaceId;
    }

    @Override
    public List<String> getActions() {
        return actions;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WorkerImpl)) return false;
        final WorkerImpl other = (WorkerImpl)obj;
        return Objects.equals(userId, other.userId) &&
               Objects.equals(workspaceId, other.workspaceId) &&
               actions.equals(other.actions);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(userId);
        hash = 31 * hash + Objects.hashCode(workspaceId);
        hash = 31 * hash + actions.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "WorkerImpl{" +
               "userId='" + userId + '\'' +
               ", workspaceId='" + workspaceId + '\'' +
               ", actions=" + actions +
               '}';
    }
}
