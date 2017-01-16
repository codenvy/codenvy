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
package com.codenvy.im.testhelper.ldap;

import org.apache.commons.io.FileUtils;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.CoreSession;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.interceptor.context.AddOperationContext;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.core.schema.SchemaPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.shared.ldap.entry.DefaultServerEntry;
import org.apache.directory.shared.ldap.entry.ServerEntry;
import org.apache.directory.shared.ldap.ldif.LdifEntry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.apache.directory.shared.ldap.schema.SchemaManager;
import org.apache.directory.shared.ldap.schema.ldif.extractor.SchemaLdifExtractor;
import org.apache.directory.shared.ldap.schema.ldif.extractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.shared.ldap.schema.loader.ldif.LdifSchemaLoader;
import org.apache.directory.shared.ldap.schema.manager.impl.DefaultSchemaManager;
import org.apache.directory.shared.ldap.schema.registries.SchemaLoader;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * A simple example exposing how to embed Apache Directory Server version 1.5.7
 * into an application.
 *
 * @author Alexander Reshetnyak
 * @author Dmytro Nochevnov
 */
public class EmbeddedADS {
    public static final String ADS_PROTOCOL                = "ldap";
    public static final String ADS_HOST                    = "localhost";
    public static final String ADS_PORT                    = "10389";
    public static final String ADS_SECURITY_PRINCIPAL      = "uid=admin,ou=system";
    public static final String ADS_SECURITY_CREDENTIALS    = "secret";
    public static final String ADS_SECURITY_AUTHENTICATION = "simple";

    /** The directory service */
    private DirectoryService service;

    /** The LDAP server */
    private LdapServer server;

    /**
     * Creates a new instance of EmbeddedADS. It initializes the directory service.
     *
     * @throws Exception
     *         If something went wrong
     */
    public EmbeddedADS(File workDir) throws Exception {
        initDirectoryService(workDir);
    }

    public LdapServer getServer() {
        return server;
    }

    public DirectoryService getService() {
        return service;
    }

    /**
     * Add a new partition to the server
     *
     * @param partitionId
     *         The partition Id
     * @param partitionDn
     *         The partition DN
     * @return The newly added partition
     * @throws Exception
     *         If the partition can't be added
     */
    public JdbmPartition addPartition(String partitionId, String partitionDn) throws Exception {
        // Create a new partition with the given partition id
        JdbmPartition partition = new JdbmPartition();
        partition.setId(partitionId);
        partition.setPartitionDir(new File(service.getWorkingDirectory(), partitionId));
        partition.setSuffix(partitionDn);
        service.addPartition(partition);

        return partition;
    }

    /**
     * initialize the schema manager and add the schema partition to directory service
     *
     * @throws Exception
     *         if the schema LDIF files are not found on the classpath
     */
    private void initSchemaPartition() throws Exception {
        SchemaPartition schemaPartition = service.getSchemaService().getSchemaPartition();

        // Init the LdifPartition
        LdifPartition ldifPartition = new LdifPartition();
        String workingDirectory = service.getWorkingDirectory().getPath();
        ldifPartition.setWorkingDirectory( workingDirectory + "/schema" );

        // Extract the schema on disk (a brand new one) and load the registries
        File schemaRepository = new File( workingDirectory, "schema" );
        SchemaLdifExtractor extractor = new DefaultSchemaLdifExtractor( new File( workingDirectory ) );
        extractor.extractOrCopy( true );

        schemaPartition.setWrappedPartition( ldifPartition );

        SchemaLoader loader = new LdifSchemaLoader( schemaRepository );
        SchemaManager schemaManager = new DefaultSchemaManager( loader );
        service.setSchemaManager( schemaManager );

        // We have to load the schema now, otherwise we won't be able
        // to initialize the Partitions, as we won't be able to parse
        // and normalize their suffix DN
        schemaManager.loadAllEnabled();

        schemaPartition.setSchemaManager( schemaManager );

        List<Throwable> errors = schemaManager.getErrors();

        if ( errors.size() != 0 )
        {
            throw new Exception( "Schema load failed : " + errors );
        }
    }


    /**
     * Initialize the server. It creates the partition, adds the index, and
     * injects the context entries for the created partitions.
     *
     * @param workDir
     *         the directory to be used for storing the data
     * @throws Exception
     *         if there were some problems while initializing the system
     */
    private void initDirectoryService(File workDir) throws Exception {
        // Initialize the LDAP service
        service = new DefaultDirectoryService();
        service.setWorkingDirectory(workDir);

        // first load the schema
        initSchemaPartition();

        // then the system partition
        // this is a MANDATORY partition
        // DO NOT add this via addPartition() method, trunk code complains about duplicate partition
        // while initializing
        JdbmPartition systemPartition = new JdbmPartition();
        systemPartition.setId("system");
        systemPartition.setPartitionDir(new File(service.getWorkingDirectory(), systemPartition.getId()));
        systemPartition.setSuffix(ServerDNConstants.SYSTEM_DN);
        systemPartition.setSchemaManager(service.getSchemaManager());

        // mandatory to call this method to set the system partition
        // Note: this system partition might be removed from trunk
        service.setSystemPartition(systemPartition);

        // Disable the ChangeLog system
        service.getChangeLog().setEnabled(false);
        service.setDenormalizeOpAttrsEnabled(true);

        // And start the service
        service.startup();
    }

    public void importEntriesFromLdif(JdbmPartition partition, Path ldif) throws Exception {
        try (java.io.InputStream is = new FileInputStream(ldif.toFile())) {
            LdifReader entries = new LdifReader(is);

            CoreSession rootDSE = service.getAdminSession();
            SchemaManager schemaManager = rootDSE.getDirectoryService().getSchemaManager();

            for (LdifEntry ldifEntry : entries) {
                ServerEntry entry = new DefaultServerEntry(schemaManager, ldifEntry.getEntry());
                AddOperationContext addContext = new AddOperationContext(service.getAdminSession(), entry);
                partition.add(addContext);
            }
        }
    }

    /**
     * starts the LdapServer
     *
     * @throws Exception
     */
    public void startServer() throws Exception {
        server = new LdapServer();
        int serverPort = 10389;
        server.setTransports(new TcpTransport(serverPort));
        server.setDirectoryService(service);
        server.setSaslPrincipal(ADS_SECURITY_PRINCIPAL);

        server.start();
    }
    
     /**
     * Stop and cleanup test Ldap Server
     */
    public void stopAndCleanupServer() {
        server.stop();
        FileUtils.deleteQuietly(service.getWorkingDirectory());
    }
}
