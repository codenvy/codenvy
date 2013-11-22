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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.NonAggregatedResultMetric;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.testng.Assert.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestSetOfActiveEntities extends BaseTest {

    private Map<String, String> params;
    private TestMetric          metric;

    @BeforeClass
    public void init() throws IOException {
        params = Utils.newContext();
        metric = new TestMetric();

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1@gmail.com")
                        .withDate("2013-01-01")
                        .withTime("10:00:00")
                        .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1@gmail.com")
                        .withDate("2013-01-01")
                        .withTime("10:00:01")
                        .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user2@gmail.com")
                        .withDate("2013-01-01")
                        .withTime("10:00:02")
                        .build());
        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130101");
        Parameters.TO_DATE.put(params, "20130101");
        Parameters.USER.put(params, Parameters.USER_TYPES.REGISTERED.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.PERSISTENT.name());
        Parameters.EVENT.put(params, EventType.TENANT_CREATED.toString());
        Parameters.PARAM.put(params, "ws");
        Parameters.STORAGE_TABLE.put(params, "testsetofactiveentities");
        Parameters.LOG.put(params, log.getAbsolutePath());

        PigServer.execute(ScriptType.ACTIVE_ENTITIES_LIST, params);

        events = new ArrayList<>();
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user2@gmail.com")
                        .withDate("2013-01-02")
                        .withTime("10:00:00")
                        .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws3", "user1@gmail.com")
                        .withDate("2013-01-02")
                        .withTime("10:00:01")
                        .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws4", "user4@gmail.com")
                        .withDate("2013-01-02")
                        .withTime("10:00:02")
                        .build());
        log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130102");
        Parameters.TO_DATE.put(params, "20130102");
        Parameters.LOG.put(params, log.getAbsolutePath());

        PigServer.execute(ScriptType.ACTIVE_ENTITIES_LIST, params);
    }

    @Test
    public void testExecute() throws Exception {
        Iterator<Tuple> iterator = PigServer.executeAndReturn(ScriptType.ACTIVE_ENTITIES_LIST, params);

        assertTrue(iterator.hasNext());
        Tuple tuple = iterator.next();
        assertEquals(tuple.get(0), timeFormat.parse("20130102 10:00:00").getTime());
        assertEquals(tuple.get(1).toString(), "(value,ws2)");

        assertTrue(iterator.hasNext());
        tuple = iterator.next();
        assertEquals(tuple.get(0), timeFormat.parse("20130102 10:00:01").getTime());
        assertEquals(tuple.get(1).toString(), "(value,ws3)");

        assertTrue(iterator.hasNext());
        tuple = iterator.next();
        assertEquals(tuple.get(0), timeFormat.parse("20130102 10:00:02").getTime());
        assertEquals(tuple.get(1).toString(), "(value,ws4)");

        assertFalse(iterator.hasNext());
    }

    @Test
    public void testSingleDateFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130101");

        assertEquals(metric.getValue(context), new SetValueData(Arrays.<ValueData>asList(new StringValueData("ws1"),
                                                                                         new StringValueData("ws2"))));
    }

    @Test
    public void testDatePeriodFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130102");

        assertEquals(metric.getValue(context), new SetValueData(Arrays.<ValueData>asList(new StringValueData("ws1"),
                                                                                         new StringValueData("ws2"),
                                                                                         new StringValueData("ws3"),
                                                                                         new StringValueData("ws4"))));
    }

    @Test
    public void testSingleUserFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130102");
        MetricFilter.USER.put(context, "user1@gmail.com");

        assertEquals(metric.getValue(context), new SetValueData(Arrays.<ValueData>asList(new StringValueData("ws1"),
                                                                                         new StringValueData("ws3"))));
    }

    @Test
    public void testDoubleUserFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130102");
        MetricFilter.USER.put(context, "user1@gmail.com,user2@gmail.com");

        assertEquals(metric.getValue(context), new SetValueData(Arrays.<ValueData>asList(new StringValueData("ws1"),
                                                                                         new StringValueData("ws2"),
                                                                                         new StringValueData("ws3"))));
    }

    @Test
    public void testSeveralFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130102");
        MetricFilter.USER.put(context, "user1@gmail.com,user2@gmail.com");
        MetricFilter.WS.put(context, "ws2");

        assertEquals(metric.getValue(context), new SetValueData(Arrays.<ValueData>asList(new StringValueData("ws2"))));
    }

    public class TestMetric extends NonAggregatedResultMetric {

        private TestMetric() {
            super("testsetofactiveentities");
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return SetValueData.class;
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
