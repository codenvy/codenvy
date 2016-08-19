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
package com.codenvy.api.permission.server;

import com.codenvy.api.permission.server.dao.CommonDomains;
import com.codenvy.api.permission.server.dao.CommonPermissionsDao;
import com.codenvy.api.permission.server.dao.PermissionsImplCodec;
import com.codenvy.api.permission.server.filter.GetPermissionsFilter;
import com.codenvy.api.permission.server.filter.RemovePermissionsFilter;
import com.codenvy.api.permission.server.filter.SetPermissionsFilter;
import com.codenvy.api.permission.server.model.impl.AbstractPermissions;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

/**
 * @author Sergii Leschenko
 */
public class PermissionsModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PermissionsService.class);
        bind(SetPermissionsFilter.class);
        bind(RemovePermissionsFilter.class);
        bind(GetPermissionsFilter.class);

        //Creates empty multibinder to avoid error during container starting
        Multibinder.newSetBinder(binder(),
                                 String.class,
                                 Names.named(SystemDomain.SYSTEM_DOMAIN_ACTIONS));

        final Multibinder<AbstractPermissionsDomain> permissionsDomainMultibinder = Multibinder.newSetBinder(binder(),
                                                                                                             AbstractPermissionsDomain.class,
                                                                                                             CommonDomains.class);
        permissionsDomainMultibinder.addBinding().to(SystemDomain.class);

        Multibinder<PermissionsDao> storages = Multibinder.newSetBinder(binder(),
                                                                            PermissionsDao.class);
        storages.addBinding().to(CommonPermissionsDao.class);

        final Multibinder<CodecProvider> binder = Multibinder.newSetBinder(binder(), CodecProvider.class);
        binder.addBinding().toInstance(new CodecProvider() {
            @Override
            public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
                if (clazz == AbstractPermissions.class) {
                    @SuppressWarnings("unchecked")
                    final Codec<T> codec = (Codec<T>)new PermissionsImplCodec(registry);
                    return codec;
                }
                return null;
            }
        });
    }
}
