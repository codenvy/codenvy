/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
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

  public AwsAccountCredentials(
      String id, String region, String accessKeyId, String secretAccessKey) {
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
