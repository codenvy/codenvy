/*
 *  [2012] - [2016] Codenvy, S.A.
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
import org.eclipse.che.commons.test.tck.AbstractTestListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * Waits until connection to the database is established,
 * fails if attempts count is reached.
 *
 * @author Yevhenii Voevodin
 */
public class WaitConnectionListener extends AbstractTestListener {

    private static final Logger LOG = LoggerFactory.getLogger(WaitConnectionListener.class);

    @Override
    public void onStart(ITestContext context) {
        final String dbUrl = System.getProperty("jdbc.url");
        final String dbUser = System.getProperty("jdbc.user");
        final String dbPassword = System.getProperty("jdbc.password");

        // Tries to establish connection 10 times with 1 second delay
        boolean isAvailable = false;
        for (int i = 0; i < 10 && !isAvailable; i++) {
            try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                isAvailable = true;
            } catch (SQLException x) {
                LOG.warn("An attempt to connect to the database failed with an error: {}", x.getLocalizedMessage());
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException x) {
                throw new RuntimeException(x.getLocalizedMessage(), x);
            }
        }
        if (!isAvailable) {
            throw new IllegalStateException("Couldn't initialize connection with a database");
        }
    }
}
