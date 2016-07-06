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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.function.Supplier;

/**
 * Defines structures to use {@code try}-with-resources statement
 * with {@link NamingEnumeration} and {@link Context}.
 *
 * @author Yevhenii Voevodin
 */
public final class LdapCloser {

    private static final Logger LOG = LoggerFactory.getLogger(LdapCloser.class);

    /**
     * Wraps given {@code enumeration} with {@link CloseableSupplier}, guarantying
     * that it will be closed, if it is used in {@code try}-with-resource statement.
     *
     * @param enumeration
     *         the enumeration to wrap
     * @param <T>
     *         the type of the elements in the enumeration
     * @return {@code CloseableSupplier} supplying the given {@code enumeration} and closing it
     */
    public static <T> CloseableSupplier<NamingEnumeration<T>> wrapCloseable(NamingEnumeration<T> enumeration) {
        return new CloseableSupplier<>(enumeration, () -> close(enumeration));
    }

    /**
     * Wraps given {@code context} with {@link CloseableSupplier}, guarantying
     * that it will be closed, if it is used in {@code try}-with-resource statement.
     *
     * @param context
     *         the context to wrap
     * @param <T>
     *         the type of the context
     * @return {@code CloseableSupplier} supplying the given {@code context} and closing it
     */
    public static <T extends Context> CloseableSupplier<T> wrapCloseable(T context) {
        return new CloseableSupplier<>(context, () -> close(context));
    }

    /**
     * Closes the context, logs error if any error occurs.
     *
     * @param context
     *         the context to close, can be null
     */
    public static void close(Context context) {
        if (context != null) {
            try {
                context.close();
            } catch (NamingException x) {
                LOG.error(x.getMessage(), x);
            }
        }
    }

    /**
     * Closes the enumeration, logs error if any error occurs.
     *
     * @param enumeration
     *         the enumeration to close, can be null
     */
    public static void close(NamingEnumeration<?> enumeration) {
        if (enumeration != null) {
            try {
                enumeration.close();
            } catch (NamingException x) {
                LOG.error(x.getMessage(), x);
            }
        }
    }

    /**
     * Allows not to handle any exception thrown by closeables,
     * as {@link #close(NamingEnumeration)} and {@link #close(Context)}
     * will take care of closing resource and logging errors if necessary.
     */
    public interface NamingCloseable extends AutoCloseable {
        @Override
        void close() throws RuntimeException;
    }

    /**
     * Delegates closing to the given {@link NamingCloseable} and
     * supplies the given value. Designed be used in {@code try}-with-resources
     * constructions.
     *
     * <p>Example:
     * <pre>{@code
     *      try (CloseableSupplier<InitialLdapContext> contextSup = wrapCloseable(contextFactory.createContext())) {
     *          InitialLdapContext context = contextSup.get();
     *          // use context
     *      }
     * }</pre>
     *
     * @param <T>
     *         type of the value supplied by this supplier
     */
    public static class CloseableSupplier<T> implements Supplier<T>, NamingCloseable {

        private final T               value;
        private final NamingCloseable closeDelegate;

        private CloseableSupplier(T value, NamingCloseable closeDelegate) {
            this.value = value;
            this.closeDelegate = closeDelegate;
        }

        @Override
        public void close() {
            closeDelegate.close();
        }

        @Override
        public T get() {
            return value;
        }
    }
}
