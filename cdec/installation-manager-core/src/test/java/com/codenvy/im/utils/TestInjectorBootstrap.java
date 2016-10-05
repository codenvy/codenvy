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
package com.codenvy.im.utils;

import com.codenvy.im.BaseTest;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** @author Dmytro Nochevnov */
public class TestInjectorBootstrap {
    public static Map<String, String> ORIGINAL_BOUND_PROPERTIES;
    public static final Injector injector = InjectorBootstrap.INJECTOR;

    @BeforeTest
    public void setup() {
        ORIGINAL_BOUND_PROPERTIES = new HashMap<>(InjectorBootstrap.boundProperties);
    }

    @AfterMethod
    public void restoreBoundProperties() {
        InjectorBootstrap.boundProperties.clear();
        InjectorBootstrap.boundProperties.putAll(ORIGINAL_BOUND_PROPERTIES);
    }

    @Test
    public void testInjector() {
        Injector injector = InjectorBootstrap.INJECTOR;
        Map<Key<?>, Binding<?>> bindings = injector.getBindings();
        assertTrue(bindings.size() >= 19, "Actual bindings: " + bindings.toString());
    }

    @Test(dataProvider = "testProperties")
    public void testBindProperties(String name, String value) {
        assertEquals(injector.getInstance(Key.get(String.class, Names.named(name))), value);
    }

    @DataProvider(name = "testProperties")
    public Object[][] getTestProperties() {
        return new Object[][]{
            {"installation-manager.base_dir", BaseTest.BASE_DIR},
            {"installation-manager.download_dir", BaseTest.DOWNLOAD_DIR},
            {"installation-manager.backup_dir", "target/backups"},
            {"installation-manager.storage_dir", "target/storage"},
            {"installation-manager.report_dir", "target/reports"},
            {"installation-manager.update_server_endpoint", BaseTest.UPDATE_API_ENDPOINT},
            {"installation-manager.min_puppet_errors_to_interrupt_im", "3"},
            {"che.api", BaseTest.API_ENDPOINT},
            {"saas.api.endpoint", BaseTest.SAAS_API_ENDPOINT},
            {"puppet.base_dir", "target/puppet"},
            {"os.redhat_release_file", "target/redhat-release"}
        };
    }

    @Test
    public void testOverrideDefaultProperties() {
        String newPropertiesDirectory = getClass().getClassLoader().getResource("codenvy/test-override-properties").getPath();
        InjectorBootstrap.overrideDefaultProperties(newPropertiesDirectory);

        assertEquals(InjectorBootstrap.boundProperties.get("installation-manager.download_dir"), "target/updates");
        assertEquals(InjectorBootstrap.boundProperties.get("installation-manager.update_server_endpoint"), "http://update2.endpoint");
        assertEquals(InjectorBootstrap.boundProperties.get("installation-manager.report_dir"),
                     format("%s/codenvy/reports", System.getProperty("user.home"))); // test InjectorBootstrap...replaceEnvVariables() method
    }

}
