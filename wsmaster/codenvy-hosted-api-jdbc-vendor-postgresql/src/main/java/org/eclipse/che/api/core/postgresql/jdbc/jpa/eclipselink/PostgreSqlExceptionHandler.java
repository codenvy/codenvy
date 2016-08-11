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
package org.eclipse.che.api.core.postgresql.jdbc.jpa.eclipselink;

import org.eclipse.che.api.core.jdbc.jpa.DuplicateKeyException;
import org.eclipse.che.api.core.jdbc.jpa.IntegrityConstraintViolationException;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.ExceptionHandler;

import java.sql.SQLException;

/**
 * Rethrows vendor specific exceptions as common exceptions.
 * See <a https://www.postgresql.org/docs/9.4/static/errcodes-appendix.html">PostgreSQL error codes</a>.
 *
 * @author Yevhenii Voevodin
 * @author Sergii Kabashniuk
 *
 */
public class PostgreSqlExceptionHandler implements ExceptionHandler {

    public Object handleException(RuntimeException exception) {

        if (exception instanceof DatabaseException && exception.getCause() instanceof SQLException) {
            final SQLException sqlEx = (SQLException)exception.getCause();
            switch (sqlEx.getSQLState()) {
                case "23505":
                    throw new DuplicateKeyException(exception.getMessage(), exception);
                case "23503":
                    throw new IntegrityConstraintViolationException(exception.getMessage(), exception);
            }
        }
        throw exception;
    }
}
