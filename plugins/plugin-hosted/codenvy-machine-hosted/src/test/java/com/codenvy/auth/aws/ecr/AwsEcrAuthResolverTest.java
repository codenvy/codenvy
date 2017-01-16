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
package com.codenvy.auth.aws.ecr;

import com.codenvy.auth.aws.AwsAccountCredentials;

import org.eclipse.che.plugin.docker.client.dto.AuthConfig;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Mykola Morhun
 */
@Listeners(MockitoTestNGListener.class)
public class AwsEcrAuthResolverTest {

    private static final String AWS_ID                = "123456789012";
    private static final String AWS_REGION            = "ua-north-1";
    private static final String AWS_ACCESS_KEY_ID     = "ABCDEFGHIJKLMNOPQRST";
    private static final String AWS_SECRET_ACCESS_KEY = "vERYverY+veRy+VeRySEcRETkEYfOrACCEss4YOu";

    private static final String AWS_ECR = AWS_ID + ".dkr.ecr." + AWS_REGION + ".amazonaws.com";

    private static final String USERNAME = "AWS";
    private static final String PASSWORD = "dynamicTokenIsHere";

    private static final String AUTHORIZATION_TOKEN = "QVdTOmR5bmFtaWNUb2tlbklzSGVyZQ==";

    @Mock
    private AwsEcrInitialAuthConfig awsEcrInitialAuthConfig;

    private AwsAccountCredentials awsAccountCredentials;

    private AwsEcrAuthResolver awsEcrAuthResolver;

    @BeforeClass
    private void before() {
        awsAccountCredentials = new AwsAccountCredentials(AWS_ID, AWS_REGION, AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
    }

    @BeforeMethod
    private void beforeTest() {
        Map<String, AwsAccountCredentials> configuredCredentials = new HashMap<>();
        configuredCredentials.put(AWS_ECR, awsAccountCredentials);
        when(awsEcrInitialAuthConfig.getAuthConfigs()).thenReturn(configuredCredentials);

        awsEcrAuthResolver = spy(new AwsEcrAuthResolver(awsEcrInitialAuthConfig));

        doReturn(AUTHORIZATION_TOKEN).when(awsEcrAuthResolver).getAwsAuthorizationToken(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
    }

    @Test
    public void shouldBeAbleToGetDynamicXRegistryAuth() {
        AuthConfig authConfig =
                awsEcrAuthResolver.getXRegistryAuth(AWS_ECR);

        verify(awsEcrAuthResolver).getAwsAuthorizationToken(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);

        assertEquals(authConfig.getUsername(), USERNAME);
        assertEquals(authConfig.getPassword(), PASSWORD);
    }

    @Test
    public void shouldReturnNullWhenGetDynamicXRegistryAuthIfRegistryIsNull() {
        assertNull(awsEcrAuthResolver.getXRegistryAuth(null));
    }

    @Test
    public void shouldReturnNullWhenGetDynamicXRegistryAuthIfCredentialsForSpecifiedRegistryIsNotConfigured() {
        assertNull(awsEcrAuthResolver.getXRegistryAuth("my.registry.com"));
    }

    @Test
    public void shouldReturnNullWhenGetDynamicXRegistryAuthIfCredentialsIsNotConfigured() {
        when(awsEcrInitialAuthConfig.getAuthConfigs()).thenReturn(new HashMap<>());

        assertNull(awsEcrAuthResolver.getXRegistryAuth(AWS_ECR));
    }

    @Test
    public void shouldReturnNullWhenGetDynamicXRegistryAuthIfFailedToRetrieveAuthToken() {
        doReturn(null).when(awsEcrAuthResolver).getAwsAuthorizationToken(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);

        assertNull(awsEcrAuthResolver.getXRegistryAuth(AWS_ECR));
    }

    @Test
    public void shouldReturnNullWhenGetDynamicXRegistryAuthIfRetrievedAuthTokenCannotBeDecodedAsBase64() {
        doReturn("QVdTOmR5SomEvRoNgBaSe64VaLuE+=").when(awsEcrAuthResolver).getAwsAuthorizationToken(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);

        assertNull(awsEcrAuthResolver.getXRegistryAuth(AWS_ECR));
    }

    @Test
    public void shouldReturnNullWhenGetDynamicXRegistryAuthIfRetrievedAuthTokenHasWrongFormat() {
        doReturn("J7sLO0KsMwKKy7h1").when(awsEcrAuthResolver).getAwsAuthorizationToken(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);

        assertNull(awsEcrAuthResolver.getXRegistryAuth(AWS_ECR));
    }

    @Test
    public void shouldGetDynamicXRegistryAuthOnlyForGivenRegistry() {
        Map<String, AwsAccountCredentials> configuredCredentials = new HashMap<>();
        configuredCredentials.put("id1234.dkr.ecr.region-1.amazonaws.com", new AwsAccountCredentials("id1234", "region-1", "accessKey", "secretKey"));
        configuredCredentials.put(AWS_ECR, awsAccountCredentials);
        configuredCredentials.put("102030.dkr.ecr.region-2.amazonaws.com", new AwsAccountCredentials("102030", "region-2", "accessKey2", "secretKey2"));
        when(awsEcrInitialAuthConfig.getAuthConfigs()).thenReturn(configuredCredentials);

        AuthConfig authConfig =
                awsEcrAuthResolver.getXRegistryAuth(AWS_ECR);

        verify(awsEcrAuthResolver, times(1)).getAwsAuthorizationToken(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);

        assertEquals(authConfig.getUsername(), USERNAME);
        assertEquals(authConfig.getPassword(), PASSWORD);
    }

    @Test
    public void shouldBeAbleToGetDynamicXRegistryConfig() {
        Map<String, AuthConfig> authConfigsMap =
                awsEcrAuthResolver.getXRegistryConfig();

        verify(awsEcrAuthResolver, atLeastOnce()).getXRegistryAuth(anyString());
        AuthConfig authConfig = authConfigsMap.get(AWS_ECR);
        assertEquals(authConfig.getUsername(), USERNAME);
        assertEquals(authConfig.getPassword(), PASSWORD);
    }

    @Test
    public void shouldGetDynamicXRegistryConfigForAllConfiguredRegistries() {
        Map<String, AwsAccountCredentials> configuredCredentials = new HashMap<>();
        configuredCredentials.put(AWS_ECR, awsAccountCredentials);
        configuredCredentials.put("id1234.dkr.ecr.region-1.amazonaws.com", new AwsAccountCredentials("id1234", "region-1", "accessKey", "secretKey"));
        when(awsEcrInitialAuthConfig.getAuthConfigs()).thenReturn(configuredCredentials);
        doReturn("QVdTOmR5bmFtaWNQYXNzd29yZA==").when(awsEcrAuthResolver).getAwsAuthorizationToken("accessKey", "secretKey");

        Map<String, AuthConfig> authConfigsMap =
                awsEcrAuthResolver.getXRegistryConfig();

        verify(awsEcrAuthResolver, atLeastOnce()).getXRegistryAuth(anyString());
        assertTrue(authConfigsMap.size() == 2);
        AuthConfig authConfig = authConfigsMap.get(AWS_ECR);
        assertEquals(authConfig.getUsername(), USERNAME);
        assertEquals(authConfig.getPassword(), PASSWORD);
        authConfig = authConfigsMap.get("id1234.dkr.ecr.region-1.amazonaws.com");
        assertEquals(authConfig.getUsername(), USERNAME);
        assertEquals(authConfig.getPassword(), "dynamicPassword");
    }

    @Test
    public void shouldGetDynamicXRegistryConfigForRegistriesForWhichPossibleGetToken() {
        Map<String, AwsAccountCredentials> configuredCredentials = new HashMap<>();
        configuredCredentials.put(AWS_ECR, awsAccountCredentials);
        configuredCredentials.put("id1234.dkr.ecr.region-1.amazonaws.com", new AwsAccountCredentials("id1234", "region-1", "accessKey", "secretKey"));
        when(awsEcrInitialAuthConfig.getAuthConfigs()).thenReturn(configuredCredentials);
        doReturn(null).when(awsEcrAuthResolver).getAwsAuthorizationToken("accessKey", "secretKey");

        Map<String, AuthConfig> authConfigsMap =
                awsEcrAuthResolver.getXRegistryConfig();

        verify(awsEcrAuthResolver, atLeastOnce()).getXRegistryAuth(anyString());
        assertTrue(authConfigsMap.size() == 1);
        AuthConfig authConfig = authConfigsMap.get(AWS_ECR);
        assertEquals(authConfig.getUsername(), USERNAME);
        assertEquals(authConfig.getPassword(), PASSWORD);
    }

    @Test
    public void shouldReturnEmptyMapWhenGetDynamicXRegistryConfigIfCredentialsIsNotConfigured() {
        when(awsEcrInitialAuthConfig.getAuthConfigs()).thenReturn(new HashMap<>());

        Map<String, AuthConfig> authConfigsMap =
                awsEcrAuthResolver.getXRegistryConfig();

        assertTrue(authConfigsMap.size() == 0);
    }

}
