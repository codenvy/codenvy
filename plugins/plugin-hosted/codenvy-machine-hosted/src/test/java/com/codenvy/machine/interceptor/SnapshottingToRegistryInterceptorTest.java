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
package com.codenvy.machine.interceptor;

import com.amazonaws.services.ecr.AmazonECRClient;
import com.codenvy.auth.aws.ecr.AwsInitialAuthConfig;

import org.aopalliance.intercept.MethodInvocation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Mykola Morhun
 */
@Listeners(MockitoTestNGListener.class)
public class SnapshottingToRegistryInterceptorTest {

    private static final String REPOSITORY = "repository1234";

    @Mock
    private MethodInvocation     methodInvocation;
    @Mock
    private AwsInitialAuthConfig awsInitialAuthConfig;
    @Mock
    private AmazonECRClient      amazonEcrClient;

    @Spy
    @InjectMocks
    private SnapshottingToRegistryInterceptor interceptor;

    @BeforeMethod
    public void setup() throws Throwable {
        doReturn(null).when(methodInvocation).proceed(); // do nothing on methodInvocation.proceed()
        doReturn(amazonEcrClient).when(interceptor).getAwsEcrClient();
        doReturn(null).when(amazonEcrClient).createRepository(anyObject()); // prevent real request to AWS

        when(awsInitialAuthConfig.getAccessKeyId()).thenReturn("1234567890");
        when(awsInitialAuthConfig.getSecretAccessKey()).thenReturn("secretValue");
    }

    @Test
    public void shouldInvokeInterceptedMethod() throws Throwable {
        addArgumentsToInvocation(REPOSITORY, "");

        interceptor.invoke(methodInvocation);

        verify(methodInvocation).proceed();
    }

    @Test(dataProvider = "nonAwsEcrHostnameProvider")
    public void shouldNotCreateNewRepositoryInAwsEcr(String registry) throws Throwable {
        addArgumentsToInvocation(REPOSITORY, registry);

        interceptor.invoke(methodInvocation);

        verify(amazonEcrClient, never()).createRepository(anyObject());
    }

    @Test(dataProvider = "awsEcrHostnameProvider")
    public void shouldCreateNewRepositoryInAwsEcr(String registry) throws Throwable {
        addArgumentsToInvocation(REPOSITORY, registry);

        interceptor.invoke(methodInvocation);

        verify(amazonEcrClient).createRepository(anyObject());
    }

    @DataProvider(name = "nonAwsEcrHostnameProvider")
    private Object[][] nonAwsEcrHostnameProvider() {
        return new Object[][] {
                {null},
                {""},
                {"docker.io"},
                {"https://index.docker.io/v2/"},
                {"registry.com"},
                {"some.registry.com:5000"},
                {"1234567890123.dkr.ecr.us-east-1.amazonaws.com"},
                {"12345678901.dkr.ecr.us-east-1.amazonaws.com"},
                {"123456789012.dkr.ecr.us-east.amazonaws.com"},
                {"123456789012.dkr.ecr.us-east-1.amazon.com"},
                {"123456789012.dkr.us-east-1.amazonaws.com"},
                {"123456789012.ecr.us-east-1.amazonaws.com"},
                {"123456789012.dkr.ecr.us-east-1.amazonaws.com:5000"},
                {"https://123456789012.dkr.ecr.us-east-1.amazonaws.com"}
        };
    }

    @DataProvider(name = "awsEcrHostnameProvider")
    private Object[][] awsEcrHostnameProvider() {
        return new Object[][] {
                {"123456789012.dkr.ecr.us-east-1.amazonaws.com"},
                {"123456789012.dkr.ecr.ua-north-5.amazonaws.com"}
        };
    }

    private void addArgumentsToInvocation(String repositoryArg, String registryArg) {
        String[] arguments = new String[2];
        arguments[0] = repositoryArg;
        arguments[1] = registryArg;

        when(methodInvocation.getArguments()).thenReturn(arguments);
    }

}
