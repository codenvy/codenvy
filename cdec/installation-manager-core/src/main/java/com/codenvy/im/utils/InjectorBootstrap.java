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
package com.codenvy.im.utils;

import com.codenvy.im.artifacts.Artifact;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.artifacts.InstallManagerArtifact;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.commons.annotation.Nullable;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.newDirectoryStream;
import static java.nio.file.Files.newInputStream;

/**
 * @author Anatoliy Bazko
 */
public class InjectorBootstrap {

    public static final  Injector            INJECTOR;
    protected static final Map<String, String> boundProperties;

    // ${...}
    private static final Pattern envPattern = Pattern.compile("\\$\\{([^\\}]*)\\}");

    public static final String INSTALLATION_MANAGER_BASE_DIR = "installation-manager.base_dir";

    static {
        boundProperties = new HashMap<>();
        INJECTOR = createInjector();
    }

    public static Injector createInjector() {
        return Guice.createInjector(new Module() {
            @Override
            public void configure(Binder binder) {
                InjectorBootstrap.bindAllProperties(binder);

                Multibinder.newSetBinder(binder, Artifact.class).addBinding().to(InstallManagerArtifact.class);
                Multibinder.newSetBinder(binder, Artifact.class).addBinding().to(CDECArtifact.class);
            }
        });
    }

    @Nullable
    public static String getProperty(String name) {
        return boundProperties.get(name);
    }

    private static void bindAllProperties(Binder binder) {
        bindFileProperties("codenvy/update-server.properties");
        bindFileProperties("codenvy/installation-manager.properties");
        bindFileProperties("codenvy/installation-manager-server.properties");
        bindFileProperties("codenvy/report.properties");

        String confDir = System.getenv("CHE_LOCAL_CONF_DIR");
        if (confDir != null) {
            overrideDefaultProperties(confDir);
        }

        for (Map.Entry<String, String> e : boundProperties.entrySet()) {
            binder.bindConstant().annotatedWith(Names.named(e.getKey())).to(e.getValue());
        }
    }

    private static void bindFileProperties(String fileProperties) {
        try (InputStream in = InjectorBootstrap.class.getClassLoader().getResourceAsStream(fileProperties)) {
            if (in != null) {
                doBindFileProperties(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Can't load properties", e);
        }
    }

    private static void doBindFileProperties(InputStream in) {
        Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Can't load properties", e);
        }

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String)entry.getKey();
            String value = replaceEnvVariables((String)entry.getValue());

            boundProperties.put(key, value);
        }
    }

    /** Replaces environment variables by them actual values using ${...} template. */
    private static String replaceEnvVariables(String value) {
        Matcher matcher = envPattern.matcher(value);
        if (matcher.find()) {
            String envVar = matcher.group(1);
            return value.substring(0, matcher.start(1) - 2) + System.getenv(envVar) + value.substring(matcher.end(1) + 1);
        } else {
            return value;
        }
    }

    /** Override default properties from jar with values from CHE_LOCAL_CONF_DIR directory */
    protected static void overrideDefaultProperties(@NotNull String localConfDir) {
        Path confDirPath = Paths.get(localConfDir);

        if (exists(confDirPath)) {
            try (DirectoryStream<Path> stream = newDirectoryStream(confDirPath)) {
                for (Path entry : stream) {
                    if (!Files.isDirectory(entry)) {
                        if (entry.toString().endsWith(".properties")) {
                            try (InputStream in = newInputStream(entry)) {
                                doBindFileProperties(in);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Can't load properties", e);
            }
        }
    }
}
