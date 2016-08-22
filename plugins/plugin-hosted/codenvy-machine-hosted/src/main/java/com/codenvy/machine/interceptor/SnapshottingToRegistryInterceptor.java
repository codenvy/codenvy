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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ecr.AmazonECRClient;
import com.amazonaws.services.ecr.model.CreateRepositoryRequest;
import com.codenvy.auth.aws.ecr.AwsInitialAuthConfig;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * This interceptor creates repository in remote registry if it doesn't support repository creation on docker push command.
 *
 * @author Mykola Morhun
 */
@Singleton
public class SnapshottingToRegistryInterceptor implements MethodInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(SnapshottingToRegistryInterceptor.class);

    // https://aws_account_id.dkr.ecr.region.amazonaws.com
    private static final String AMAZON_ECR_URL_REG_EXP = "^\\d{12}\\.dkr\\.ecr\\..+-\\d\\.amazonaws\\.com$";
    private static final Pattern AMAZON_ECR_URL_PATTERN = Pattern.compile(AMAZON_ECR_URL_REG_EXP);

    @Inject
    private AwsInitialAuthConfig awsInitialAuthConfig;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Object[] arguments = methodInvocation.getArguments();
        String repository = (String) arguments[0];
        String registry = (String) arguments[1];

        if (registry != null) {
            try {
                if (AMAZON_ECR_URL_PATTERN.matcher(registry).find()) {
                    AmazonECRClient amazonECRClient = getAwsEcrClient();
                    CreateRepositoryRequest createRepositoryForSnapshotRequest = new CreateRepositoryRequest();
                    createRepositoryForSnapshotRequest.setRepositoryName(repository);
                    amazonECRClient.createRepository(createRepositoryForSnapshotRequest);
                }
            } catch (Exception e) {
                LOG.error("Unable to create {} repository for {} registry. Reason: {}", repository, registry, e);
            }
        }

        return methodInvocation.proceed();
    }

    @VisibleForTesting
    AmazonECRClient getAwsEcrClient() {
        AWSCredentials credentials = new BasicAWSCredentials(awsInitialAuthConfig.getAccessKeyId(),
                                                             awsInitialAuthConfig.getSecretAccessKey());
        return new AmazonECRClient(credentials);
    }

}
