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

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class EventFactoryTest {
    public static final String STRING_LENGTH_1001 = StringUtils.repeat("1", 1001);

    @Test
    public void shouldCreateImSubscriptionAddedEventWithTime() {
        Event event = EventFactory.createImSubscriptionAddedEventWithTime("plan", "user");
        assertEquals(event.getType(), Event.Type.IM_SUBSCRIPTION_ADDED);
        assertEquals(event.getParameters().size(), 3);
        assertEquals(event.getParameters().get(Event.PLAN_PARAM), "plan");
        assertEquals(event.getParameters().get(Event.USER_PARAM), "user");
        assertTrue(event.getParameters().get(Event.TIME_PARAM).matches("\\d*"), "Actual time: " + event.getParameters().get(Event.TIME_PARAM));
    }

    @Test
    public void shouldCreateImArtifactDownloadedEventWithTime() {
        Event event = EventFactory.createImArtifactDownloadedEventWithTime("codenvy", "1.0.0", "user");
        assertEquals(event.getType(), Event.Type.IM_ARTIFACT_DOWNLOADED);
        assertEquals(event.getParameters().size(), 4);
        assertEquals(event.getParameters().get(Event.ARTIFACT_PARAM), "codenvy");
        assertEquals(event.getParameters().get(Event.VERSION_PARAM), "1.0.0");
        assertEquals(event.getParameters().get(Event.USER_PARAM), "user");
        assertTrue(event.getParameters().get(Event.TIME_PARAM).matches("\\d*"), "Actual time: " + event.getParameters().get(Event.TIME_PARAM));
    }

    @Test
    public void shouldCreateImArtifactInstallStartedEventWithTime() {
        Event event = EventFactory.createImArtifactInstallStartedWithTime("codenvy", "1.0.0");
        assertEquals(event.getType(), Event.Type.IM_ARTIFACT_INSTALL_STARTED);
        assertEquals(event.getParameters().size(), 3);
        assertEquals(event.getParameters().get(Event.ARTIFACT_PARAM), "codenvy");
        assertEquals(event.getParameters().get(Event.VERSION_PARAM), "1.0.0");
        assertTrue(event.getParameters().get(Event.TIME_PARAM).matches("\\d*"), "Actual time: " + event.getParameters().get(Event.TIME_PARAM));
    }

    @Test
    public void shouldCreateImArtifactInstallFinishedSuccessfullyEventWithTime() {
        Event event = EventFactory.createImArtifactInstallSuccessWithTime("codenvy", "1.0.0");
        assertEquals(event.getType(), Event.Type.IM_ARTIFACT_INSTALL_FINISHED_SUCCESSFULLY);
        assertEquals(event.getParameters().size(), 3);
        assertEquals(event.getParameters().get(Event.ARTIFACT_PARAM), "codenvy");
        assertEquals(event.getParameters().get(Event.VERSION_PARAM), "1.0.0");
        assertTrue(event.getParameters().get(Event.TIME_PARAM).matches("\\d*"), "Actual time: " + event.getParameters().get(Event.TIME_PARAM));
    }

    @Test
    public void shouldCreateImArtifactInstallFinishedUnsuccessfullyEventWithTime() {
        Event event = EventFactory.createImArtifactInstallUnsuccessWithTime("codenvy", "1.0.0", STRING_LENGTH_1001);
        assertEquals(event.getType(), Event.Type.IM_ARTIFACT_INSTALL_FINISHED_UNSUCCESSFULLY);
        assertEquals(event.getParameters().size(), 4);
        assertEquals(event.getParameters().get(Event.ARTIFACT_PARAM), "codenvy");
        assertEquals(event.getParameters().get(Event.VERSION_PARAM), "1.0.0");
        assertEquals(event.getParameters().get(Event.ERROR_MESSAGE_PARAM), STRING_LENGTH_1001.substring(0, 999));
        assertTrue(event.getParameters().get(Event.TIME_PARAM).matches("\\d*"), "Actual time: " + event.getParameters().get(Event.TIME_PARAM));
    }
}
