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
package com.codenvy.plugin.webhooks.vsts.shared;

import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.dto.shared.JsonFieldName;

@DTO
public interface Fields {

    public String SYSTEM_AREA_PATH_FIELD                 = "System.AreaPath";
    public String SYSTEM_TEAM_PROJECT_FIELD              = "System.TeamProject";
    public String SYSTEM_ITERATION_PATH_FIELD            = "System.IterationPath";
    public String SYSTEM_WORK_ITEM_TYPE_FIELD            = "System.WorkItemType";
    public String SYSTEM_STATE_FIELD                     = "System.State";
    public String SYSTEM_REASON_FIELD                    = "System.Reason";
    public String SYSTEM_CREATED_DATE_FIELD              = "System.CreatedDate";
    public String SYSTEM_CREATED_BY_FIELD                = "System.CreatedBy";
    public String SYSTEM_CHANGED_DATE_FIELD              = "System.ChangedDate";
    public String SYSTEM_CHANGED_BY_FIELD                = "System.ChangedBy";
    public String SYSTEM_TITLE_FIELD                     = "System.Title";
    public String MICROSOFT_VSTS_STATE_CHANGE_DATE_FIELD = "Microsoft.VSTS.Common.StateChangeDate";
    public String MICROSOFT_VSTS_PRIORITY_FIELD          = "Microsoft.VSTS.Common.Priority";
    public String MICROSOFT_VSTS_SEVERITY_FIELD          = "Microsoft.VSTS.Common.Severity";
    public String MICROSOFT_VSTS_VALUE_AREA_FIELD        = "Microsoft.VSTS.Common.ValueArea";

    /**
     * Get resource System.AreaPath field.
     *
     * @return {@link String} areaPath
     */
    @JsonFieldName(SYSTEM_AREA_PATH_FIELD)
    String getAreaPath();

    void setAreaPath(final String areaPath);

    Fields withAreaPath(final String areaPath);

    /**
     * Get resource System.TeamProject field.
     *
     * @return {@link String} teamProject
     */
    @JsonFieldName(SYSTEM_TEAM_PROJECT_FIELD)
    String getTeamProject();

    void setTeamProject(final String teamProject);

    Fields withTeamProject(final String teamProject);

    /**
     * Get resource System.IterationPath field.
     *
     * @return {@link String} iterationPath
     */
    @JsonFieldName(SYSTEM_ITERATION_PATH_FIELD)
    String getIterationPath();

    void setIterationPath(final String iterationPath);

    Fields withIterationPath(final String iterationPath);

    /**
     * Get resource System.WorkItemType field.
     *
     * @return {@link String} workItemType
     */
    @JsonFieldName(SYSTEM_WORK_ITEM_TYPE_FIELD)
    String getWorkItemType();

    void setWorkItemType(final String workItemType);

    Fields withWorkItemType(final String workItemType);

    /**
     * Get resource System.State field.
     *
     * @return {@link String} state
     */
    @JsonFieldName(SYSTEM_STATE_FIELD)
    String getState();

    void setState(final String state);

    Fields withState(final String state);

    /**
     * Get resource System.Reason field.
     *
     * @return {@link String} reason
     */
    @JsonFieldName(SYSTEM_REASON_FIELD)
    String getReason();

    void setReason(final String reason);

    Fields withReason(final String reason);

    /**
     * Get resource System.CreatedDate field.
     *
     * @return {@link String} createdDate
     */
    @JsonFieldName(SYSTEM_CREATED_DATE_FIELD)
    String getCreatedDate();

    void setCreatedDate(final String createdDate);

    Fields withCreatedDate(final String createdDate);

    /**
     * Get resource System.CreatedBy field.
     *
     * @return {@link String} createdBy
     */
    @JsonFieldName(SYSTEM_CREATED_BY_FIELD)
    String getCreatedBy();

    void setCreatedBy(final String createdBy);

    Fields withCreatedBy(final String createdBy);

    /**
     * Get resource System.ChangedDate field.
     *
     * @return {@link String} changedDate
     */
    @JsonFieldName(SYSTEM_CHANGED_DATE_FIELD)
    String getChangedDate();

    void setChangedDate(final String changedDate);

    Fields withChangedDate(final String changedDate);

    /**
     * Get resource System.ChangedBy field.
     *
     * @return {@link String} changedBy
     */
    @JsonFieldName(SYSTEM_CHANGED_BY_FIELD)
    String getChangedBy();

    void setChangedBy(final String changedBy);

    Fields withChangedBy(final String changedBy);

    /**
     * Get resource System.Title field.
     *
     * @return {@link String} title
     */
    @JsonFieldName(SYSTEM_TITLE_FIELD)
    String getTitle();

    void setTitle(final String title);

    Fields withTitle(final String title);

    /**
     * Get resource Microsoft.VSTS.Common.StateChangeDate field.
     *
     * @return {@link String} stateChangeDate
     */
    @JsonFieldName(MICROSOFT_VSTS_STATE_CHANGE_DATE_FIELD)
    String getStateChangeDate();

    void setStateChangeDate(final String stateChangeDate);

    Fields withStateChangeDate(final String stateChangeDate);

    /**
     * Get resource Microsoft.VSTS.Common.Priority field.
     *
     * @return {@link Integer} priority
     */
    @JsonFieldName(MICROSOFT_VSTS_PRIORITY_FIELD)
    int getPriority();

    void setPriority(final int priority);

    Fields withPriority(final int priority);

    /**
     * Get resource Microsoft.VSTS.Common.Severity field.
     *
     * @return {@link String} severity
     */
    @JsonFieldName(MICROSOFT_VSTS_SEVERITY_FIELD)
    String getSeverity();

    void setSeverity(final String severity);

    Fields withSeverity(final String severity);

    /**
     * Get resource Microsoft.VSTS.Common.ValueArea field.
     *
     * @return {@link String} valueArea
     */
    @JsonFieldName(MICROSOFT_VSTS_VALUE_AREA_FIELD)
    String getValueArea();

    void setValueArea(final String valueArea);

    Fields withValueArea(final String valueArea);
}
