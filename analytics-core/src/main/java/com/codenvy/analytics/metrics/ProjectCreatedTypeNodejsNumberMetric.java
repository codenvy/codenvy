/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.ValueFromMapMetric.ValueType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectCreatedTypeNodejsNumberMetric extends AbstractProjectsCreatedMetric {

    ProjectCreatedTypeNodejsNumberMetric() {
        super(MetricType.PROJECT_TYPE_NODEJS_NUMBER, MetricFactory.createMetric(MetricType.PROJECTS_CREATED_LIST), "nodejs",
              ValueType.NUMBER);
    }
}
