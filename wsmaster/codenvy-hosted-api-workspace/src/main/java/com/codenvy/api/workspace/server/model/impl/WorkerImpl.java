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

import com.codenvy.api.permission.server.model.impl.AbstractPermissions;
import com.codenvy.api.workspace.server.WorkspaceDomain;
import com.codenvy.api.workspace.server.model.Worker;

import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;

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
 * Data object for {@link Worker}
 *
 * @author Sergii Leschenko
 */
@Entity(name = "Worker")
@NamedQueries(
        {
                @NamedQuery(name = "Worker.getByWorkspaceId",
                            query = "SELECT worker " +
                                    "FROM Worker worker " +
                                    "WHERE worker.workspaceId = :workspaceId "),
                @NamedQuery(name = "Worker.getCountByWorkspaceId",
                            query = "SELECT COUNT(worker) " +
                                    "FROM Worker worker " +
                                    "WHERE worker.workspaceId = :workspaceId "),
                @NamedQuery(name = "Worker.getByUserId",
                            query = "SELECT worker " +
                                    "FROM Worker worker " +
                                    "WHERE worker.userId = :userId "),
                @NamedQuery(name = "Worker.getByUserAndWorkspaceId",
                            query = "SELECT worker " +
                                    "FROM Worker worker " +
                                    "WHERE worker.userId = :userId " +
                                    "AND worker.workspaceId = :workspaceId ")
        }
)
@Table(name = "worker")
public class WorkerImpl extends AbstractPermissions implements Worker {

    @Column(name = "workspaceid")
    private String workspaceId;

    @ManyToOne
    @JoinColumn(name = "workspaceid", insertable = false, updatable = false)
    private WorkspaceImpl workspace;

    public WorkerImpl() {
    }

    public WorkerImpl(String workspaceId, String userId, List<String> actions) {
        super(userId, actions);
        this.workspaceId = workspaceId;
    }

    public WorkerImpl(Worker worker) {
        this(worker.getWorkspaceId(), worker.getUserId(), worker.getActions());
    }

    @Override
    public String getInstanceId() {
        return workspaceId;
    }

    @Override
    public String getDomainId() {
        return WorkspaceDomain.DOMAIN_ID;
    }

    @Override
    public String getWorkspaceId() {
        return workspaceId;
    }

    @Override
    public String toString() {
        return "WorkerImpl{" +
               "userId='" + getUserId() + '\'' +
               ", workspaceId='" + workspaceId + '\'' +
               ", actions=" + actions +
               '}';
    }
}
