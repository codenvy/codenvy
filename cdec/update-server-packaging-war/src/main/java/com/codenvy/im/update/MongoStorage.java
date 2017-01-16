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
package com.codenvy.im.update;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Version;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoDatabase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class MongoStorage {
    private static final Logger LOG = LoggerFactory.getLogger(MongoStorage.class);

    private final MongoDatabase db;
    private final Path          dir;
    private final MongoClientURI uri;

    @Inject
    public MongoStorage(@Named("update-server.mongodb.url") String url,
                        @Named("update-server.mongodb.embedded") boolean embedded,
                        @Named("update-server.mongodb.embedded_dir") String dir) throws IOException {
        this.uri = new MongoClientURI(url);
        this.dir = Paths.get(dir);

        if (embedded) { // for testing purpose only
            try {
                initEmbeddedStorage();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                throw new IOException(e);
            }
        }

        db = connectToDB();
    }

    protected MongoDatabase getDb() {
        return db;
    }

    private MongoDatabase connectToDB() throws IOException {
        final MongoClient mongoClient = new MongoClient(uri);

        MongoDatabase db;
        try {
            db = mongoClient.getDatabase(uri.getDatabase());
            db.withWriteConcern(WriteConcern.ACKNOWLEDGED);
            db.withReadPreference(ReadPreference.primaryPreferred());
        } catch (MongoException e) {
            mongoClient.close();
            throw new IOException(e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                mongoClient.close();
            }
        });

        LOG.info("Connection to MongoDB has been initialized");
        return db;
    }

    private void initEmbeddedStorage() throws IOException {
        if (isStarted()) {
            return;
        }

        LOG.info("Embedded MongoDB is starting up");

        Files.createDirectories(dir);

        MongodConfigBuilder mongodConfigBuilder = new MongodConfigBuilder();
        mongodConfigBuilder.net(new Net(12000, false));
        mongodConfigBuilder.replication(new Storage(dir.toString(), null, 0));
        mongodConfigBuilder.version(Version.V2_5_4);

        RuntimeConfigBuilder runtimeConfigBuilder = new RuntimeConfigBuilder().defaults(Command.MongoD);

        MongodStarter starter = MongodStarter.getInstance(runtimeConfigBuilder.build());
        final MongodExecutable mongoExe = starter.prepare(mongodConfigBuilder.build());

        try {
            mongoExe.start();
            LOG.info("Embedded MongoDB is started");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                mongoExe.stop();
            }
        });
    }

    /**
     * Checks if embedded storage is started. If connection can be opened, then it means storage is started. All JVM
     * share the same storage.
     */
    private boolean isStarted() {
        try {
            try (Socket sock = new Socket()) {
                URI url = new URI(uri.toString());

                int timeout = 500;
                InetAddress addr = InetAddress.getByName(url.getHost());
                SocketAddress sockAddr = new InetSocketAddress(addr, url.getPort());

                sock.connect(sockAddr, timeout);
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
