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
package com.codenvy.api.dao.ldap;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.repository.TckRepository;

import static com.codenvy.api.dao.ldap.LdapEmbeddedServerListener.LDAP_SERVER_URL_ATTRIBUTE_NAME;

/**
 * @author Yevhenii Voevodin
 */
public class LdapTckModule extends TckModule {

    @Override
    public void configure() {
        final InitialLdapContextFactory contextFactory =
                new InitialLdapContextFactory(() -> getTestContext().getAttribute(LDAP_SERVER_URL_ATTRIBUTE_NAME).toString(),
                                              null,
                                              null,
                                              null,
                                              null,
                                              null,
                                              null,
                                              null,
                                              null);
        bind(InitialLdapContextFactory.class).toInstance(contextFactory);

        bind(UserAttributesMapper.class).toInstance(new UserAttributesMapper());

        @SuppressWarnings("unchecked") // array contains only strings
        final Pair<String, String>[] allowedAttributes = new Pair[3];
        allowedAttributes[0] = Pair.of("givenName", "firstName");
        allowedAttributes[1] = Pair.of("sn", "lastName");
        allowedAttributes[2] = Pair.of("o", "company");
        bind(ProfileAttributesMapper.class).toInstance(new ProfileAttributesMapper("dc=codenvy;dc=com",
                                                                                   "uid",
                                                                                   "uid",
                                                                                   allowedAttributes));

        bind(new TypeLiteral<TckRepository<UserImpl>>() {}).to(UserTckRepository.class);
        bind(new TypeLiteral<TckRepository<ProfileImpl>>() {}).to(ProfileTckRepository.class);

        bindConstant().annotatedWith(Names.named("user.ldap.user_dn")).to("uid");
        bindConstant().annotatedWith(Names.named("user.ldap.old_user_dn")).to("cn");
        bindConstant().annotatedWith(Names.named("user.ldap.user_container_dn")).to("dc=codenvy;dc=com");

        bind(UserDao.class).to(LdapUserDao.class);
        bind(ProfileDao.class).to(LdapProfileDao.class);
    }
}
