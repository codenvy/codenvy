/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTopSessions30DayMetric extends AbstractTopSessionsMetric {

    public FactoryUrlTopSessions30DayMetric() {
        super(MetricType.FACTORY_URL_TOP_SESSIONS_BY_30DAY, 30);
    }

    @Override
    public String getDescription() {
        return "Top 100 sessions in temporary workspaces by time usage during last 30 days";
    }
}
