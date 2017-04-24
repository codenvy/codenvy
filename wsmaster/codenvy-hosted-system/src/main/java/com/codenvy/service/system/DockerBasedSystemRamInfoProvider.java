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
package com.codenvy.service.system;

import com.google.inject.Inject;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorProvider;
import org.eclipse.che.plugin.docker.client.json.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.deepToString;
import static org.eclipse.che.commons.lang.Size.parseSize;

/**
 * Implementation of {@link SystemRamInfoProvider} based on docker.
 *
 * @author Igor Vinokur
 */
public class DockerBasedSystemRamInfoProvider implements SystemRamInfoProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DockerBasedSystemRamInfoProvider.class);

    private static final String SYSTEM_RAM_INFO_ERROR = "An error occurred while getting system RAM info.";

    private final DockerConnector dockerConnector;

    @Inject
    public DockerBasedSystemRamInfoProvider(DockerConnectorProvider dockerConnectorProvider) {
        this.dockerConnector = dockerConnectorProvider.get();
    }

    @Override
    public SystemRamInfo getSystemRamInfo() throws ServerException {
        SystemInfo systemInfo;
        try {
            systemInfo = dockerConnector.getSystemInfo();
        } catch (IOException e) {
            LOG.error("Failed to retrieve system information from docker.", e);
            throw new ServerException(SYSTEM_RAM_INFO_ERROR, e);
        }
        String[][] driverStatus = systemInfo.getDriverStatus();
        String[][] systemStatus = systemInfo.getSystemStatus();
        String[][] statusOutput = driverStatus == null ? systemStatus : driverStatus;
        if (statusOutput == null) {
            LOG.error("Empty system information was received from docker. Whole system info from docker {}", systemInfo);
            throw new ServerException(SYSTEM_RAM_INFO_ERROR);
        }

        List<String> allNodesRamUsage = new ArrayList<>();
        /*
          System values from docker response are introduced in a 2-dimensional string array e.g.:
          "SystemStatus": [
             .
             .
             .
             " node1.<host>",
             "<host>:<port>"
             ],
             "  └ Reserved Memory",
             "1000 MiB / 5.881 GiB"
             ],
             .
             .
             .
             " node2.<host>",
             "<host>:<port>"
             ],
             [
             "  └ Reserved Memory",
             "1000 MiB / 1.511 GiB"
             ],
             .
             .
             .
          ],
          RAM vales are divided by nodes and introduced in a string array which has '└ Reserved Memory' in first element,
          and RAM values of node in format <used RAM size> / <total RAM size> in second element.
          There are as many RAM values arrays inside the 'statusOutput' array as many nodes are present in the system.
         */
        for (String[] systemInfoEntry : statusOutput) {
            if (systemInfoEntry.length == 2 && " └ Reserved Memory".equals(systemInfoEntry[0])) {
                allNodesRamUsage.add(systemInfoEntry[1]);
            }
        }
        if (allNodesRamUsage.isEmpty()) {
            LOG.error("System RAM values was not found in docker system info response. All system values from docker: {}",
                      deepToString(statusOutput));
            throw new ServerException(SYSTEM_RAM_INFO_ERROR);
        }

        long systemRamUsed = 0;
        long systemRamTotal = 0;
        for (String nodeRamUsage : allNodesRamUsage) {
            String[] ramValues = nodeRamUsage.split(" / ");
            if (ramValues.length != 2) {
                LOG.error("A problem occurred while parsing system information from docker. " +
                          "Expected: <used RAM size> / <total RAM size> but got: " + nodeRamUsage);
                throw new ServerException(SYSTEM_RAM_INFO_ERROR);
            }
            systemRamUsed += parseSize(ramValues[0]);
            systemRamTotal += parseSize(ramValues[1]);
        }

        return new SystemRamInfo(systemRamUsed, systemRamTotal);
    }
}
