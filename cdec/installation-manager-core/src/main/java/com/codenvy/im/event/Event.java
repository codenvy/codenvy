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

import com.codenvy.im.utils.Commons;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.server.JsonSerializable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * @author Dmytro Nochevnov
 */
public class Event implements JsonSerializable {
    public static final String       TIME_PARAM           = "TIME";
    public static final String       USER_PARAM           = "USER";
    public static final String       PLAN_PARAM           = "PLAN";
    public static final String       ARTIFACT_PARAM       = "ARTIFACT";
    public static final String       VERSION_PARAM        = "VERSION";
    public static final String       USER_IP_PARAM        = "USER-IP";
    public static final String       ERROR_MESSAGE_PARAM  = "ERROR-MESSAGE";
    public static final List<String> PARAMETERS_TO_ENCODE = Collections.singletonList(ERROR_MESSAGE_PARAM);

    public static final int MAX_EXTENDED_PARAMS_NUMBER  = 10;
    public static final int RESERVED_PARAMS_NUMBER      = 5;     // reserved for TIME_PARAM, USER_PARAM and USER_IP_PARAM
    public static final int MAX_PARAM_NAME_LENGTH       = 20;
    public static final int MAX_PARAM_VALUE_LENGTH      = 100;
    public static final int MAX_LONG_PARAM_VALUE_LENGTH = 1000;

    public static final Collection NAMES_OF_LONG_PARAMETERS = Collections.singletonList(ERROR_MESSAGE_PARAM);

    private Type type;

    private Map<String, String> parameters;

    @Override
    public String toJson() {
        try {
            return Commons.toJson(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public JsonElement toJsonElement() {
        JsonParser jsonParser = new JsonParser();
        return jsonParser.parse(toJson());
    }

    public enum Type {
        IM_ARTIFACT_DOWNLOADED,
        IM_ARTIFACT_INSTALL_STARTED,
        IM_ARTIFACT_INSTALL_FINISHED_SUCCESSFULLY,
        IM_ARTIFACT_INSTALL_FINISHED_UNSUCCESSFULLY,
        IM_SUBSCRIPTION_ADDED,
        CDEC_FIRST_LOGIN;

        /**
         * transform from "IM_ARTIFACT_DOWNLOADED" to "im-artifact-downloaded"
         */
        public String toString() {
            return this.name().toLowerCase().replaceAll("_", "-");
        }
    }

    /** Default constructor is needed to use Event object as an argument of REST services */
    public Event() {
    }

    public Event(Type type, Map<String, String> parameters) {
        validate(parameters);

        setType(type);
        setParameters(parameters);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }

        this.type = type;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * set parameters as extended
     */
    public void setParameters(Map<String, String> parameters) {
        validate(parameters);

        this.parameters = parameters;
    }

    /**
     * add new parameter or replace existed one
     */
    public void putParameter(String param, String value) {
        if (param == null) {
            throw new IllegalArgumentException("Parameter name cannot be null");
        }

        if (value == null) {
            throw new IllegalArgumentException("Parameter value cannot be null");
        }

        Map<String, String> newParameters;
        if (this.parameters != null) {
            newParameters = new LinkedHashMap<>(this.getParameters());
        } else {
            newParameters = new LinkedHashMap<>();
        }

        newParameters.put(param, value);
        validate(newParameters);

        this.parameters = newParameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder record = new StringBuilder(format("EVENT#%s#", type));

        if (parameters == null) {
            return record.toString();
        }

        parameters.forEach((key, value) -> {
            if (isParameterToEncode(key)) {
                try {
                    value = URLEncoder.encode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }

            record.append(format(" %s#%s#", key, value));
        });

        return record.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Event event = (Event)o;

        if (type != null ? !type.equals(event.type) : event.type != null) {
            return false;
        }

        if (parameters != null ? !parameters.equals(event.parameters) : event.parameters != null) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        return result;
    }


    private void validate(@Nullable Map<String, String> parameters) throws IllegalArgumentException {
        if (parameters == null) {
            return;
        }

        validateNumberOfParameters(parameters);

        parameters.forEach(this::validate);
    }

    public void validateNumberOfParameters(Map<String, String> parameters) throws IllegalArgumentException {
        if (parameters.size() > (MAX_EXTENDED_PARAMS_NUMBER + RESERVED_PARAMS_NUMBER)) {
            throw new IllegalArgumentException("The number of parameters exceeded the limit in " + (MAX_EXTENDED_PARAMS_NUMBER + RESERVED_PARAMS_NUMBER));
        }
    }

    private void validate(String param, String value) {
        if (param.length() > MAX_PARAM_NAME_LENGTH) {
            throw new IllegalArgumentException(format("The length of parameter name '%s' exceeded the length in %s characters",
                                                      param,
                                                      MAX_PARAM_NAME_LENGTH));
        }

        if (!NAMES_OF_LONG_PARAMETERS.contains(param)
            && value.length() > MAX_PARAM_VALUE_LENGTH) {
            throw new IllegalArgumentException(format("The length of parameter %s value '%s' exceeded the length in %s characters",
                                                      param,
                                                      value,
                                                      MAX_PARAM_VALUE_LENGTH));
        }

        if (value.length() > MAX_LONG_PARAM_VALUE_LENGTH) {
            throw new IllegalArgumentException(format("The length of parameter %s value '%s' exceeded the length in %s characters",
                                                      param,
                                                      value,
                                                      MAX_LONG_PARAM_VALUE_LENGTH));
        }
    }

    public static void validateNumberOfParametersTreatingAsExtended(Map<String, String> parameters) throws IllegalArgumentException {
        if (parameters.size() > MAX_EXTENDED_PARAMS_NUMBER) {
            throw new IllegalArgumentException("The number of parameters exceeded the limit of extended parameters in " + MAX_EXTENDED_PARAMS_NUMBER);
        }
    }

    /**
     * The parameter which contains special characters like "#" and so should be encoded to be properly processed by Analytics.
     *
     * @return true if key belongs to list of parameters to encode
     */
    private static boolean isParameterToEncode(String key) {
        return PARAMETERS_TO_ENCODE.contains(key.toUpperCase());
    }
}
