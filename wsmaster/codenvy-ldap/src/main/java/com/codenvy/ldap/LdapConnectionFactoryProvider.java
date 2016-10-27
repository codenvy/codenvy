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

import org.eclipse.che.commons.annotation.Nullable;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.BindRequest;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.ad.extended.FastBindOperation;
import org.ldaptive.pool.BindPassivator;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.pool.Passivator;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.SearchValidator;
import org.ldaptive.sasl.CramMd5Config;
import org.ldaptive.sasl.DigestMd5Config;
import org.ldaptive.sasl.ExternalConfig;
import org.ldaptive.sasl.GssApiConfig;
import org.ldaptive.sasl.Mechanism;
import org.ldaptive.sasl.QualityOfProtection;
import org.ldaptive.sasl.SaslConfig;
import org.ldaptive.sasl.SecurityStrength;
import org.ldaptive.ssl.KeyStoreCredentialConfig;
import org.ldaptive.ssl.SslConfig;
import org.ldaptive.ssl.X509CredentialConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.time.Duration;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

@Singleton
public class LdapConnectionFactoryProvider implements Provider<PooledConnectionFactory> {
    private static final Logger LOG = LoggerFactory.getLogger(LdapConnectionFactoryProvider.class);

    private final PooledConnectionFactory connFactory;

    @Inject
    public LdapConnectionFactoryProvider(@NotNull @Named("ldap.url") String ldapUrl,
                                         @Nullable @Named("ldap.connection.provider") String providerClass,
                                         @Nullable @Named("ldap.connection.bind.dn") String bindDn,
                                         @Nullable @Named("ldap.connection.bind.password") String bindCredential,
                                         @Nullable @Named("ldap.connection.use_ssl") String useSsl,
                                         @Nullable @Named("ldap.connection.use_start_tls") String useStartTls,
                                         @Nullable @Named("ldap.connection.pool.min_size") String minPoolSize,
                                         @Nullable @Named("ldap.connection.pool.max_size") String maxPoolSize,
                                         @Nullable @Named("ldap.connection.pool.validate.on_checkout") String validateOnCheckout,
                                         @Nullable @Named("ldap.connection.pool.validate.on_checkin") String validateOnCheckin,
                                         @Nullable @Named("ldap.connection.pool.validate.period_ms") String validatePeriod,
                                         @Nullable @Named("ldap.connection.pool.validate.periodically") String validatePeriodically,
                                         @Nullable @Named("ldap.connection.pool.fail_fast") String failFast,
                                         @Nullable @Named("ldap.connection.pool.idle_ms") String idleTime,
                                         @Nullable @Named("ldap.connection.pool.prune_ms") String prunePeriod,
                                         @Nullable @Named("ldap.connection.pool.block_wait_ms") String blockWaitTime,
                                         @Nullable @Named("ldap.connection.connect_timeout_ms") String connectTimeout,
                                         @Nullable @Named("ldap.connection.response_timeout_ms") String responseTimeout,
                                         @Nullable @Named("ldap.connection.ssl.trust_certificates") String trustCertificates,
                                         @Nullable @Named("ldap.connection.ssl.keystore.name") String keystore,
                                         @Nullable @Named("ldap.connection.ssl.keystore.password") String keystorePassword,
                                         @Nullable @Named("ldap.connection.ssl.keystore.type") String keystoreType,
                                         @Nullable @Named("ldap.connection.sasl.realm") String saslRealm,
                                         @Nullable @Named("ldap.connection.sasl.mechanism") String saslMechanism,
                                         @Nullable @Named("ldap.connection.sasl.authorization_id") String saslAuthorizationId,
                                         @Nullable @Named("ldap.connection.sasl.security_strength") String saslSecurityStrength,
                                         @Nullable @Named("ldap.connection.sasl.mutual_auth") String saslMutualAuth,
                                         @Nullable @Named("ldap.connection.sasl.quality_of_protection") String saslQualityOfProtection) {
        final PoolConfig pc = new PoolConfig();
        if (!isNullOrEmpty(minPoolSize)) {
            pc.setMinPoolSize(parseInt(minPoolSize));
        }
        if (!isNullOrEmpty(maxPoolSize)) {
            pc.setMaxPoolSize(parseInt(maxPoolSize));
        }
        if (!isNullOrEmpty(validateOnCheckout)) {
            pc.setValidateOnCheckOut(parseBoolean(validateOnCheckout));
        }
        if (!isNullOrEmpty(validateOnCheckin)) {
            pc.setValidateOnCheckIn(parseBoolean(validateOnCheckin));
        }
        if (!isNullOrEmpty(validatePeriodically)) {
            pc.setValidatePeriodically(parseBoolean(validatePeriodically));
        }
        if (!isNullOrEmpty(validatePeriod)) {
            pc.setValidatePeriod(Duration.ofMillis(parseLong(validatePeriod)));
        }


        final ConnectionConfig cc = new ConnectionConfig(ldapUrl);
        if (!isNullOrEmpty(useSsl)) {
            cc.setUseSSL(parseBoolean(useSsl));
        }
        if (!isNullOrEmpty(useStartTls)) {
            cc.setUseStartTLS(parseBoolean(useStartTls));
        }
        if (!isNullOrEmpty(connectTimeout)) {
            cc.setConnectTimeout(Duration.ofMillis(parseLong(connectTimeout)));
        }

        if (!isNullOrEmpty(responseTimeout)) {
            cc.setResponseTimeout(Duration.ofMillis(parseLong(responseTimeout)));
        }
        if (!isNullOrEmpty(trustCertificates)) {
            final X509CredentialConfig cfg = new X509CredentialConfig();
            cfg.setTrustCertificates(trustCertificates);
            cc.setSslConfig(new SslConfig(cfg));
        } else if (!isNullOrEmpty(keystore)) {
            final KeyStoreCredentialConfig cfg = new KeyStoreCredentialConfig();
            cfg.setKeyStore(keystore);

            if (!isNullOrEmpty(keystorePassword)) {
                cfg.setKeyStorePassword(keystorePassword);
            }
            if (!isNullOrEmpty(keystoreType)) {
                cfg.setKeyStoreType(keystoreType);
            }
            cc.setSslConfig(new SslConfig(cfg));
        } else {
            cc.setSslConfig(new SslConfig());
        }

        Passivator<Connection> passivator = null;

        if (!isNullOrEmpty(saslMechanism)) {
            final BindConnectionInitializer bc = new BindConnectionInitializer();
            final SaslConfig sc;
            switch (Mechanism.valueOf(saslMechanism)) {
                case DIGEST_MD5:
                    sc = new DigestMd5Config();
                    ((DigestMd5Config)sc).setRealm(saslRealm);
                    break;
                case CRAM_MD5:
                    sc = new CramMd5Config();
                    break;
                case EXTERNAL:
                    sc = new ExternalConfig();
                    break;
                case GSSAPI:
                    sc = new GssApiConfig();
                    ((GssApiConfig)sc).setRealm(saslRealm);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown SASL mechanism " + saslMechanism);

            }

            if (!isNullOrEmpty(saslAuthorizationId)) {
                sc.setAuthorizationId(saslAuthorizationId);
            }
            if (!isNullOrEmpty(saslMutualAuth)) {
                sc.setMutualAuthentication(parseBoolean(saslMutualAuth));
            }
            if (!isNullOrEmpty(saslQualityOfProtection)) {
                sc.setQualityOfProtection(QualityOfProtection.valueOf(saslQualityOfProtection));
            }
            if (!isNullOrEmpty(saslSecurityStrength)) {
                sc.setSecurityStrength(SecurityStrength.valueOf(saslSecurityStrength));
            }
            bc.setBindSaslConfig(sc);
            cc.setConnectionInitializer(bc);
            passivator = new BindPassivator(new BindRequest(sc));
        } else if ("*".equals(bindCredential) && "*".equals(bindDn)) {
            cc.setConnectionInitializer(new FastBindOperation.FastBindConnectionInitializer());
        } else if (!isNullOrEmpty(bindDn) && !isNullOrEmpty(bindCredential)) {
            Credential credential = new Credential(bindCredential);
            cc.setConnectionInitializer(new BindConnectionInitializer(bindDn, credential));
            passivator = new BindPassivator(new BindRequest(bindDn, credential));
        }

        final DefaultConnectionFactory bindCf = new DefaultConnectionFactory(cc);

        if (!isNullOrEmpty(providerClass)) {
            try {
                final Class clazz = Class.forName(providerClass);
                bindCf.setProvider(org.ldaptive.provider.Provider.class.cast(clazz.newInstance()));
            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        final BlockingConnectionPool cp = new BlockingConnectionPool(pc, bindCf);

        if (!isNullOrEmpty(blockWaitTime)) {
            cp.setBlockWaitTime(Duration.ofMillis(parseLong(blockWaitTime)));
        }
        cp.setPoolConfig(pc);

        final IdlePruneStrategy strategy = new IdlePruneStrategy();
        if (!isNullOrEmpty(idleTime)) {
            strategy.setIdleTime(Duration.ofMillis(parseLong(idleTime)));
        }
        if (!isNullOrEmpty(prunePeriod)) {
            strategy.setPrunePeriod(Duration.ofMillis(parseLong(prunePeriod)));
        }
        cp.setPruneStrategy(strategy);

        if (pc.isValidatePeriodically() ||
            pc.isValidateOnCheckIn() ||
            pc.isValidateOnCheckOut()) {
            cp.setValidator(new SearchValidator());
        }

        if (passivator != null) {
            cp.setPassivator(passivator);
        }

        if (!isNullOrEmpty(failFast)) {
            cp.setFailFastInitialize(parseBoolean(failFast));
        }
        cp.initialize();
        connFactory = new PooledConnectionFactory(cp);
    }

    @Override
    public PooledConnectionFactory get() {
        return connFactory;
    }


}
