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
package com.codenvy.im.managers;

import com.codenvy.im.utils.Version;
import com.google.common.collect.ImmutableList;

import com.google.common.collect.ImmutableMap;
import org.eclipse.che.commons.annotation.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codenvy.im.utils.OSUtils.getVersion;
import static com.codenvy.im.utils.Version.valueOf;
import static java.util.Collections.unmodifiableMap;

/** @author Dmytro Nochevnov */
public class Config {
    public static final String PUPPET_CONF_FILE_NAME = "puppet.conf";

    public static final String SINGLE_SERVER_PP             = "manifests/nodes/single_server/single_server.pp";
    public static final String SINGLE_SERVER_BASE_CONFIG_PP = "manifests/nodes/single_server/base_config.pp";

    public static final String MULTI_SERVER_CUSTOM_CONFIG_PP = "manifests/nodes/multi_server/custom_configurations.pp";
    public static final String MULTI_SERVER_BASE_CONFIG_PP   = "manifests/nodes/multi_server/base_configurations.pp";
    public static final String MULTI_SERVER_NODES_PP         = "manifests/nodes/multi_server/nodes.pp";

    public static final String SINGLE_SERVER_4_0_PROPERTIES     = "manifests/nodes/codenvy/codenvy.pp";
    public static final String MULTI_SERVER_4_0_PROPERTIES      = "manifests/nodes/codenvy_multi/codenvy.pp";
    public static final String MULTI_SERVER_BASE_4_0_PROPERTIES = "manifests/nodes/codenvy_multi/base.pp";

    public static final Pattern ENCLOSED_PROPERTY_NAME_PATTERN =  Pattern.compile("\\$\\w+");

    public static final String VERSION = "version";

    public static final String PUPPET_AGENT_PACKAGE  = "puppet_agent_package";
    public static final String PUPPET_SERVER_PACKAGE = "puppet_server_package";
    public static final String PUPPET_RESOURCE_URL   = "puppet_resource_url";

    public static final String AIO_HOST_URL              = "aio_host_url"; // 3.1.0  TODO [ndp] remove outdated property
    public static final String HOST_URL                  = "host_url";
    public static final String NODE_HOST_PROPERTY_SUFFIX = "_host_name";  // suffix of property like "builder_host_name"

    public static final String PUPPET_MASTER_HOST_NAME = "puppet_master_host_name";

    public static final String MONGO_ADMIN_USERNAME      = "mongo_admin_user_name";
    public static final String MONGO_ADMIN_PASSWORD      = "mongo_admin_pass";
    public static final String NODE_SSH_USER_NAME        = "node_ssh_user_name";
    public static final String NODE_SSH_USER_PRIVATE_KEY = "node_ssh_user_private_key";

    /* ldap properties */
    public static final String SYSTEM_LDAP_USER_BASE     = "system_ldap_user_base";

    public static final String ADMIN_LDAP_DN        = "admin_ldap_dn";
    public static final String ADMIN_LDAP_USER_NAME = "admin_ldap_user_name";
    public static final String ADMIN_LDAP_PASSWORD  = "admin_ldap_password";
    public static final String ADMIN_LDAP_MAIL      = "admin_ldap_mail";

    public static final String USER_LDAP_USER_CONTAINER_DN = "user_ldap_user_container_dn";
    public static final String USER_LDAP_OBJECT_CLASSES    = "user_ldap_object_classes";
    public static final String USER_LDAP_DN                = "user_ldap_dn";
    public static final String USER_LDAP_PASSWORD          = "user_ldap_password";
    public static final String USER_LDAP_USERS_OU          = "user_ldap_users_ou";
    public static final String USER_LDAP_USER_DN           = "user_ldap_user_dn";

    public static final String LDAP_HOST     = "ldap_host";
    public static final String LDAP_PORT     = "ldap_port";
    public static final String LDAP_PROTOCOL = "ldap_protocol";


    public static final String JAVA_NAMING_SECURITY_AUTHENTICATION = "java_naming_security_authentication";
    public static final String JAVA_NAMING_SECURITY_PRINCIPAL      = "java_naming_security_principal";

    public static final String PUBLIC_KEY  = "public_key";
    public static final String PRIVATE_KEY = "private_key";

    public static final String ADDITIONAL_BUILDERS = "additional_builders";
    public static final String ADDITIONAL_RUNNERS  = "additional_runners";

    public static final String SWARM_NODES = "swarm_nodes";

    public static final String MACHINE_EXTRA_HOSTS = "machine_extra_hosts";

    public static final String CODENVY_INSTALL_TYPE = "codenvy_install_type";

    public static final String SYSTEM_HTTP_PROXY  = "http_proxy";
    public static final String SYSTEM_HTTPS_PROXY = "https_proxy";
    public static final String SYSTEM_NO_PROXY    = "che.workspace.no_proxy";

    public static final String HTTP_PROXY_FOR_CODENVY  = "http_proxy_for_codenvy";
    public static final String HTTPS_PROXY_FOR_CODENVY = "https_proxy_for_codenvy";
    public static final String NO_PROXY_FOR_CODENVY    = "no_proxy_for_codenvy";

    public static final String PGSQL_DATABASE_NAME = "pgsql_database_name";

    public static final List<String> MULTI_SERVER_PROPERTIES = ImmutableList.of(MULTI_SERVER_BASE_CONFIG_PP,
                                                                                MULTI_SERVER_4_0_PROPERTIES,
                                                                                MULTI_SERVER_BASE_4_0_PROPERTIES,
                                                                                MULTI_SERVER_CUSTOM_CONFIG_PP);

    public static final List<String> SINGLE_SERVER_PROPERTIES = ImmutableList.of(SINGLE_SERVER_BASE_CONFIG_PP,
                                                                                 SINGLE_SERVER_4_0_PROPERTIES,
                                                                                 SINGLE_SERVER_PP);

    public static final Map<String, Map<String, String>> PROPERTIES_BY_VERSION = ImmutableMap.of(
        PUPPET_AGENT_PACKAGE, ImmutableMap.of("7", "puppet-3.8.6-1.el7.noarch"),
        PUPPET_SERVER_PACKAGE, ImmutableMap.of("7", "puppet-server-3.8.6-1.el7.noarch"),
        PUPPET_RESOURCE_URL, ImmutableMap.of("7", "https://yum.puppetlabs.com/el/7/products/x86_64/puppetlabs-release-7-11.noarch.rpm")
    );

    public static final Set<String> PROPERTIES_DEPEND_ON_VERSION = PROPERTIES_BY_VERSION.keySet();

    private Map<String, String> properties;

    public Config(Map<String, String> properties) {
        this.properties = Collections.unmodifiableMap(properties);
    }

    /**
     * Recursively substitute properties which are enclosed into the requesting value.
     * For example, value "ou=$user_ldap_users_ou,$user_ldap_dn" has enclosed properties '$user_ldap_users_ou', '$user_ldap_dn'.
     * Algorithm doesn't take into account:
     * - qualified variable names,
     * - names like ${foo}.
     *
     * Algorithm doesn't substitute cyclic property links.
     */
    @Nullable
    public String getValue(String property) {
        return getValue(property, new ArrayList<>());
    }

    /**
     * @return original value without substitution enclosed variable, for example, "ou=$user_ldap_users_ou,$user_ldap_dn".
     */
    @Nullable
    public String getValueWithoutSubstitution(String property) {
        if (PROPERTIES_DEPEND_ON_VERSION.contains(property)) {
            return PROPERTIES_BY_VERSION.get(property).get(getVersion());
        }

        return properties.get(property);
    }

    @Nullable
    private String getValue(String property, List<String> usedProperties) {
        if (usedProperties.contains(property)) {
            return null;
        }

        usedProperties.add(property);

        String value;
        if (PROPERTIES_DEPEND_ON_VERSION.contains(property)) {
            value = PROPERTIES_BY_VERSION.get(property).get(getVersion());
        } else {
            value = properties.get(property);
        }

        if (value != null) {
            Matcher m = ENCLOSED_PROPERTY_NAME_PATTERN.matcher(value);
            while (m.find()) {
                String enclosedPropertyName = m.group();
                String enclosedPropertyValue = getValue(enclosedPropertyName.replace("$", ""), usedProperties);

                if (enclosedPropertyValue != null) {
                    value = value.replace(enclosedPropertyName, enclosedPropertyValue);
                }
            }
        }

        return value;
    }

    /** @return list of property values separated by delimiter. Substitute enclosed variables like $host_url */
    @Nullable
    public List<String> getAllValues(String property, String delimiter) {
        return getAllValues(property, delimiter, true);
    }

    /** @return list of property values separated by delimiter. Don't substitute variables like $host_url */
    @Nullable
    public List<String> getAllValuesWithoutSubstitution(String property, String delimiter) {
        return getAllValues(property, delimiter, false);
    }

    /** @return list of property values separated by delimiter. Substitute enclosed variables like $host_url if "withSubstitution" parameter == true */
    @Nullable
    private List<String> getAllValues(String property, String delimiter, boolean withSubstitution) {
        property = property.toLowerCase();
        String value;
        if (PROPERTIES_DEPEND_ON_VERSION.contains(property)) {
            value = PROPERTIES_BY_VERSION.get(property).get(getVersion());
        } else {
            value = (withSubstitution) ? getValue(property) : getValueWithoutSubstitution(property);
        }

        List<String> result = new LinkedList<>();
        if (value == null) {
            return null;
        }

        for (String item : value.split(delimiter)) {
            item = item.trim();
            if (!item.isEmpty()) {
                result.add(item);
            }
        }

        return result;
    }

    /** Getter for #properties. Unmodifiable map will be returned */
    public Map<String, String> getProperties() {
        return unmodifiableMap(properties);
    }

    /** @return the either #HOST_URL or #AIO_HOST_URL property value */
    public String getHostUrl() {
        return properties.containsKey(HOST_URL) ? properties.get(HOST_URL) : properties.get(AIO_HOST_URL);
    }

    /**
     * @return path to directory with generic codenvy configuration files
     */
    public Path getPathToCodenvyConfigDir() {
        Version codenvyVersion = valueOf(getValue(VERSION));
        if (codenvyVersion.compareTo(valueOf("4.3.0-RC1-SNAPSHOT")) < 0) {
            return Paths.get("/home/codenvy/codenvy-data/cloud-ide-local-configuration");
        } else {
            return Paths.get("/home/codenvy/codenvy-data/conf");
        }
    }
}
