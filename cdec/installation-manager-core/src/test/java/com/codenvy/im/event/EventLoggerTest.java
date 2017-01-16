/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.im.event;

import com.google.common.collect.ImmutableMap;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertTrue;

/** @author Dmytro Nochevnov */
public class EventLoggerTest {

    public static final Event.Type TEST_EVENT = Event.Type.IM_ARTIFACT_DOWNLOADED;

    @Mock
    private Logger mockLogger;

    private EventLogger spyEventLogger;

    private Logger ORIGINAL_LOG = EventLogger.LOG;

    @BeforeClass
    public void classSetup() {
        ORIGINAL_LOG = EventLogger.LOG;
    }

    @BeforeMethod
    public void testSetup() throws Exception {
        MockitoAnnotations.initMocks(this);

        spyEventLogger = spy(new EventLogger());
        EventLogger.LOG = mockLogger;
        spyEventLogger.init();
    }

    @Test
    public void shouldLogEvent() throws Exception {
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        Map<String, String> testEventParameters = ImmutableMap.of(
            "PARAM1", "param1-value",
            "PARAM2", "param2-value"
        );

        Event event = EventFactory.createWithTime(TEST_EVENT, testEventParameters);
        spyEventLogger.log(event);

        Thread.sleep(2 * EventLogger.LogThread.SLEEP_ON_EMPTY_QUEUE_TIMEOUT_MILLIS);

        verify(mockLogger, atLeastOnce()).info(argument.capture());

        List<String> values = argument.getAllValues();

        boolean findOne = false;
        for (String value: values) {
            if (value.matches("EVENT#" + TEST_EVENT + "# PARAM1#param1-value# PARAM2#param2-value# TIME#\\d*#")) {
                findOne = true;
            }
        }

        assertTrue(findOne, "Actual invocations: " + values.toString());
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Empty event is not accepted")
    public void shouldNotLogNullEvent() throws UnsupportedEncodingException {
        ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);

        spyEventLogger.log(null);

        verify(spyEventLogger, never()).offerEvent(event.capture());
    }

    @AfterMethod
    public void testTearDown() {
        spyEventLogger.destroy();
    }

    @AfterClass
    public void classTearDown() {
        EventLogger.LOG = ORIGINAL_LOG;
    }
}
