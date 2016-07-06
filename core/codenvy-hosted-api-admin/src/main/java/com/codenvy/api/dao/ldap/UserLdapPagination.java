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

import com.google.inject.Inject;

import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.lang.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.naming.ldap.SortControl;
import javax.naming.ldap.SortResponseControl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.collect.Iterables.getLast;

/**
 * It constructs filter to start fetching only desired items.
 * In case of fetching data page by page class provides filters to retrieve the only needed items.
 */
public class UserLdapPagination {
    private static final Logger LOG = LoggerFactory.getLogger(UserLdapPagination.class);

    private final UserAttributesMapper      mapper;
    private final InitialLdapContextFactory contextFactory;
    private final String                    userObjectClassFilter;
    private final String                    userContainerDn;

    /**
     * Contains the item number and its name.
     */
    private AtomicReference<Pair<Integer, String>> filterPairRef;

    @Inject
    public UserLdapPagination(@Named("user.ldap.user_container_dn") String userContainerDn,
                              UserAttributesMapper userAttributesMapper,
                              InitialLdapContextFactory contextFactory) {
        this.mapper = userAttributesMapper;
        this.contextFactory = contextFactory;
        this.userContainerDn = userContainerDn;
        this.filterPairRef = new AtomicReference<>(new Pair<>(0, ""));

        final StringBuilder sb = new StringBuilder();
        for (String objectClass : userAttributesMapper.userObjectClasses) {
            sb.append("(objectClass=");
            sb.append(objectClass);
            sb.append(')');
        }
        this.userObjectClassFilter = sb.toString();
    }

    /**
     * Ldap pagination is a bit tricky {@literal https://docs.oracle.com/javase/8/docs/api/javax/naming/ldap/PagedResultsControl.html}.
     * There is no way to skip some entries to get the desired page. That's why {@link UserLdapPagination} is introduce to construct
     * special filter to add skip behaviour to Ldap.
     */
    public List<UserImpl> get(int maxItems, int skipCount) throws NamingException, IOException {
        List<UserImpl> result = new ArrayList<>();

        byte[] cookie = null;

        InitialLdapContext context = contextFactory.createContext();

        try {
            Pair<Integer, String> filterPair = createFilter(skipCount + 1);

            final int starNum = filterPair.first;
            final String filter = filterPair.second;
            final SortControl sortCtl = new SortControl(mapper.userNameAttr, Control.NONCRITICAL);
            final SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            int curNum = starNum;
            do {
                context.setRequestControls(new Control[] {sortCtl, new PagedResultsControl(maxItems, cookie, Control.CRITICAL)});

                NamingEnumeration<SearchResult> enumeration = context.search(userContainerDn, filter, searchCtls);
                try {
                    while (enumeration.hasMore()) {
                        SearchResult item = enumeration.next();

                        if (skipCount < curNum && curNum <= skipCount + maxItems) {
                            UserImpl user = mapper.fromAttributes(item.getAttributes());
                            result.add(user);

                            if (curNum == skipCount + maxItems) {
                                skip(enumeration);
                                if (isResultSorted(context)) {
                                    storeFilterPair(new Pair<>(curNum, getLast(result).getName()));
                                } else {
                                    LOG.debug("Result not sorted. Page context is undefined.");
                                }
                                return result;
                            }
                        }
                        curNum++;
                    }

                    cookie = getServerGeneratedCookie(context);
                } finally {
                    close(enumeration);
                }
            } while (cookie != null);
        } finally {
            close(context);
        }

        return result;
    }

    /**
     * @param desiredStartNum
     *         the desired item number to start retrieving data from,
     *         the returning starting number might be differ from the desired depending on the {@link #filterPairRef}
     * @return the {@link Pair} containing the item number to start retrieve data from and corresponding filter
     */
    private Pair<Integer, String> createFilter(int desiredStartNum) {
        Pair<Integer, String> filterPair = filterPairRef.get();

        final int startNum = desiredStartNum == filterPair.first + 1 ? desiredStartNum : 1;
        final String filter = doCreateFilter(startNum, filterPair.second);

        return new Pair<>(startNum, filter);
    }

    /**
     * Creates filter corresponding to starting item.
     *
     * The idea is consists in getting entries that are greater than a specific one.
     * It is supposed that entries are sorted.
     *
     * @param startNum
     *         the item number to start retrieving items from
     * @param name
     *         the name of the preceding item
     * @return the corresponding filter
     */
    private String doCreateFilter(int startNum, String name) {
        if (startNum == 1) {
            return "(&" + userObjectClassFilter + ")";
        } else {
            return String.format("(&(%1$s>=%2$s)(!(%1$s=%2$s))(&" + userObjectClassFilter + "))", mapper.userNameAttr, name);
        }
    }

    private void storeFilterPair(Pair<Integer, String> filterPair) {
        LOG.debug("Filter is set to " + filterPair);
        filterPairRef.set(filterPair);
    }

    /** Skips results till end to get response controls then. */
    private void skip(NamingEnumeration<SearchResult> enumeration) throws NamingException {
        while (enumeration.hasMore()) {
            enumeration.next();
        }
    }

    /** Returns server side cookie to retrieve the following page in the next request */
    private byte[] getServerGeneratedCookie(InitialLdapContext context) throws NamingException {
        Control[] controls = context.getResponseControls();
        if (controls != null) {
            for (Control control : controls) {
                if (control instanceof PagedResultsResponseControl) {
                    PagedResultsResponseControl prrc = (PagedResultsResponseControl)control;
                    return prrc.getCookie();
                }
            }
        }
        return null;
    }

    /** Indicates if result returned from server is sorted. */
    private boolean isResultSorted(InitialLdapContext context) throws NamingException {
        Control[] controls = context.getResponseControls();
        if (controls != null) {
            for (Control control : controls) {
                if (control instanceof SortResponseControl) {
                    SortResponseControl src = (SortResponseControl)control;
                    return src.isSorted();
                }
            }
        }

        return false;
    }

    private void close(Context ctx) {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void close(NamingEnumeration<SearchResult> enumeration) {
        if (enumeration != null) {
            try {
                enumeration.close();
            } catch (NamingException e) {
                LOG.warn(e.getMessage(), e);
            }
        }
    }
}
