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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.fail;

/**
 * @author Anatoly Bazko
 * @author Dmytro Nochevnov
 */
public class EventTest {

    public static final Event.Type TEST_EVENT                = Event.Type.IM_ARTIFACT_DOWNLOADED;
    public static final String     STRING_LENGTH_21          = StringUtils.repeat("1", 21);
    public static final String     STRING_LENGTH_101         = StringUtils.repeat("1", 101);
    public static final String     STRING_LENGTH_1001        = StringUtils.repeat("1", 1001);
    public static final String     ANY_PARAM                 = "PARAM";
    public static final String     ANY_VALUE                 = "any_value";
    public static final int        MAXIMUM_PARAMETERS_NUMBER = Event.MAX_EXTENDED_PARAMS_NUMBER + Event.RESERVED_PARAMS_NUMBER;

    public static Map<String, String> MAP_WITH_TOO_MANY_PARAMETERS = new HashMap<>();
    public static Map<String, String> MAP_WITH_MAXIMUM_PARAMETERS  = new HashMap<>();

    @Test(dataProvider = "dataToTestCorrectArguments")
    public void shouldCreateEventWithCorrectArguments(Event.Type type, Map<String, String> parameters, String expectedStringRepresentation, String expectedJsonRepresentation) {
        Event event = new Event(type, parameters);
        assertEquals(event.toString(), expectedStringRepresentation);
        assertEquals(event.toJson(), expectedJsonRepresentation);

        IntStream.range(0, MAXIMUM_PARAMETERS_NUMBER)
                 .forEach(i -> MAP_WITH_MAXIMUM_PARAMETERS.put(String.valueOf(i), "a"));

        IntStream.range(0, MAXIMUM_PARAMETERS_NUMBER + 1)
                 .forEach(i -> MAP_WITH_TOO_MANY_PARAMETERS.put(String.valueOf(i), "a"));
    }

    @DataProvider
    public Object[][] dataToTestCorrectArguments() {
        return new Object[][]{
            {TEST_EVENT, null,
             "EVENT#im-artifact-downloaded#",
             "{\n"
             + "  \"type\" : \"IM_ARTIFACT_DOWNLOADED\"\n"
             + "}"},

            {TEST_EVENT, new HashMap<>(),
             format("EVENT#%s#", TEST_EVENT),
             format("{\n"
                    + "  \"type\" : \"%s\",\n"
                    + "  \"parameters\" : { }\n"
                    + "}", TEST_EVENT.name())},

            {TEST_EVENT, ImmutableMap.of(ANY_PARAM, STRING_LENGTH_21),
             format("EVENT#%s# %s#%s#", TEST_EVENT, ANY_PARAM, STRING_LENGTH_21),
             format("{\n"
                    + "  \"type\" : \"%s\",\n"
                    + "  \"parameters\" : {\n"
                    + "    \"%s\" : \"%s\"\n"
                    + "  }\n"
                    + "}", TEST_EVENT.name(), ANY_PARAM, STRING_LENGTH_21)},

            {TEST_EVENT, ImmutableMap.of(Event.ERROR_MESSAGE_PARAM, STRING_LENGTH_101),
             format("EVENT#%s# %s#%s#", TEST_EVENT, Event.ERROR_MESSAGE_PARAM, STRING_LENGTH_101),
             format("{\n"
                    + "  \"type\" : \"%s\",\n"
                    + "  \"parameters\" : {\n"
                    + "    \"%s\" : \"%s\"\n"
                    + "  }\n"
                    + "}", TEST_EVENT.name(), Event.ERROR_MESSAGE_PARAM, STRING_LENGTH_101)},
        };
    }

    @Test(dataProvider = "dataToTestIncorrectArguments")
    public void shouldThrowExceptionOnIncorrectArguments(Event.Type type, Map<String, String> parameters, String expectedExceptionMessage) {
        try {
            new Event(type, parameters);
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), expectedExceptionMessage);
            return;
        }

        fail("There should be IllegalArgumentException exception with error message: " + expectedExceptionMessage);
    }

    @DataProvider
    public Object[][] dataToTestIncorrectArguments() {
        return new Object[][] {
            {null, null,
             "Type cannot be null"},

            {TEST_EVENT, MAP_WITH_TOO_MANY_PARAMETERS,
             format("The number of parameters exceeded the limit in %s", MAXIMUM_PARAMETERS_NUMBER)},

            {TEST_EVENT, ImmutableMap.of(STRING_LENGTH_101, STRING_LENGTH_21),
             format("The length of parameter name '%s' exceeded the length in %s characters", STRING_LENGTH_101, Event.MAX_PARAM_NAME_LENGTH)},

            {TEST_EVENT, ImmutableMap.of(ANY_PARAM, STRING_LENGTH_101),
             format("The length of parameter %s value '%s' exceeded the length in %s characters", ANY_PARAM, STRING_LENGTH_101, Event.MAX_PARAM_VALUE_LENGTH)},

            {TEST_EVENT, ImmutableMap.of(Event.ERROR_MESSAGE_PARAM, STRING_LENGTH_1001),
             format("The length of parameter %s value '%s' exceeded the length in %s characters", Event.ERROR_MESSAGE_PARAM, STRING_LENGTH_1001, Event.MAX_LONG_PARAM_VALUE_LENGTH)},
        };
    }

    @Test(dataProvider = "dataToTestPutCorrectParameter")
    public void shouldPutParameter(Event event, String param, String value, String expectedStringRepresentation) {
        event.putParameter(param, value);
        assertEquals(event.toString(), expectedStringRepresentation);
    }

    @DataProvider
    public Object[][] dataToTestPutCorrectParameter() {
        return new Object[][]{
            {new Event(TEST_EVENT, null),
             ANY_PARAM, STRING_LENGTH_21,
             format("EVENT#%s# %s#%s#", TEST_EVENT, ANY_PARAM, STRING_LENGTH_21)},

            {new Event(TEST_EVENT, new HashMap<>()),
             ANY_PARAM, STRING_LENGTH_21,
             format("EVENT#%s# %s#%s#", TEST_EVENT, ANY_PARAM, STRING_LENGTH_21)},

            {new Event(TEST_EVENT, ImmutableMap.of(ANY_PARAM, STRING_LENGTH_21)),
             ANY_PARAM, ANY_VALUE,
             format("EVENT#%s# %s#%s#", TEST_EVENT, ANY_PARAM, ANY_VALUE)},

            {new Event(TEST_EVENT, ImmutableMap.of(ANY_PARAM, STRING_LENGTH_21)),
             Event.TIME_PARAM, ANY_VALUE,
             format("EVENT#%s# %s#%s# %s#%s#", TEST_EVENT, ANY_PARAM, STRING_LENGTH_21, Event.TIME_PARAM, ANY_VALUE)},
        };
    }

    @Test(dataProvider = "dataToTestPutIncorrectParameter")
    public void shouldThrowExceptionOnPutIncorrectParameter(Event event, String param, String value, String expectedExceptionMessage) {
        try {
            event.putParameter(param, value);
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), expectedExceptionMessage);
            return;
        }

        fail("There should be IllegalArgumentException exception with error message: " + expectedExceptionMessage);
    }

    @DataProvider
    public Object[][] dataToTestPutIncorrectParameter() {
        return new Object[][]{
            {new Event(TEST_EVENT, null),
             null, null,
             "Parameter name cannot be null"},

            {new Event(TEST_EVENT, null),
             ANY_PARAM, null,
             "Parameter value cannot be null"},

            {new Event(TEST_EVENT, new HashMap<>()),
             STRING_LENGTH_101, STRING_LENGTH_21,
             format("The length of parameter name '%s' exceeded the length in %s characters", STRING_LENGTH_101, Event.MAX_PARAM_NAME_LENGTH)},

            {new Event(TEST_EVENT, ImmutableMap.of(ANY_PARAM, STRING_LENGTH_21)),
             ANY_PARAM, STRING_LENGTH_101,
             format("The length of parameter %s value '%s' exceeded the length in %s characters", ANY_PARAM, STRING_LENGTH_101, Event.MAX_PARAM_VALUE_LENGTH)},

            {new Event(TEST_EVENT, ImmutableMap.of(ANY_PARAM, STRING_LENGTH_21)),
             Event.ERROR_MESSAGE_PARAM, STRING_LENGTH_1001,
             format("The length of parameter %s value '%s' exceeded the length in %s characters", Event.ERROR_MESSAGE_PARAM, STRING_LENGTH_1001, Event.MAX_LONG_PARAM_VALUE_LENGTH)},

            {new Event(TEST_EVENT, MAP_WITH_MAXIMUM_PARAMETERS),
             ANY_PARAM, STRING_LENGTH_21,
             format("The number of parameters exceeded the limit in %s", MAXIMUM_PARAMETERS_NUMBER)},

        };
    }

    @Test
    public void shouldLogEventWithParametersSpecialCharactersUseCase1() throws UnsupportedEncodingException {
        Map<String, String> parameters = ImmutableMap.of("p1", ",",
                                                         "p2", "=",
                                                         "p3", "#",
                                                         "p4", " ");

        Event event = new Event(TEST_EVENT, parameters);
        assertEquals(event.toString(), "EVENT#" + TEST_EVENT + "# p1#,# p2#=# p3### p4# #");
    }

    @Test
    public void shouldLogEventWithParametersSpecialCharactersUseCase2() throws UnsupportedEncodingException {
        String keyToEncode = Event.PARAMETERS_TO_ENCODE.get(0);
        String valueToEncode = ",=# ";
        Map<String, String> parameters = ImmutableMap.of(keyToEncode, valueToEncode);

        Event event = new Event(TEST_EVENT, parameters);
        assertEquals(event.toString(),
                     format("EVENT#%s# %s#%s#", TEST_EVENT, keyToEncode, URLEncoder.encode(valueToEncode, "UTF-8")));
    }

    @Test
    public void testEqualsAndHashCode() {
        Event event1 = new Event(TEST_EVENT, ImmutableMap.of(ANY_PARAM, STRING_LENGTH_21));
        Event event2 = new Event(Event.Type.IM_ARTIFACT_INSTALL_STARTED, ImmutableMap.of(Event.PLAN_PARAM, STRING_LENGTH_21));

        assertEquals(event1, event1);
        assertEquals(event1.hashCode(), event1.hashCode());

        assertNotEquals(event1, event2);
        assertNotEquals(event1.hashCode(), event2.hashCode());
    }

    @Test
    public void testToJsonElement() throws Exception {
        Event event = new Event(TEST_EVENT, ImmutableMap.of(ANY_PARAM, ANY_VALUE));
        JsonElement jsonElement = event.toJsonElement();

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertEquals(jsonObject.get("type").getAsString(), TEST_EVENT.name());

        JsonObject parameters = jsonObject.get("parameters").getAsJsonObject();
        assertEquals(parameters.get(ANY_PARAM).getAsString(), ANY_VALUE);
    }
}
