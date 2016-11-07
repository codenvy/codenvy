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
package com.codenvy.auth.aws.ecr;

import com.codenvy.auth.aws.AwsAccountCredentials;
import com.google.inject.Singleton;

import org.eclipse.che.inject.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static java.lang.String.format;

/**
 * Keeps auth configurations for AWS Elastic Container Registry.
 * Credentials might be configured in .properties files.
 *
 * @author Mykola Morhun
 * @author Alexander Andrienko
 */
@Singleton
public class AwsEcrInitialAuthConfig {

    private static final Logger LOG = LoggerFactory.getLogger(AwsEcrInitialAuthConfig.class);

    private static final String CONFIG_PREFIX                = "codenvy.docker.registry.aws.";
    private static final String CONFIGURATION_PREFIX_PATTERN = "codenvy\\.docker\\.registry\\.aws\\..+";

    private static final String VALID_DOCKER_PROPERTY_NAME_EXAMPLE = CONFIG_PREFIX + "aws_registry_name.parameter_name";

    private static final String AWS_ACCOUNT_ID    = "id";
    private static final String AWS_REGION        = "region";
    private static final String ACCESS_KEY_ID     = "access_key_id";
    private static final String SECRET_ACCESS_KEY = "secret_access_key";

    /** format: (registry hostname) -> (AWS credentials: id, region, keyId, key) */
    private Map<String, AwsAccountCredentials> authConfigs;

    @Inject
    public AwsEcrInitialAuthConfig(ConfigurationProperties configurationProperties) {
        Map<String, String> awsAuthProperties = configurationProperties.getProperties(CONFIGURATION_PREFIX_PATTERN);

        Set<String> registryPrefixes = awsAuthProperties.entrySet()
                                                        .stream()
                                                        .map(property -> getRegistryPrefix(property.getKey()))
                                                        .collect(Collectors.toSet());

        authConfigs = newHashMapWithExpectedSize(registryPrefixes.size());
        for (String awsEcrPrefix : registryPrefixes) {
            String id = getPropertyValue(awsAuthProperties, awsEcrPrefix + AWS_ACCOUNT_ID);
            String region = getPropertyValue(awsAuthProperties, awsEcrPrefix + AWS_REGION);
            String keyId = getPropertyValue(awsAuthProperties, awsEcrPrefix + ACCESS_KEY_ID);
            String keyValue = getPropertyValue(awsAuthProperties, awsEcrPrefix + SECRET_ACCESS_KEY);

            authConfigs.put(calculateAwsEcrHostname(id, region),
                            new AwsAccountCredentials(id, region, keyId, keyValue));
        }
    }

    /**
     * @return configured AWS credentials or empty map if not credentials found
     */
    public Map<String, AwsAccountCredentials> getAuthConfigs() {
        return authConfigs;
    }

    private String getRegistryPrefix(String propertyName) {
        String[] parts = propertyName.replaceFirst(CONFIG_PREFIX, "").split("\\.");

        if (parts.length < 2) {
            throw new IllegalArgumentException(format("In the property '%s' is missing '.'. Valid credential registry format is '%s'",
                                                      propertyName, VALID_DOCKER_PROPERTY_NAME_EXAMPLE));
        }
        if (parts.length > 2) {
            throw new IllegalArgumentException(format("Property '%s' contains redundant '.'. Valid credential registry format is '%s'",
                                                      propertyName, VALID_DOCKER_PROPERTY_NAME_EXAMPLE));
        }

        String propertyIdentifier = parts[1];
        if (!AWS_ACCOUNT_ID.equals(propertyIdentifier) &&
            !AWS_REGION.equals(propertyIdentifier) &&
            !ACCESS_KEY_ID.equals(propertyIdentifier) &&
            !SECRET_ACCESS_KEY.equals(propertyIdentifier)) {
            LOG.warn("Set unused property: '{}'.", propertyName);
        }

        return CONFIG_PREFIX + parts[0] + ".";
    }

    private String getPropertyValue(Map<String, String> authProperties, String propertyName) {
        String propertyValue = authProperties.get(propertyName);
        if (isNullOrEmpty(propertyValue)) {
            throw new IllegalArgumentException(format("Property '%s' is missing.", propertyName));
        }
        return propertyValue;
    }

    private String calculateAwsEcrHostname(String awsAccountId, String awsRegion) {
        if (!isNullOrEmpty(awsAccountId) && !isNullOrEmpty(awsRegion)) {
            return awsAccountId + ".dkr.ecr." + awsRegion + ".amazonaws.com";
        }
        return null;
    }

}
