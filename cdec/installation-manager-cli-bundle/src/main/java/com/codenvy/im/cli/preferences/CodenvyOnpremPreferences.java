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
package com.codenvy.im.cli.preferences;

import com.codenvy.cli.command.builtin.MultiRemoteCodenvy;
import com.codenvy.cli.command.builtin.Remote;
import com.codenvy.cli.preferences.Preferences;
import com.codenvy.cli.security.RemoteCredentials;
import org.apache.commons.lang.StringUtils;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * @author Dmytro Nochevnov
 */
public class CodenvyOnpremPreferences {
    public static final String CODENVY_ONPREM_REMOTE_NAME = "codenvy-onprem";

    private final Preferences globalPreferences;
    private final String remote = CODENVY_ONPREM_REMOTE_NAME;

    private       MultiRemoteCodenvy storage;

    public CodenvyOnpremPreferences(Preferences globalPreferences, MultiRemoteCodenvy storage) {
        this.globalPreferences = globalPreferences;
        this.storage = storage;
    }

    /**
     * Ensure Codenvy on-prem preferences has url = codenvyOnpremUrl.
     **/
    public void setUrl(String codenvyOnpremUrl) {
        Remote remote = storage.getRemote(CodenvyOnpremPreferences.CODENVY_ONPREM_REMOTE_NAME);
        if (remote != null) {
            remote.setUrl(codenvyOnpremUrl);
            globalPreferences.path("remotes").put(CodenvyOnpremPreferences.CODENVY_ONPREM_REMOTE_NAME, remote);
            return;
        }

        storage.addRemote(CodenvyOnpremPreferences.CODENVY_ONPREM_REMOTE_NAME, codenvyOnpremUrl);
    }

    /**
     * @return auth token of Codenvy on-prem stored earler after login.
     * @throws PreferenceNotFoundException
     */
    public String getAuthToken() throws PreferenceNotFoundException {
        RemoteCredentials credentials = readPreference(RemoteCredentials.class);
        if (credentials == null || StringUtils.isEmpty(credentials.getToken())) {
            throw new PreferenceNotFoundException("Auth token of Codenvy onprem not found");
        }

        return credentials.getToken();
    }

    /**
     * @return auth token which is obtained after
     * @throws PreferenceNotFoundException
     */
    public String getUrl() throws PreferenceNotFoundException {
        Remote remote = storage.getRemote(CodenvyOnpremPreferences.CODENVY_ONPREM_REMOTE_NAME);
        if (remote == null || StringUtils.isEmpty(remote.getUrl())) {
            throw new PreferenceNotFoundException("Url of Codenvy onprem not found");
        }

        return remote.getUrl();
    }

    /**
     * @return name of user which logged into Codenvy on-prem
     * @throws PreferenceNotFoundException
     */
    public String getUsername() throws PreferenceNotFoundException {
        RemoteCredentials credentials = readPreference(RemoteCredentials.class);
        if (credentials == null || StringUtils.isEmpty(credentials.getUsername())) {
            throw new PreferenceNotFoundException("Name of user which logged into Codenvy onprem not found");
        }

        return credentials.getUsername();
    }

    @Nullable
    private <T> T readPreference(Class<T> clazz) {
        return globalPreferences.path("remotes").get(remote, clazz);
    }

    private <T> void writePreference(T preference) {
        globalPreferences.path("remotes").merge(remote, preference);
    }
}
