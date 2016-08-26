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

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.List;
import java.util.Objects;

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
                @NamedQuery(name = "Worker.getByUserId",
                            query = "SELECT worker " +
                                    "FROM Worker worker " +
                                    "WHERE worker.userId = :userId "),
                @NamedQuery(name = "Worker.getByUserAndWorkspaceId",
                            query = "SELECT worker " +
                                    "FROM Worker worker " +
                                    "WHERE worker.workspaceId = :workspaceId " +
                                    "AND worker.userId = :userId")
        }
)
@Table(indexes = @Index(columnList = "userId, workspaceId", unique = true))
public class WorkerImpl extends AbstractPermissions implements Worker {

    private String workspaceId;

    @OneToOne
    @JoinColumn(name = "workspaceId", insertable = false, updatable = false)
    private WorkspaceImpl workspace;

    public WorkerImpl() {
    }

    public WorkerImpl(String workspaceId, String userId, List<String> actions) {
        super(userId, actions);
        this.workspaceId = workspaceId;
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
               "userId='" + userId + '\'' +
               ", workspaceId='" + workspaceId + '\'' +
               ", actions=" + actions +
               '}';
    }
}
