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
package com.codenvy.api.permission.server.dao;

import com.codenvy.api.permission.server.model.impl.AbstractPermissions;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.List;

/**
 * Encodes & decodes {@link AbstractPermissions}.
 *
 * @author Sergii Leschenko
 */
public class PermissionsImplCodec implements Codec<AbstractPermissions> {

    private Codec<Document> codec;

    public PermissionsImplCodec(CodecRegistry registry) {
        codec = registry.get(Document.class);
    }

    @Override
    public AbstractPermissions decode(BsonReader reader, DecoderContext decoderContext) {
        final Document document = codec.decode(reader, decoderContext);

        @SuppressWarnings("unchecked") // 'actions' fields is aways list
        final List<String> actions = (List<String>)document.get("actions");

        return new AbstractPermissions(document.getString("user"),
                                   document.getString("domain"),
                                   document.getString("instance"),
                                   actions);
    }

    @Override
    public void encode(BsonWriter writer, AbstractPermissions permissions, EncoderContext encoderContext) {
        final Document document = new Document().append("user", permissions.getUserId())
                                                .append("domain", permissions.getDomainId())
                                                .append("instance", permissions.getInstanceId())
                                                .append("actions", permissions.getActions());

        codec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<AbstractPermissions> getEncoderClass() {
        return AbstractPermissions.class;
    }
}
