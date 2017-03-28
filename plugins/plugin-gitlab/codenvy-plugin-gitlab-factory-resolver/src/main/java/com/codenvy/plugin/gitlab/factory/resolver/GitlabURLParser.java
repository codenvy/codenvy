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
package com.codenvy.plugin.gitlab.factory.resolver;

/**
 * Interface for Gitlab repository URL parsers.
 *
 * @author Max Shaposhnik
 */
public interface GitlabURLParser {

    /**
     * Check if the URL is a valid gitlab url for the given provider.
     *
     * @param url
     *         a not null string representation of URL
     * @return {@code true} if the URL is a valid url for the given provider.
     */
    boolean isValid(String url);

    /**
     * Provides a parsed URL object of the given provider type.
     *
     * @param url
     *         URL to transform into a managed object
     * @return managed url object
     */
    GitlabUrl parse(String url);
}
