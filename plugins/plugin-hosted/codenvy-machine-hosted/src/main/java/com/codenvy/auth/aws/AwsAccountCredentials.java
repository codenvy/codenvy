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
package com.codenvy.auth.aws;

/**
 * Holds AWS account credentials
 *
 * @author Mykola Morhun
 */
public class AwsAccountCredentials {
    private final String id;
    private final String region;
    private final String accessKeyId;
    private final String secretAccessKey;

    public AwsAccountCredentials(String id, String region, String accessKeyId, String secretAccessKey) {
        this.id = id;
        this.region = region;
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
    }

    public String getId() {
        return id;
    }

    public String getRegion() {
        return region;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

}
