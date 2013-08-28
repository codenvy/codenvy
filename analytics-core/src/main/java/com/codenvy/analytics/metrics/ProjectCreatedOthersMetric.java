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

import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProjectCreatedOthersMetric extends CalculatedMetric {

    public ProjectCreatedOthersMetric() {
        super(MetricType.PROJECT_TYPE_OTHERS, MetricType.PROJECT_CREATED_TYPES);
    }

    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        MetricParameter.PARAM.put(context, "null");
        ValueData value = super.getValue(context);

        MetricParameter.PARAM.put(context, "default");
        value = value.union(super.getValue(context));

        MetricParameter.PARAM.put(context, "eXo");
        return value.union(super.getValue(context));
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getDescription() {
        return "The number of undefined projects types";
    }
}
