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
package com.codenvy.im.update;

import com.mongodb.client.MongoDatabase;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;


/**
 * @author Anatoliy Bazko
 */
public class MongoStorageTest {

    @Test
    public void testInitMongo() throws Exception {
        MongoStorage mongoStorage = new MongoStorage("mongodb://localhost:12000/test", true, "target");
        MongoDatabase db = mongoStorage.getDb();

        assertNotNull(db);
    }
}