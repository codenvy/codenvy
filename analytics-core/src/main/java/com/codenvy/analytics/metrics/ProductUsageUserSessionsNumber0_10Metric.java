/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.ProductUsageTimeFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProductUsageUserSessionsNumber0_10Metric extends CalculateBasedMetric {

    private final Metric basedMetric;

    public ProductUsageUserSessionsNumber0_10Metric() {
        super(MetricType.PRODUCT_USAGE_USER_SESSIONS_NUMBER_0_10);
        this.basedMetric = MetricFactory.createMetric(MetricType.PRODUCT_USAGE_TIME_LIST);
    }

    /** {@inheritDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }

    /** {@inheritDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        ListListStringValueData value = (ListListStringValueData)basedMetric.getValue(context);

        ProductUsageTimeFilter filter = new ProductUsageTimeFilter(value);
        return new LongValueData(filter.getNumberOfSessions(0, true, 10 * 60, false));
    }
}
