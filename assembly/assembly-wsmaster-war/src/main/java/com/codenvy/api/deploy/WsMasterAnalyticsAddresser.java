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
package com.codenvy.api.deploy;

import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.schedule.ScheduleRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * @author Mihail Kuznyetsov
 */
@Singleton
public class WsMasterAnalyticsAddresser {

    private static final Logger LOG = LoggerFactory.getLogger(WsMasterAnalyticsAddresser.class);

    public static final String ID = NameGenerator.generate("COM", 10);

    @ScheduleRate(period = 1, unit = TimeUnit.HOURS)
    void send() {
        HttpURLConnection connection = null;
        try {
            final URL url = new URL("https://install.codenvycorp.com/codenvy/init/server?ID=" + ID);
            connection = (HttpsURLConnection)url.openConnection();
            connection.getResponseCode();
        } catch (Exception e) {
            LOG.debug("Failed to send master analytics", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
