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

import com.codenvy.api.dao.authentication.PasswordEncryptor;
import com.codenvy.api.dao.authentication.SSHAPasswordEncryptor;

import org.eclipse.che.api.user.server.model.impl.UserImpl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static javax.naming.directory.DirContext.REPLACE_ATTRIBUTE;

/**
 * Mapper is used for mapping LDAP Attributes to/from {@code User} POJO.
 *
 * @author andrew00x
 * @author Eugene Voevodin
 */
@Singleton
public class UserAttributesMapper {

    final PasswordEncryptor encryptor;
    final String[]          userObjectClasses;
    final String            userNameAttr;
    final String            userIdAttr;
    final String            userPasswordAttr;
    final String            userEmailAttr;
    final String            userAliasesAttr;

    /**
     * Basically for representing user in LDAP 'person', 'organizationalPerson' or 'inetOrgPerson' are used. Some of
     * attributes may be
     * required by LDAP schemas but we may not have some of them in {@code User} abstraction. This attributes may
     * have pre-configured with
     * default values.
     */
    private final Map<String, String> requiredAttributes;

    /**
     * Creates new instance of UserAttributesMapper.
     *
     * @param encryptor
     *         encryptor of user passwords
     * @param userObjectClasses
     *         values for objectClass attribute. Typical value is 'inetOrgPerson'.
     * @param userNameAttr
     *         name of attribute that contains name of User object. Typical value is 'CN'.
     *         <p/>
     *         Example:
     *         Imagine this attribute is set to typical name 'CN' and full name of parent object for user records is
     *         'dc=codenvy,dc=com'
     *         then full name to user record is 'CN=my_user,dc=codenvy,dc=com'.
     * @param userIdAttr
     *         name of attribute that contains ID of User object. Typical value is 'uid'.
     * @param userPasswordAttr
     *         name of attribute that contains password. Typical value is 'userPassword'.
     * @param userEmailAttr
     *         name of attribute that contains email address. Typical value is 'mail'.
     * @param userAliasesAttr
     *         name of attribute that contains emails associated with user in our implementation.
     *         Typical value is 'initials'.
     */
    @Inject
    public UserAttributesMapper(PasswordEncryptor encryptor,
                                @Named("user.ldap.object_classes") String[] userObjectClasses,
                                @Named("user.ldap.attr.id") String userIdAttr,
                                @Named("user.ldap.attr.name") String userNameAttr,
                                @Named("user.ldap.attr.password") String userPasswordAttr,
                                @Named("user.ldap.attr.email") String userEmailAttr,
                                @Named("user.ldap.attr.aliases") String userAliasesAttr) {
        this.encryptor = encryptor;
        this.userObjectClasses = userObjectClasses;
        this.userNameAttr = userNameAttr;
        this.userIdAttr = userIdAttr;
        this.userPasswordAttr = userPasswordAttr;
        this.userEmailAttr = userEmailAttr;
        this.userAliasesAttr = userAliasesAttr;
        requiredAttributes = new LinkedHashMap<>();
        addRequiredAttributesTo(userObjectClasses, requiredAttributes);
    }

    public UserAttributesMapper() {
        this(new SSHAPasswordEncryptor(), new String[] {"inetOrgPerson"}, "uid", "cn", "userPassword", "mail",
             "initials");
    }

    /**
     * Add mapping for required attributes. Such attributes are required by LDAP schema but may not be obtained directly or indirectly from
     * {@code User} instance. Such attributes will be added as in newly created user record in LDAP server.
     */
    protected void addRequiredAttributesTo(String[] objectClasses, Map<String, String> requiredAttributes) {
        for (String objectClass : objectClasses) {
            if ("inetOrgPerson".equalsIgnoreCase(objectClass)
                || "organizationalPerson".equalsIgnoreCase(objectClass)
                || "person".equalsIgnoreCase(objectClass)) {
                requiredAttributes.put("sn", "<none>");
                break;
            }
        }
    }

    /**
     * Restores instance of {@code User} from LDAP {@code Attributes}.
     */
    public UserImpl fromAttributes(Attributes attributes) throws NamingException {
        final UserImpl user = new UserImpl(attributes.get(userIdAttr).get().toString(),
                                           attributes.get(userEmailAttr).get().toString(),
                                           attributes.get(userNameAttr).get().toString());
        final NamingEnumeration enumeration = attributes.get(userAliasesAttr).getAll();
        final List<String> aliases = new LinkedList<>();
        try {
            while (enumeration.hasMoreElements()) {
                String value = (String)enumeration.nextElement();
                aliases.add(value);
            }
        } finally {
            enumeration.close();
        }
        user.setAliases(aliases);
        return user;
    }

    /**
     * Converts {@code User} instance to LDAP {@code Attributes}.
     */
    public Attributes toAttributes(UserImpl user) throws NamingException {
        final Attributes attributes = new BasicAttributes();
        final Attribute objectClassAttr = new BasicAttribute("objectClass");
        for (String oc : userObjectClasses) {
            objectClassAttr.add(oc);
        }
        attributes.put(objectClassAttr);
        attributes.put(userIdAttr, user.getId());
        if (user.getName() != null) {
            attributes.put(userNameAttr, user.getName());
        } else {
            attributes.put(userNameAttr, user.getId());
        }
        if (user.getEmail() == null) {
            user.setEmail(user.getName());
        }
        attributes.put(userEmailAttr, user.getEmail());
        attributes.put(userPasswordAttr, new String(encryptor.encrypt(user.getPassword().getBytes())));
        final Attribute aliasesAttr = new BasicAttribute(userAliasesAttr);
        final List<String> aliases = user.getAliases();
        if (!aliases.isEmpty()) {
            for (String alias : aliases) {
                aliasesAttr.add(alias);
            }
        } else {
            aliasesAttr.add(user.getEmail());
        }
        attributes.put(aliasesAttr);
        for (Map.Entry<String, String> e : requiredAttributes.entrySet()) {
            attributes.put(e.getKey(), e.getValue());
        }
        return attributes;
    }

    /**
     * Compares two {@code User} objects and provides diff of {@code ModificationItem[]} form.
     */
    public ModificationItem[] createModifications(UserImpl src, UserImpl dest) throws NamingException {
        final List<ModificationItem> mods = new ArrayList<>();

        // create modification for email if necessary
        if (dest.getEmail() != null && !dest.getEmail().equals(src.getEmail())) {
            mods.add(new ModificationItem(REPLACE_ATTRIBUTE, new BasicAttribute(userEmailAttr, dest.getEmail())));
        }

        // create modification for name if necessary
        if (dest.getName() != null && !dest.getName().equals(src.getName())) {
            mods.add(new ModificationItem(REPLACE_ATTRIBUTE, new BasicAttribute(userNameAttr, dest.getName())));
        }

        // create modification for password if necessary
        if (dest.getPassword() != null) {
            mods.add(new ModificationItem(REPLACE_ATTRIBUTE,
                                          new BasicAttribute(userPasswordAttr,
                                                             encryptor.encrypt(dest.getPassword().getBytes()))));
        }

        // create modifications for aliases
        if (!src.getAliases().equals(dest.getAliases())) {
            final Attribute aliasesAttr = new BasicAttribute(userAliasesAttr);
            final List<String> aliases = dest.getAliases();
            if (!aliases.isEmpty()) {
                for (String alias : aliases) {
                    aliasesAttr.add(alias);
                }
            } else {
                aliasesAttr.add(dest.getEmail());
            }
            mods.add(new ModificationItem(REPLACE_ATTRIBUTE, aliasesAttr));
        }
        return mods.toArray(new ModificationItem[mods.size()]);
    }
}
