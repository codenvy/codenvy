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

import org.eclipse.che.inject.ConfigurationProperties;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Mykola Morhun
 */
@Listeners(MockitoTestNGListener.class)
public class AwsInitialAuthConfigTest {

    private final String CONFIG_PREFIX = "codenvy.docker.registry.aws.";

    private static final String AWS_ACCOUNT_ID    = "id";
    private static final String AWS_REGION        = "region";
    private static final String ACCESS_KEY_ID     = "access_key_id";
    private static final String SECRET_ACCESS_KEY = "secret_access_key";

    private final String AWS_REGISTRY_NAME_1 = "aws-ecr_reg";
    private final String AWS_REGISTRY_NAME_2 = "awsregistry";

    private final String AWS_ID_VALUE_1                = "123456789012";
    private final String AWS_REGION_VALUE_1            = "ua-north-1";
    private final String AWS_ACCESS_KEY_ID_VALUE_1     = "ABCDEFGHIJKLMNOPQRST";
    private final String AWS_SECRET_ACCESS_KEY_VALUE_1 = "vERYverY+veRy+VeRySEcRETkEYfOrACCEss4YOu";

    private final String AWS_ID_VALUE_2                = "000000000000";
    private final String AWS_REGION_VALUE_2            = "ua-west-1";
    private final String AWS_ACCESS_KEY_ID_VALUE_2     = "AAAAAAAAAAAAAAAAAAAA";
    private final String AWS_SECRET_ACCESS_KEY_VALUE_2 = "SeCrEtKeYsEcReTkEySeCrEtKeYsEcReTkEySeCr";

    private final String AWS_ECR_HOSTNAME_1 = AWS_ID_VALUE_1 + ".dkr.ecr." + AWS_REGION_VALUE_1 + ".amazonaws.com";
    private final String AWS_ECR_HOSTNAME_2 = AWS_ID_VALUE_2 + ".dkr.ecr." + AWS_REGION_VALUE_2 + ".amazonaws.com";

    @Mock
    private ConfigurationProperties configurationProperties;

    private Map<String, String> properties;

    private AwsEcrInitialAuthConfig awsInitialAuthConfig;

    @BeforeMethod
    public void beforeTest() {
        properties = new HashMap<>();
        doReturn(properties).when(configurationProperties).getProperties(anyString());
    }

    @Test
    public void shouldParseAwsEcrConfigs() {
        // given
        addAwsAccountConfig(AWS_REGISTRY_NAME_1, AWS_ID_VALUE_1, AWS_REGION_VALUE_1, AWS_ACCESS_KEY_ID_VALUE_1, AWS_SECRET_ACCESS_KEY_VALUE_1);
        addAwsAccountConfig(AWS_REGISTRY_NAME_2, AWS_ID_VALUE_2, AWS_REGION_VALUE_2, AWS_ACCESS_KEY_ID_VALUE_2, AWS_SECRET_ACCESS_KEY_VALUE_2);

        // when
        awsInitialAuthConfig = spy(new AwsEcrInitialAuthConfig(configurationProperties));

        // then
        Map<String, AwsAccountCredentials> awsInitialConfig = awsInitialAuthConfig.getAuthConfigs();
        AwsAccountCredentials credentials1 = awsInitialConfig.get(AWS_ECR_HOSTNAME_1);
        assertNotNull(credentials1);
        assertEquals(credentials1.getId(), AWS_ID_VALUE_1);
        assertEquals(credentials1.getRegion(), AWS_REGION_VALUE_1);
        assertEquals(credentials1.getAccessKeyId(), AWS_ACCESS_KEY_ID_VALUE_1);
        assertEquals(credentials1.getSecretAccessKey(), AWS_SECRET_ACCESS_KEY_VALUE_1);
        AwsAccountCredentials credentials2 = awsInitialConfig.get(AWS_ECR_HOSTNAME_2);
        assertNotNull(credentials2);
        assertEquals(credentials2.getId(), AWS_ID_VALUE_2);
        assertEquals(credentials2.getRegion(), AWS_REGION_VALUE_2);
        assertEquals(credentials2.getAccessKeyId(), AWS_ACCESS_KEY_ID_VALUE_2);
        assertEquals(credentials2.getSecretAccessKey(), AWS_SECRET_ACCESS_KEY_VALUE_2);
    }

    @Test
    public void shouldAcceptNoConfiguredAws() {
        // given
        properties = new HashMap<>();
        doReturn(properties).when(configurationProperties).getProperties(anyString());
        
        // when
        awsInitialAuthConfig = spy(new AwsEcrInitialAuthConfig(configurationProperties));

        // then
        assertEquals(awsInitialAuthConfig.getAuthConfigs().size(), 0);
    }

    @Test(dataProvider = "invalidConfigs",
          expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Property '.*' is missing\\.")
    public void shouldThrowIllegalArgumentExceptionIfCredentialsNotFullyConfigured(String awsId,
                                                                                   String awsRegion,
                                                                                   String accessKeyId,
                                                                                   String secretAccessKey) {
        addAwsAccountConfig("some-registry", awsId, awsRegion, accessKeyId, secretAccessKey);

        awsInitialAuthConfig = spy(new AwsEcrInitialAuthConfig(configurationProperties));
    }

    @Test(dataProvider = "incompleteConfigs",
          expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Property '.*' is missing\\.")
    public void shouldThrowIllegalArgumentExceptionIfSomeValuesInPropertiesNotConfigured(String key1, String value1,
                                                                                         String key2, String value2,
                                                                                         String key3, String value3) {
        final String prefix = CONFIG_PREFIX + "incomplete.";
        properties.put(prefix + key1, value1);
        properties.put(prefix + key2, value2);
        properties.put(prefix + key3, value3);

        awsInitialAuthConfig = spy(new AwsEcrInitialAuthConfig(configurationProperties));
    }

    @Test (expectedExceptions = IllegalArgumentException.class,
           expectedExceptionsMessageRegExp = "Property '.*' contains redundant '\\.'.*")
    public void shouldThrowIllegalArgumentExceptionIfAwsAccountNameContainDot() {
        addAwsAccountConfig("bad.name", AWS_ID_VALUE_1, AWS_REGION_VALUE_1, AWS_ACCESS_KEY_ID_VALUE_1, AWS_SECRET_ACCESS_KEY_VALUE_1);

        awsInitialAuthConfig = spy(new AwsEcrInitialAuthConfig(configurationProperties));
    }

    @Test (expectedExceptions = IllegalArgumentException.class,
           expectedExceptionsMessageRegExp = "In the property '.*' is missing '\\.'.*")
    public void shouldThrowIllegalArgumentExceptionIfAwsAccountNameDoNotSpecified() {
        properties.put(CONFIG_PREFIX + AWS_ACCOUNT_ID, AWS_ID_VALUE_1);
        properties.put(CONFIG_PREFIX + AWS_REGION, AWS_REGION_VALUE_1);
        properties.put(CONFIG_PREFIX + ACCESS_KEY_ID, AWS_ACCESS_KEY_ID_VALUE_1);
        properties.put(CONFIG_PREFIX + SECRET_ACCESS_KEY, AWS_SECRET_ACCESS_KEY_VALUE_1);

        awsInitialAuthConfig = spy(new AwsEcrInitialAuthConfig(configurationProperties));
    }

    @DataProvider(name = "invalidConfigs")
    private Object[][] invalidConfigs() {
        return new Object[][] {
                {null, AWS_REGION_VALUE_1, AWS_ACCESS_KEY_ID_VALUE_1, AWS_SECRET_ACCESS_KEY_VALUE_1},
                {AWS_ID_VALUE_1, null, AWS_ACCESS_KEY_ID_VALUE_1, AWS_SECRET_ACCESS_KEY_VALUE_1},
                {AWS_ID_VALUE_1, AWS_REGION, null, AWS_SECRET_ACCESS_KEY_VALUE_1},
                {AWS_ID_VALUE_1, AWS_REGION, AWS_ACCESS_KEY_ID_VALUE_1, null},
                {"", AWS_REGION_VALUE_1, AWS_ACCESS_KEY_ID_VALUE_1, AWS_SECRET_ACCESS_KEY_VALUE_1},
                {AWS_ID_VALUE_1, "", AWS_ACCESS_KEY_ID_VALUE_1, AWS_SECRET_ACCESS_KEY_VALUE_1},
                {AWS_ID_VALUE_1, AWS_REGION, "", AWS_SECRET_ACCESS_KEY_VALUE_1},
                {AWS_ID_VALUE_1, AWS_REGION, AWS_ACCESS_KEY_ID_VALUE_1, ""},
                {AWS_ID_VALUE_1, "", null, AWS_SECRET_ACCESS_KEY_VALUE_1}
        };
    }

    @DataProvider(name = "incompleteConfigs")
    private Object[][] incompleteConfigs() {
        return new Object[][] {
                {AWS_REGION, AWS_REGION_VALUE_1, ACCESS_KEY_ID, AWS_ACCESS_KEY_ID_VALUE_1, SECRET_ACCESS_KEY, AWS_SECRET_ACCESS_KEY_VALUE_1},
                {AWS_ACCOUNT_ID, AWS_ID_VALUE_1, ACCESS_KEY_ID, AWS_ACCESS_KEY_ID_VALUE_1, SECRET_ACCESS_KEY, AWS_SECRET_ACCESS_KEY_VALUE_1},
                {AWS_ACCOUNT_ID, AWS_ID_VALUE_1, AWS_REGION, AWS_REGION_VALUE_1, SECRET_ACCESS_KEY, AWS_SECRET_ACCESS_KEY_VALUE_1},
                {AWS_ACCOUNT_ID, AWS_ID_VALUE_1, AWS_REGION, AWS_REGION_VALUE_1, ACCESS_KEY_ID, AWS_ACCESS_KEY_ID_VALUE_1}
        };
    }

    private void addAwsAccountConfig(String name, String id, String region, String keyId, String keyValue) {
        String awsEcrConfigPrefix = CONFIG_PREFIX + name + '.';
        properties.put(awsEcrConfigPrefix + AWS_ACCOUNT_ID, id);
        properties.put(awsEcrConfigPrefix + AWS_REGION, region);
        properties.put(awsEcrConfigPrefix + ACCESS_KEY_ID, keyId);
        properties.put(awsEcrConfigPrefix + SECRET_ACCESS_KEY, keyValue);
    }

}
