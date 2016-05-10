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
package com.codenvy.im.artifacts.helper;

import java.util.Objects;

/**
 * Class to obtain and hold http proxy system settings and https proxy system settings.
 * @author Dmytro Nochevnov
 */
public class SystemProxySettings {
    private String httpUser;
    private String httpPassword;
    private String httpHost;
    private String httpPort;

    private String httpsUser;
    private String httpsPassword;
    private String httpsHost;
    private String httpsPort;

    /**
     * Read proxy settings from the System properties and enclose it into the ProxySettings object.
     * @return system proxy settings.
     */
    public static SystemProxySettings create() {
        final SystemProxySettings proxySettings = new SystemProxySettings();
        proxySettings.httpUser = System.getProperty("http.proxyUser");
        proxySettings.httpPassword = System.getProperty("http.proxyPassword");
        proxySettings.httpHost = System.getProperty("http.proxyHost");
        proxySettings.httpPort = System.getProperty("http.proxyPort");

        proxySettings.httpsUser = System.getProperty("https.proxyUser");
        proxySettings.httpsPassword = System.getProperty("https.proxyPassword");
        proxySettings.httpsHost = System.getProperty("https.proxyHost");
        proxySettings.httpsPort = System.getProperty("https.proxyPort");
        
        return proxySettings;
    }

    /**
     * @return true if only all proxy settings is null.
     */
    public boolean isEmpty() {
        return Objects.isNull(httpUser)
               && Objects.isNull(httpPassword)
               && Objects.isNull(httpHost)
               && Objects.isNull(httpPort)
               && Objects.isNull(httpsUser)
               && Objects.isNull(httpsPassword)
               && Objects.isNull(httpsHost)
               && Objects.isNull(httpsPort);
    }

    public String getHttpUser() {
        return httpUser;
    }

    public String getHttpPassword() {
        return httpPassword;
    }

    public String getHttpHost() {
        return httpHost;
    }

    public String getHttpPort() {
        return httpPort;
    }

    public String getHttpsUser() {
        return httpsUser;
    }

    public String getHttpsPassword() {
        return httpsPassword;
    }

    public String getHttpsHost() {
        return httpsHost;
    }

    public String getHttpsPort() {
        return httpsPort;
    }
}
