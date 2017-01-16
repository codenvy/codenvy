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
package com.codenvy.im.response;

import org.testng.annotations.Test;

import java.util.Collections;

import static com.codenvy.im.utils.Commons.fromJson;
import static com.codenvy.im.utils.Commons.toJson;
import static org.testng.Assert.assertEquals;


/**
 * @author Anatoliy Bazko
 */
public class UpdateResponseTest {

    @Test
    public void test() throws Exception {
        UpdateResponse result = new UpdateResponse();
        result.setStatus(ResponseCode.ERROR);
        result.setMessage("error");
        result.setArtifacts(Collections.<UpdateArtifactInfo>emptyList());

        String json = toJson(result);
        assertEquals(json, "{\n" +
                           "  \"artifacts\" : [ ],\n" +
                           "  \"message\" : \"error\",\n" +
                           "  \"status\" : \"ERROR\"\n" +
                           "}");
        assertEquals(fromJson(json, UpdateResponse.class), result);
    }
}