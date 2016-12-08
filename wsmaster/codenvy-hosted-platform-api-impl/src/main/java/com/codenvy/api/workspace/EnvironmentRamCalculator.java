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
package com.codenvy.api.workspace;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.environment.server.EnvironmentParser;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.commons.lang.Size;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Helps to calculate amount of RAM defined in {@link Environment environment}
 *
 * @author Sergii Leschenko
 */
public class EnvironmentRamCalculator {
    private static final long BYTES_TO_MEGABYTES_DIVIDER = 1024L * 1024L;

    private final EnvironmentParser environmentParser;
    private final long              defaultMachineMemorySizeBytes;

    @Inject
    public EnvironmentRamCalculator(EnvironmentParser environmentParser,
                                    @Named("che.workspace.default_memory_mb") int defaultMachineMemorySizeMB) {
        this.environmentParser = environmentParser;
        this.defaultMachineMemorySizeBytes = Size.parseSize(defaultMachineMemorySizeMB + "MB");
    }

    /**
     * Parses (and fetches if needed) recipe of environment and sums RAM size of all machines in environment in megabytes.
     */
    public long calculate(Environment environment) throws ServerException {
        CheServicesEnvironmentImpl composeEnv = environmentParser.parse(environment);

        long sumBytes = composeEnv.getServices()
                                  .values()
                                  .stream()
                                  .mapToLong(value -> {
                                      if (value.getMemLimit() == null || value.getMemLimit() == 0) {
                                          return defaultMachineMemorySizeBytes;
                                      } else {
                                          return value.getMemLimit();
                                      }
                                  })
                                  .sum();
        return sumBytes / BYTES_TO_MEGABYTES_DIVIDER;
    }
}
