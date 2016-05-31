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
import org.eclipse.che.api.core.model.machine.Limits;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl.MachineConfigImplBuilder;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;

import java.util.List;

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

        @SuppressWarnings("unchecked") // 'environments' fields is aways map
        final List<Document> envDocuments = (List<Document>)configDocument.get("environments");
        final List<EnvironmentImpl> environments = envDocuments.stream()
                                                               .map(WorkspaceImplCodec::asEnvironment)
                                                               .collect(toList());

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
                                                    .stream()
                                                    .map(WorkspaceImplCodec::asDocument)
                                                    .collect(toList()));
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
        @SuppressWarnings("unchecked") // 'machineConfigs' field is always list of documents
        final List<Document> machineDocuments = (List<Document>)document.get("machineConfigs");
        final List<MachineConfigImpl> machineConfigs = machineDocuments.stream()
                                                                       .map(WorkspaceImplCodec::asMachineConfig)
                                                                       .collect(toList());
        RecipeImpl recipe = null;
        final Document recipeDocument = document.get("recipe", Document.class);
        if (recipeDocument != null) {
            recipe = new RecipeImpl();
            recipe.setType(recipeDocument.getString("type"));
            recipe.setScript(recipeDocument.getString("script"));
        }
        return new EnvironmentImpl(document.getString("name"), recipe, machineConfigs);
    }

    private static Document asDocument(EnvironmentImpl environment) {
        final Document document = new Document();

        final Recipe recipe = environment.getRecipe();
        if (recipe != null) {
            document.append("recipe", new Document("type", recipe.getType()).append("script", recipe.getScript()));
        }

        document.append("name", environment.getName())
                .append("machineConfigs", environment.getMachineConfigs()
                                                     .stream()
                                                     .map(WorkspaceImplCodec::asDocument)
                                                     .collect(toList()));
        return document;
    }

    private static ServerConfImpl asServerConf(Document document) {
        return new ServerConfImpl(document.getString("ref"),
                                  document.getString("port"),
                                  document.getString("protocol"),
                                  document.getString("path"));
    }

    private static MachineConfigImpl asMachineConfig(Document document) {
        final MachineConfigImplBuilder builder = MachineConfigImpl.builder()
                                                                  .setDev(document.getBoolean("isDev"))
                                                                  .setName(document.getString("name"))
                                                                  .setType(document.getString("type"));
        final Document sourceDocument = document.get("source", Document.class);
        if (sourceDocument != null) {
            builder.setSource(new MachineSourceImpl(sourceDocument.getString(MACHINE_SOURCE_TYPE))
                                      .setContent(sourceDocument.getString(MACHINE_SOURCE_CONTENT))
                                      .setLocation(sourceDocument.getString(MACHINE_SOURCE_LOCATION)));
        }
        final Document limitsDocument = document.get("limits", Document.class);
        if (limitsDocument != null) {
            builder.setLimits(new LimitsImpl(limitsDocument.getInteger("ram", 0)));
        }

        @SuppressWarnings("unchecked") // 'servers' field is always list
        final List<Document> serversDocuments = (List<Document>)document.get("servers");
        if (serversDocuments != null) {
            builder.setServers(serversDocuments.stream()
                                               .map(WorkspaceImplCodec::asServerConf)
                                               .collect(toList()));
        }

        @SuppressWarnings("unchecked") // 'envVariables' field is always list
        final List<Document> envVariables = (List<Document>)document.get("envVariables");
        if (envVariables != null) {
            builder.setEnvVariables(documentsListAsMap(envVariables));
        }
        return builder.build();
    }

    private static Document asDocument(MachineConfigImpl config) {
        final Document document = new Document().append("isDev", config.isDev())
                                                .append("name", config.getName())
                                                .append("type", config.getType())
                                                .append("envVariables", mapAsDocumentsList(config.getEnvVariables()));
        final MachineSource source = config.getSource();
        if (source != null) {
            document.append("source", new Document(MACHINE_SOURCE_TYPE, source.getType())
                    .append(MACHINE_SOURCE_LOCATION, source.getLocation())
                    .append(MACHINE_SOURCE_CONTENT, source.getContent()));
        }
        final Limits limits = config.getLimits();
        if (limits != null) {
            document.append("limits", new Document("ram", limits.getRam()));
        }

        document.append("servers", config.getServers()
                                         .stream()
                                         .map(server -> new Document().append("ref", server.getRef())
                                                                      .append("port", server.getPort())
                                                                      .append("protocol", server.getProtocol())
                                                                      .append("path", server.getPath()))
                                         .collect(toList()));

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
        final List<Document> envDocuments = (List<Document>)document.get("environments");
        final List<EnvironmentImpl> environments = envDocuments.stream()
                                                               .map(WorkspaceImplCodec::asEnvironment)
                                                               .collect(toList());

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
                                                 .stream()
                                                 .map(WorkspaceImplCodec::asDocument)
                                                 .collect(toList()));
        return document;
    }
}
