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
package com.codenvy.ldap;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.multibindings.OptionalBinder;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;

public class DefaultPropertiesModule extends AbstractModule {
    @Override
    protected void configure() {
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.auth.dn_format")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.auth.user_password_attribute")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.auth.user.filter")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.auth.allow_multiple_dns")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.auth.subtree_search")))
                      .setDefault().toProvider(Providers.of(null));

        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.provider")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.bind.dn")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.bind.password")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.use_ssl")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.use_start_tls")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.pool.min_size")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.pool.max_size")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.pool.validate.on_checkout")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.pool.validate.on_checkin")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.pool.validate.period_ms")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.pool.validate.periodically")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.pool.fail_fast")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.pool.idle_ms")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.pool.prune_ms")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.pool.block_wait_ms")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.connect_timeout_ms")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.response_timeout_ms")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.ssl.trust_certificates")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.ssl.keystore.name")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.ssl.keystore.password")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.ssl.keystore.type")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.sasl.realm")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.sasl.mechanism")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.sasl.authorization_id")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.sasl.security_strength")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.sasl.mutual_auth")))
                      .setDefault().toProvider(Providers.of(null));
        OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.connection.sasl.quality_of_protection")))
                      .setDefault().toProvider(Providers.of(null));


    }
}
