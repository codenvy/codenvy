/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */

package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.FixedListLongValueData;

/**
 * Visits >5 And Lifetime >=300
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersSegmentAnalysisCondition3 extends AbstractUsersSegmentAnalysis {

    public UsersSegmentAnalysisCondition3() {
        super(MetricType.USERS_SEGMENT_ANALYSIS_CONDITION_3);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isAccepted(FixedListLongValueData valueData) {
        return getSessionsNumber(valueData) > 5 && getUsageTime(valueData) >= 300;
    }

    @Override
    public String getDescription() {
        return "The number of users who have the number of sessions more than 5 and usage time more or equal to 120 minutes";
    }
}
