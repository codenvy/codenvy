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
package com.codenvy.api.dao.mongo;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.model.workspace.EnvironmentRecipe;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentRecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConf2Impl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;

import java.util.List;
import java.util.Map;

import static com.codenvy.api.dao.mongo.MongoUtil.documentsListAsMap;
import static com.codenvy.api.dao.mongo.MongoUtil.mapAsDocumentsList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Encodes & decodes {@link WorkspaceImpl}.
 *
 * @author Eugene Voevodin
 */
public class WorkspaceImplCodec implements Codec<WorkspaceImpl> {

    public static final String MACHINE_SOURCE = "machineSource";
    public static final String MACHINE_SOURCE_TYPE = "type";
    public static final String MACHINE_SOURCE_LOCATION = "location";
    public static final String MACHINE_SOURCE_CONTENT = "content";

    private Codec<Document> codec;

    public WorkspaceImplCodec(CodecRegistry registry) {
        codec = registry.get(Document.class);
    }

    @Override
    public WorkspaceImpl decode(BsonReader reader, DecoderContext decoderContext) {
        final Document document = codec.decode(reader, decoderContext);
        @SuppressWarnings("unchecked") // 'config' field is always document
        final Document configDocument = (Document)document.get("config");

        @SuppressWarnings("unchecked") // 'attributes' field is always list
        final List<Document> attributes = (List<Document>)document.get("attributes");

        @SuppressWarnings("unchecked") // 'commands' field is always list
        final List<Document> commandDocuments = (List<Document>)configDocument.get("commands");
        final List<CommandImpl> commands = commandDocuments.stream()
                                                           .map(WorkspaceImplCodec::asCommand)
                                                           .collect(toList());

        @SuppressWarnings("unchecked") // 'projects' field is always list
        final List<Document> projectDocuments = (List<Document>)configDocument.get("projects");
        final List<ProjectConfigImpl> projects = projectDocuments.stream()
                                                                 .map(WorkspaceImplCodec::asProjectConfig)
                                                                 .collect(toList());

        @SuppressWarnings("unchecked") // 'environments' fields is always map
        final Map<String, Document> envDocuments = (Map<String, Document>)configDocument.get("environments");
        final Map<String, EnvironmentImpl> environments = envDocuments.entrySet()
                                                                      .stream()
                                                                      .collect(toMap(Map.Entry::getKey,
                                                                                     entry -> WorkspaceImplCodec
                                                                                             .asEnvironment(
                                                                                                     entry.getValue())));

        return WorkspaceImpl.builder()
                            .setId(document.getString("_id"))
                            .setConfig(WorkspaceConfigImpl.builder()
                                                          .setName(configDocument.getString("name"))
                                                          .setDescription(configDocument.getString("description"))
                                                          .setDefaultEnv(configDocument.getString("defaultEnv"))
                                                          .setCommands(commands)
                                                          .setProjects(projects)
                                                          .setEnvironments(environments)
                                                          .build())
                            .setAttributes(documentsListAsMap(attributes))
                            .setNamespace(document.getString("namespace"))
                            .build();
    }

    @Override
    public void encode(BsonWriter writer, WorkspaceImpl workspace, EncoderContext encoderContext) {
        final Document document = new Document().append("_id", workspace.getId())
                                                .append("namespace", workspace.getNamespace())
                                                .append("attributes", mapAsDocumentsList(workspace.getAttributes()));
        final WorkspaceConfigImpl config = workspace.getConfig();
        final Document configDocument = new Document().append("name", config.getName())
                                                      .append("description", config.getDescription())
                                                      .append("defaultEnv", config.getDefaultEnv());
        configDocument.append("commands", config.getCommands()
                                                .stream()
                                                .map(command -> new Document().append("name", command.getName())
                                                                              .append("commandLine", command.getCommandLine())
                                                                              .append("type", command.getType())
                                                                              .append("attributes",
                                                                                      mapAsDocumentsList(command.getAttributes())))
                                                .collect(toList()));
        configDocument.append("projects", config.getProjects()
                                                .stream()
                                                .map(WorkspaceImplCodec::asDocument)
                                                .collect(toList()));

        configDocument.append("environments", config.getEnvironments()
                                                    .entrySet()
                                                    .stream()
                                                    .collect(toMap(Map.Entry::getKey,
                                                                   entry -> asDocument(entry.getValue()))));
        document.append("config", configDocument);
        codec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<WorkspaceImpl> getEncoderClass() {
        return WorkspaceImpl.class;
    }

    private static CommandImpl asCommand(Document document) {
        CommandImpl command = new CommandImpl(document.getString("name"),
                                              document.getString("commandLine"),
                                              document.getString("type"));

        @SuppressWarnings("unchecked") // 'attributes' field is always list
        final List<Document> attributes = (List<Document>)document.get("attributes");
        if (attributes != null) {
            command.setAttributes(documentsListAsMap(attributes));
        }
        return command;
    }

    @SuppressWarnings("unchecked") // contains safe casts - see #decode
    private static ProjectConfigImpl asProjectConfig(Document document) {
        final ProjectConfigImpl projectConfig = new ProjectConfigImpl();
        projectConfig.setName(document.getString("name"));
        projectConfig.setPath(document.getString("path"));
        projectConfig.setType(document.getString("type"));
        projectConfig.setDescription(document.getString("description"));

        final List<String> mixins = (List<String>)document.get("mixins");
        projectConfig.setMixins(mixins);

        final List<Document> attributes = (List<Document>)document.get("attributes");
        projectConfig.setAttributes(attributes.stream()
                                              .collect(toMap(d -> d.getString("name"), d -> (List<String>)d.get("value"))));

        final Document sourceDocument = document.get("source", Document.class);
        if (sourceDocument != null) {
            final SourceStorageImpl storage = new SourceStorageImpl(sourceDocument.getString("type"),
                                                                    sourceDocument.getString("location"),
                                                                    documentsListAsMap((List<Document>)sourceDocument.get("parameters")));
            projectConfig.setSource(storage);
        }
        return projectConfig;
    }

    private static Document asDocument(ProjectConfig project) {
        final Document document = new Document().append("name", project.getName())
                                                .append("path", project.getPath())
                                                .append("type", project.getType())
                                                .append("description", project.getDescription())
                                                .append("mixins", project.getMixins())
                                                .append("attributes", mapAsDocumentsList(project.getAttributes()));

        final SourceStorage sourceStorage = project.getSource();
        if (sourceStorage != null) {
            document.append("source", new Document().append("type", sourceStorage.getType())
                                                    .append("location", sourceStorage.getLocation())
                                                    .append("parameters", mapAsDocumentsList(sourceStorage.getParameters())));
        }


        return document;
    }

    private static EnvironmentImpl asEnvironment(Document document) {
        EnvironmentImpl environment = new EnvironmentImpl();

        Document recipeDocument = document.get("recipe", Document.class);
        if (recipeDocument != null) {
            EnvironmentRecipeImpl recipe = new EnvironmentRecipeImpl(recipeDocument.getString("type"),
                                                                     recipeDocument.getString("contentType"),
                                                                     recipeDocument.getString("content"),
                                                                     recipeDocument.getString("location"));
            environment.setRecipe(recipe);
        }

        @SuppressWarnings("unchecked") // machines field is always map
        Map<String, Document> machinesDocuments = (Map<String, Document>)document.get("machines");
        if (machinesDocuments != null) {
            Map<String, ExtendedMachineImpl> machines =
                    machinesDocuments.entrySet()
                                     .stream()
                                     .collect(toMap(Map.Entry::getKey,
                                                    entry -> asExtendedMachine(entry.getValue())));
            environment.setMachines(machines);
        }
        return environment;
    }

    private static Document asDocument(EnvironmentImpl environment) {
        final Document document = new Document();

        EnvironmentRecipe recipe = environment.getRecipe();
        if (recipe != null) {
            document.append("recipe", new Document().append("type", recipe.getType())
                                                    .append("content", recipe.getContent())
                                                    .append("location", recipe.getLocation())
                                                    .append("contentType", recipe.getContentType()));
        }
        if (environment.getMachines() != null) {
            document.append("machines", environment.getMachines()
                                                   .entrySet()
                                                   .stream()
                                                   .collect(toMap(Map.Entry::getKey,
                                                                  entry -> asDocument(entry.getValue()))));
        }

        return document;
    }

    private static ExtendedMachineImpl asExtendedMachine(Document document) {
        ExtendedMachineImpl machine = new ExtendedMachineImpl();
        @SuppressWarnings("unchecked")
        List<String> agents = (List<String>)document.get("agents");
        machine.setAgents(agents);
        @SuppressWarnings("unchecked")
        Map<String, Document> serversDocs = (Map<String, Document>)document.get("servers");
        if (serversDocs != null) {
            Map<String, ServerConf2Impl> servers = serversDocs.entrySet()
                                                              .stream()
                                                              .collect(toMap(Map.Entry::getKey,
                                                                             entry -> asServerConf2(entry.getValue())));
            machine.setServers(servers);
        }
        @SuppressWarnings("unchecked")
        List<Document> attributes = (List<Document>)document.get("attributes");
        if (attributes != null) {
            machine.setAttributes(documentsListAsMap(attributes));
        }

        return machine;
    }

    private static Document asDocument(ExtendedMachineImpl machine) {
        Document document = new Document();
        document.append("agents", machine.getAgents());

        if (machine.getServers() != null) {
            Map<String, Document> servers = machine.getServers()
                                                   .entrySet()
                                                   .stream()
                                                   .collect(toMap(Map.Entry::getKey,
                                                                  entry -> asDocument(entry.getValue())));
            document.append("servers", servers);
        }
        if (machine.getAttributes() != null) {
            document.append("attributes", mapAsDocumentsList(machine.getAttributes()));
        }

        return document;
    }

    private static ServerConf2Impl asServerConf2(Document document) {
        @SuppressWarnings("unchecked")
        List<Document> properties = (List<Document>)document.get("properties");
        return new ServerConf2Impl(document.getString("port"),
                                   document.getString("protocol"),
                                   properties == null ? null : documentsListAsMap(properties));
    }

    private static Document asDocument(ServerConf2Impl serverConf2) {
        Document document = new Document();

        document.append("port", serverConf2.getPort());
        document.append("protocol", serverConf2.getProtocol());
        document.append("properties", serverConf2.getProperties() == null ?
                                      null :
                                      mapAsDocumentsList(serverConf2.getProperties()));

        return document;
    }

    public static WorkspaceConfigImpl asWorkspaceConfig(Document document) {
        @SuppressWarnings("unchecked") // 'commands' field is always list
        final List<Document> commandDocuments = (List<Document>)document.get("commands");
        final List<CommandImpl> commands = commandDocuments.stream()
                                                           .map(WorkspaceImplCodec::asCommand)
                                                           .collect(toList());

        @SuppressWarnings("unchecked") // 'projects' field is always list
        final List<Document> projectDocuments = (List<Document>)document.get("projects");
        final List<ProjectConfigImpl> projects = projectDocuments.stream()
                                                                 .map(WorkspaceImplCodec::asProjectConfig)
                                                                 .collect(toList());

        @SuppressWarnings("unchecked") // 'environments' fields is aways map
        final Map<String, Document> envDocuments = (Map<String, Document>)document.get("environments");
        final Map<String, EnvironmentImpl> environments = envDocuments.entrySet()
                                                                      .stream()
                                                                      .collect(toMap(Map.Entry::getKey,
                                                                                     entry -> asEnvironment(entry.getValue())));

        return WorkspaceConfigImpl.builder()
                                  .setName(document.getString("name"))
                                  .setDescription(document.getString("description"))
                                  .setDefaultEnv(document.getString("defaultEnv"))
                                  .setCommands(commands)
                                  .setProjects(projects)
                                  .setEnvironments(environments)
                                  .build();
    }

    public static Document asDocument(WorkspaceConfigImpl workspace) {
        final Document document = new Document().append("name", workspace.getName())
                                                .append("description", workspace.getDescription())
                                                .append("defaultEnv", workspace.getDefaultEnv());
        document.append("commands", workspace.getCommands()
                                             .stream()
                                             .map(command -> new Document().append("name", command.getName())
                                                                           .append("commandLine", command.getCommandLine())
                                                                           .append("type", command.getType()))
                                             .collect(toList()));
        document.append("projects", workspace.getProjects()
                                             .stream()
                                             .map(WorkspaceImplCodec::asDocument)
                                             .collect(toList()));

        document.append("environments", workspace.getEnvironments()
                                                 .entrySet()
                                                 .stream()
                                                 .collect(toMap(Map.Entry::getKey,
                                                                entry -> asDocument(entry.getValue()))));
        return document;
    }
}
