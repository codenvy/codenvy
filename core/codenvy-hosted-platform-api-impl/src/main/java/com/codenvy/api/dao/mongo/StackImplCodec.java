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
import org.bson.types.Binary;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.machine.server.recipe.GroupImpl;
import org.eclipse.che.api.machine.server.recipe.PermissionsImpl;
import org.eclipse.che.api.machine.shared.Group;
import org.eclipse.che.api.machine.shared.Permissions;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackSourceImpl;
import org.eclipse.che.api.workspace.server.stack.image.StackIcon;
import org.eclipse.che.api.workspace.shared.stack.StackComponent;
import org.eclipse.che.api.workspace.shared.stack.StackSource;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.codenvy.api.dao.mongo.MongoUtil.asDBList;
import static com.codenvy.api.dao.mongo.MongoUtil.mapAsDocumentsList;
import static java.util.stream.Collectors.toList;

/**
 * Encodes and decodes {@link StackImpl}
 *
 * @author Alexander Andrienko
 */
public class StackImplCodec implements Codec<StackImpl> {

    private Codec<Document> codec;

    public StackImplCodec(CodecRegistry registry) {
        this.codec = registry.get(Document.class);
    }

    @Override
    public StackImpl decode(BsonReader reader, DecoderContext decoderContext) {
        final Document document = codec.decode(reader, decoderContext);

        @SuppressWarnings("unchecked")//tags is always String list
        List<String> tags = (List<String>)document.get("tags");

        WorkspaceConfig workspaceConfig = null;
        Document workspaceDocument = (Document)document.get("workspaceConfig");
        if (workspaceDocument != null) {
            workspaceConfig = WorkspaceImplCodec.asWorkspaceConfig(workspaceDocument);
        }

        StackSource source = null;
        Document sourceDocument = (Document)document.get("source");
        if (sourceDocument != null) {
            source = asStackSource(sourceDocument);
        }

        StackIcon stackIcon = null;
        Document iconDocument = (Document)document.get("stackIcon");
        if (iconDocument != null) {
            try {
                stackIcon = asStackIcon(iconDocument);
            } catch (IOException | ConflictException e) {
                //do nothing
            }
        }

        Document permissionDocument = (Document)document.get("permissions");
        Permissions permissions = null;
        if (permissionDocument != null) {
            permissions = asPermissions(permissionDocument);
        }

        @SuppressWarnings("unchecked")//components is always list
        List<Document> componentDocument = (List<Document>)document.get("components");
        List<StackComponent> components = componentDocument.stream()
                                                           .map(StackImplCodec::asStackComponent)
                                                           .collect(toList());

        return StackImpl.builder()
                        .setId(document.getString("_id"))
                        .setName(document.getString("name"))
                        .setDescription(document.getString("description"))
                        .setScope(document.getString("scope"))
                        .setCreator(document.getString("creator"))
                        .setTags(tags)
                        .setWorkspaceConfig(workspaceConfig)
                        .setSource(source)
                        .setComponents(components)
                        .setStackIcon(stackIcon)
                        .setPermissions(permissions)
                        .build();
    }

    @Override
    public void encode(BsonWriter writer, StackImpl stack, EncoderContext encoderContext) {
        final Document document = new Document().append("_id", stack.getId())
                                                .append("name", stack.getName())
                                                .append("description", stack.getDescription())
                                                .append("scope", stack.getScope())
                                                .append("creator", stack.getCreator())
                                                .append("tags", stack.getTags());

        WorkspaceConfigImpl workspaceConfig = stack.getWorkspaceConfig();
        if (workspaceConfig != null) {
            Document workspaceConfigDocument = WorkspaceImplCodec.asDocument(new WorkspaceConfigImpl(workspaceConfig));
            document.append("workspaceConfig", workspaceConfigDocument);
        }

        StackSource stackSource = stack.getSource();
        if (stackSource != null) {
            document.append("source", asDocument(stackSource));
        }

        document.append("components", stack.getComponents()
                                           .stream()
                                           .map(StackImplCodec::asDocument)
                                           .collect(toList()));

        StackIcon stackIcon = stack.getStackIcon();
        if (stackIcon != null) {
            document.append("stackIcon", asDocument(stackIcon));
        }

        Permissions permissions = stack.getPermissions();
        if (permissions != null) {
            document.append("permissions", asDocument(permissions));
        }

        codec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<StackImpl> getEncoderClass() {
        return StackImpl.class;
    }

    private static StackIcon asStackIcon(Document document) throws IOException, ConflictException {
        return new StackIcon(document.getString("name"), document.getString("mediaType"), ((Binary)document.get("data")).getData());
    }

    private static Document asDocument(StackIcon stackIcon) {
        return new Document().append("name", stackIcon.getName())
                             .append("mediaType", stackIcon.getMediaType())
                             .append("data", stackIcon.getData());
    }

    private static Document asDocument(StackComponent component) {
        return new Document().append("name", component.getName()).append("version", component.getVersion());
    }

    private static StackComponent asStackComponent(Document document) {
        return new StackComponentImpl(document.getString("name"), document.getString("version"));
    }

    private static Document asDocument(StackSource source) {
        return new Document().append("type", source.getType()).append("origin", source.getOrigin());
    }

    private static StackSource asStackSource(Document document) {
        return new StackSourceImpl(document.getString("type"), document.getString("origin"));
    }

    @SuppressWarnings("unchecked")
    private static Permissions asPermissions(Document document) {
        Map<String, List<String>> users = new HashMap<>();
        for (Document userDocument : (List<Document>)document.get("users")) {
            String key = userDocument.getString("name");
            List<String> value = (List<String>)userDocument.get("value");
            users.put(key, value);
        }

        List<Document> groupDocuments = (List<Document>)document.get("groups");
        List<Group> groups = groupDocuments.stream().map(StackImplCodec::asGroup).collect(Collectors.toList());

        return new PermissionsImpl(users, groups);
    }

    private static Document asDocument(Permissions permissions) {
        List<Document> groups = permissions.getGroups().stream().map(StackImplCodec::asDocument).collect(toList());
        return new Document().append("groups", groups)
                             .append("users", mapAsDocumentsList(permissions.getUsers()));
    }

    private static Group asGroup(Document document) {
        @SuppressWarnings("unchecked")//acl is always String list
        List<String> acls = document.get("acl") == null ? Collections.emptyList() : (List<String>)document.get("acl");
        return new GroupImpl(document.getString("name"), document.getString("unit"), acls);
    }

    private static Document asDocument(Group group) {
        return new Document().append("name", group.getName())
                             .append("unit", group.getUnit())
                             .append("acl", asDBList(group.getAcl()));
    }
}
