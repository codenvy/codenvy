/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.ValueFromMapMetric.ValueType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectCreatedTypeJavaSpringNumberMetric extends AbstractProjectsCreatedMetric {


    ProjectCreatedTypeJavaSpringNumberMetric() {
        super(MetricType.PROJECT_TYPE_JAVA_SPRING_NUMBER, MetricFactory.createMetric(MetricType.PROJECTS_CREATED_LIST), "Spring",
              ValueType.NUMBER);
    }
}
