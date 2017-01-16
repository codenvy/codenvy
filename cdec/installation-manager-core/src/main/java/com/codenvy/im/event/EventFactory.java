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

import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Dmytro Nochevnov
 */
public class EventFactory {


    /**
     * Creates Event object of certain type with certain parameters + parameter(TIME = [current_system_time])
     */
    public static Event createWithTime(final Event.Type type, @NotNull Map<String, String> parameters) {
        parameters = new LinkedHashMap<>(parameters);
        parameters.put(Event.TIME_PARAM, getTime());

        return new Event(type, parameters);
    }

    public static Event createImSubscriptionAddedEventWithTime(final String planId, final String userId) {
        Map<String, String> eventParameters = new LinkedHashMap<>();
        eventParameters.put(Event.PLAN_PARAM, planId);
        eventParameters.put(Event.USER_PARAM, userId);

        return createWithTime(Event.Type.IM_SUBSCRIPTION_ADDED, eventParameters);
    }

    public static Event createImArtifactDownloadedEventWithTime(final String artifact, final String version, final String userId) {
        Map<String, String> eventParameters = new LinkedHashMap<>();
        eventParameters.put(Event.ARTIFACT_PARAM, artifact);
        eventParameters.put(Event.VERSION_PARAM, version);
        eventParameters.put(Event.USER_PARAM, userId);

        return createWithTime(Event.Type.IM_ARTIFACT_DOWNLOADED, eventParameters);
    }

    public static Event createImArtifactInstallStartedWithTime(final String artifact, final String version) {
        Map<String, String> eventParameters = new LinkedHashMap<>();
        eventParameters.put(Event.ARTIFACT_PARAM, artifact);
        eventParameters.put(Event.VERSION_PARAM, version);

        return createWithTime(Event.Type.IM_ARTIFACT_INSTALL_STARTED, eventParameters);
    }

    public static Event createImArtifactInstallSuccessWithTime(final String artifact, final String version) {
        Map<String, String> eventParameters = new LinkedHashMap<>();
        eventParameters.put(Event.ARTIFACT_PARAM, artifact);
        eventParameters.put(Event.VERSION_PARAM, version);

        return createWithTime(Event.Type.IM_ARTIFACT_INSTALL_FINISHED_SUCCESSFULLY, eventParameters);
    }

    public static Event createImArtifactInstallUnsuccessWithTime(final String artifact,
                                                                 final String version,
                                                                 String errorMessage) {
        Map<String, String> eventParameters = new LinkedHashMap<>();
        eventParameters.put(Event.ARTIFACT_PARAM, artifact);
        eventParameters.put(Event.VERSION_PARAM, version);

        errorMessage = StringUtils.substring(errorMessage, 0, Event.MAX_LONG_PARAM_VALUE_LENGTH - 1);
        eventParameters.put(Event.ERROR_MESSAGE_PARAM, errorMessage);

        return createWithTime(Event.Type.IM_ARTIFACT_INSTALL_FINISHED_UNSUCCESSFULLY, eventParameters);
    }

    private static String getTime() {
        return String.valueOf(System.currentTimeMillis());
    }

}
