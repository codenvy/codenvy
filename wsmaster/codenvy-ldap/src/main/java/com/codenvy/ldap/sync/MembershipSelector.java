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
package com.codenvy.ldap.sync;

import org.ldaptive.Connection;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.ad.handler.ObjectGuidHandler;
import org.ldaptive.ad.handler.RangeEntryHandler;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.ldaptive.ResultCode.SUCCESS;
import static org.ldaptive.SearchScope.OBJECT;
import static org.ldaptive.SearchScope.SUBTREE;

/**
 * Queries all the group members and returns and
 * iterable requesting each of them when {@link Iterator#next()} is called.
 *
 * @author Yevhenii Voevodin
 */
public class MembershipSelector implements LdapEntrySelector {

    private final String   baseDn;
    private final String   groupsFilter;
    private final String   usersFilter;
    private final String   membersAttr;
    private final String[] returnAttrs;

    public MembershipSelector(String baseDn,
                              String groupsFilter,
                              String usersFilter,
                              String membersAttr,
                              String... returnAttrs) {
        this.baseDn = baseDn;
        this.groupsFilter = groupsFilter;
        this.usersFilter = usersFilter;
        this.membersAttr = membersAttr;
        this.returnAttrs = returnAttrs;
    }

    @Override
    public Iterable<LdapEntry> select(Connection connection) {
        final SearchRequest groupsSearch = new SearchRequest();
        groupsSearch.setBaseDn(baseDn);
        groupsSearch.setSearchFilter(new SearchFilter(groupsFilter));
        groupsSearch.setSearchScope(SUBTREE);
        groupsSearch.setReturnAttributes(membersAttr);
        groupsSearch.setSearchEntryHandlers(new RangeEntryHandler());
        try {
            final Response<SearchResult> response = new SearchOperation(connection).execute(groupsSearch);
            if (response.getResultCode() != SUCCESS) {
                throw new SyncException("Couldn't get groups, result code is " + response.getResultCode());
            }
            return new RequestEachEntryIterable(response.getResult()
                                                        .getEntries()
                                                        .stream()
                                                        .flatMap(entry -> entry.getAttribute(membersAttr)
                                                                               .getStringValues()
                                                                               .stream())
                                                        .collect(toSet()),
                                                connection,
                                                usersFilter,
                                                returnAttrs);
        } catch (LdapException x) {
            throw new SyncException(x.getLocalizedMessage(), x);
        }
    }

    @Override
    public String toString() {
        return "MembershipSelector{" +
               "baseDn='" + baseDn + '\'' +
               ", groupsFilter='" + groupsFilter + '\'' +
               ", usersFilter='" + usersFilter + '\'' +
               ", membersAttr='" + membersAttr + '\'' +
               ", returnAttrs=" + Arrays.toString(returnAttrs) +
               '}';
    }

    private static class RequestEachEntryIterable implements Iterable<LdapEntry> {
        private final Set<String>  dns;
        private final Connection   connection;
        private final SearchFilter usersFilter;

        private RequestEachEntryIterable(Set<String> dns,
                                         Connection connection,
                                         String usersFilter,
                                         String[] returnAttrs) {
            this.dns = dns;
            this.connection = connection;
            this.usersFilter = new SearchFilter(usersFilter);
            this.returnAttrs = returnAttrs;
        }

        private final String[] returnAttrs;

        @Override
        public Iterator<LdapEntry> iterator() {
            return new RequestEachEntryIterator(dns.iterator(), connection, usersFilter, returnAttrs);
        }
    }

    private static class RequestEachEntryIterator implements Iterator<LdapEntry> {

        private final Iterator<String> dnsIterator;
        private final SearchFilter     usersFilter;
        private final String[]         returnAttrs;
        private final SearchOperation  searchOp;

        private RequestEachEntryIterator(Iterator<String> dns,
                                         Connection connection,
                                         SearchFilter usersFilter,
                                         String[] returnAttrs) {
            this.dnsIterator = dns;
            this.usersFilter = usersFilter;
            this.returnAttrs = returnAttrs;
            this.searchOp = new SearchOperation(connection);
        }

        @Override
        public boolean hasNext() {
            return dnsIterator.hasNext();
        }

        @Override
        public LdapEntry next() {
            final String dn = dnsIterator.next();
            final SearchRequest request = new SearchRequest();
            request.setBaseDn(dn);
            request.setSearchFilter(usersFilter);
            request.setSearchScope(OBJECT);
            request.setReturnAttributes(returnAttrs);
            request.setSearchEntryHandlers(new ObjectGuidHandler());
            try {
                final Response<SearchResult> response = searchOp.execute(request);
                if (response.getResultCode() != SUCCESS) {
                    throw new SyncException(format("Couldn't get entry dn '%s', result code is '%s'", dn, response.getResultCode()));
                }
                return response.getResult().getEntry();
            } catch (LdapException x) {
                throw new SyncException(x.getLocalizedMessage(), x);
            }
        }
    }
}
