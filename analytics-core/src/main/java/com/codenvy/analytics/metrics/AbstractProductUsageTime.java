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

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.text.ParseException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractProductUsageTime extends ReadBasedMetric {

    final private long    min;
    final private long    max;
    final private boolean includeMin;
    final private boolean includeMax;

    protected AbstractProductUsageTime(String metricName,
                                       long min,
                                       long max,
                                       boolean includeMin,
                                       boolean includeMax) {
        super(metricName);
        this.min = min;
        this.max = max;
        this.includeMin = includeMin;
        this.includeMax = includeMax;
    }

    protected AbstractProductUsageTime(MetricType metricType,
                                       long min,
                                       long max,
                                       boolean includeMin,
                                       boolean includeMax) {
        this(metricType.name(), min, max, includeMin, includeMax);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getStorageTable() {
        return "product_usage_sessions";
    }

    @Override
    public boolean isAggregationSupport() {
        return true;
    }

    @Override
    public DBObject getAggregator(Map<String, String> clauses) {
        DBObject group = new BasicDBObject();

        group.put("_id", null);
        group.put("value", new BasicDBObject("$sum", "$value"));

        return new BasicDBObject("$group", group);
    }

    @Override
    public DBObject getMatcher(Map<String, String> clauses) throws ParseException {
        DBObject dbObject = super.getMatcher(clauses);
        DBObject match = (DBObject)dbObject.get("$match");

        DBObject range = new BasicDBObject();
        range.put(includeMin ? "$gte" : "$gt", min);
        range.put(includeMax ? "$lte" : "$lt", max);
        match.put("value", range);

        return dbObject;
    }
}
