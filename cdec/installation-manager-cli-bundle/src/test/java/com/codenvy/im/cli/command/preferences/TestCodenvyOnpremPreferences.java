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
package com.codenvy.im.cli.command.preferences;

import com.codenvy.cli.command.builtin.Remote;
import com.codenvy.cli.preferences.Preferences;
import com.codenvy.cli.preferences.PreferencesAPI;
import com.codenvy.im.cli.command.AbstractTestCommand;
import com.codenvy.im.cli.preferences.CodenvyOnpremPreferences;
import com.codenvy.im.cli.preferences.PreferenceNotFoundException;
import com.google.common.io.Files;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNull;

/** @author Dmytro Nochevnov */
public class TestCodenvyOnpremPreferences extends AbstractTestCommand {

    private Preferences spyGlobalPreferences;

    public final static String DEFAULT_PREFERENCES                           = "default-preferences.json";
    public final static String PREFERENCES_WITH_CODENVY_ONPREM               = "preferences-with-codenvy-onprem.json";
    public final static String PREFERENCES_WITH_CODENVY_ONPREM_WITHOUT_LOGIN = "preferences-with-codenvy-onprem-without-login.json";

    @Mock
    Remote mockRemote;

    @BeforeMethod
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    private void testGetAuthToken() throws PreferenceNotFoundException {
        spyGlobalPreferences = loadPreferences(PREFERENCES_WITH_CODENVY_ONPREM);
        CodenvyOnpremPreferences codenvyOnpremPreferences = new CodenvyOnpremPreferences(spyGlobalPreferences, mockMultiRemoteCodenvy);

        assertEquals(codenvyOnpremPreferences.getAuthToken(), CODENVY_ONPREM_AUTH_TOKEN);
    }

    @Test(expectedExceptions = PreferenceNotFoundException.class,
          expectedExceptionsMessageRegExp = "Auth token of Codenvy onprem not found")
    private void testGetAbsentAuthToken() throws PreferenceNotFoundException {
        spyGlobalPreferences = loadPreferences(PREFERENCES_WITH_CODENVY_ONPREM_WITHOUT_LOGIN);
        CodenvyOnpremPreferences codenvyOnpremPreferences = new CodenvyOnpremPreferences(spyGlobalPreferences, mockMultiRemoteCodenvy);
        codenvyOnpremPreferences.getAuthToken();
    }

    @Test(expectedExceptions = PreferenceNotFoundException.class,
        expectedExceptionsMessageRegExp = "Auth token of Codenvy onprem not found")
    private void testGetAuthTokenWhenAbsentRemote() throws PreferenceNotFoundException {
        spyGlobalPreferences = loadPreferences(DEFAULT_PREFERENCES);
        CodenvyOnpremPreferences codenvyOnpremPreferences = new CodenvyOnpremPreferences(spyGlobalPreferences, mockMultiRemoteCodenvy);
        codenvyOnpremPreferences.getAuthToken();
    }
    @Test
    private void testGetUsername() throws PreferenceNotFoundException {
        spyGlobalPreferences = loadPreferences(PREFERENCES_WITH_CODENVY_ONPREM);
        CodenvyOnpremPreferences codenvyOnpremPreferences = new CodenvyOnpremPreferences(spyGlobalPreferences, mockMultiRemoteCodenvy);
        assertEquals(codenvyOnpremPreferences.getUsername(), CODENVY_ONPREM_USER);
    }

    @Test(expectedExceptions = PreferenceNotFoundException.class,
        expectedExceptionsMessageRegExp = "Name of user which logged into Codenvy onprem not found")
    private void testGetAbsentUsername() throws PreferenceNotFoundException {
        spyGlobalPreferences = loadPreferences(PREFERENCES_WITH_CODENVY_ONPREM_WITHOUT_LOGIN);
        CodenvyOnpremPreferences codenvyOnpremPreferences = new CodenvyOnpremPreferences(spyGlobalPreferences, mockMultiRemoteCodenvy);
        codenvyOnpremPreferences.getUsername();
    }

    @Test(expectedExceptions = PreferenceNotFoundException.class,
        expectedExceptionsMessageRegExp = "Name of user which logged into Codenvy onprem not found")
    private void testGetUsernameWhenAbsentRemote() throws PreferenceNotFoundException {
        spyGlobalPreferences = loadPreferences(DEFAULT_PREFERENCES);
        CodenvyOnpremPreferences codenvyOnpremPreferences = new CodenvyOnpremPreferences(spyGlobalPreferences, mockMultiRemoteCodenvy);
        codenvyOnpremPreferences.getUsername();
    }

    @Test
    private void testGetUrl() throws PreferenceNotFoundException {
        doReturn(mockRemote).when(mockMultiRemoteCodenvy).getRemote(CodenvyOnpremPreferences.CODENVY_ONPREM_REMOTE_NAME);
        doReturn(CODENVY_ONPREM_URL).when(mockRemote).getUrl();

        CodenvyOnpremPreferences codenvyOnpremPreferences = new CodenvyOnpremPreferences(spyGlobalPreferences, mockMultiRemoteCodenvy);
        assertEquals(codenvyOnpremPreferences.getUrl(), CODENVY_ONPREM_URL);
    }

    @Test(expectedExceptions = PreferenceNotFoundException.class,
        expectedExceptionsMessageRegExp = "Url of Codenvy onprem not found")
    private void testGetAbsentUrl() throws PreferenceNotFoundException {
        doReturn(null).when(mockMultiRemoteCodenvy).getRemote(CodenvyOnpremPreferences.CODENVY_ONPREM_REMOTE_NAME);
        CodenvyOnpremPreferences codenvyOnpremPreferences = new CodenvyOnpremPreferences(spyGlobalPreferences, mockMultiRemoteCodenvy);
        codenvyOnpremPreferences.getUrl();
    }

    @Test
    public void testSetNewUrl() {
        doReturn(null).when(mockMultiRemoteCodenvy).getRemote(CodenvyOnpremPreferences.CODENVY_ONPREM_REMOTE_NAME);
        CodenvyOnpremPreferences codenvyOnpremPreferences = new CodenvyOnpremPreferences(spyGlobalPreferences, mockMultiRemoteCodenvy);

        codenvyOnpremPreferences.setUrl(CODENVY_ONPREM_URL);

        verify(mockMultiRemoteCodenvy).addRemote(CodenvyOnpremPreferences.CODENVY_ONPREM_REMOTE_NAME, CODENVY_ONPREM_URL);
    }

    @Test
    public void testUpdateExistedUrl() throws PreferenceNotFoundException {
        doReturn(mockRemote).when(mockMultiRemoteCodenvy).getRemote(CodenvyOnpremPreferences.CODENVY_ONPREM_REMOTE_NAME);
        doReturn(mockRemote).when(mockMultiRemoteCodenvy).getRemote(CodenvyOnpremPreferences.CODENVY_ONPREM_REMOTE_NAME);

        Preferences mockPreferences = mock(Preferences.class);
        doReturn(mockPreferences).when(spyGlobalPreferences).path("remotes");

        CodenvyOnpremPreferences codenvyOnpremPreferences = new CodenvyOnpremPreferences(spyGlobalPreferences, mockMultiRemoteCodenvy);
        codenvyOnpremPreferences.setUrl(CODENVY_ONPREM_URL);

        verify(mockRemote).setUrl(CODENVY_ONPREM_URL);
        verify(mockPreferences).put(CodenvyOnpremPreferences.CODENVY_ONPREM_REMOTE_NAME, mockRemote);
        verify(mockMultiRemoteCodenvy, never()).addRemote(anyString(), anyString());
    }

    private Preferences loadPreferences(String preferencesFileRelativePath) {
        String preferencesFileFullPath = getClass().getClassLoader().getResource(preferencesFileRelativePath).getPath();
        String tempPreferencesFileFullPath = preferencesFileFullPath + ".temp";
        File preferencesFile = new File(preferencesFileFullPath);
        File tempPreferencesFile = new File(tempPreferencesFileFullPath);

        try {
            Files.copy(preferencesFile, tempPreferencesFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return spy(PreferencesAPI.getPreferences(tempPreferencesFile.toURI()));
    }
}
