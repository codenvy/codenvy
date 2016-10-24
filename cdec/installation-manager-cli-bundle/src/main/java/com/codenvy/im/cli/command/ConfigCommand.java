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
package com.codenvy.im.cli.command;


import com.codenvy.im.artifacts.Artifact;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.artifacts.InstallManagerArtifact;
import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.PropertyNotFoundException;
import com.codenvy.im.response.BasicResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.eclipse.che.commons.json.JsonParseException;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import static com.codenvy.im.artifacts.ArtifactFactory.createArtifact;
import static com.google.api.client.repackaged.com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.fusesource.jansi.Ansi.Color.DEFAULT;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.WHITE;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
@Command(scope = "codenvy", name = "config", description = "Configure Codenvy on-prem")
public class ConfigCommand extends AbstractIMCommand {

    @Argument(index = 0, name = "property", description = "Codenvy property name", required = false, multiValued = false)
    private String property;

    @Argument(index = 1, name = "value", description = "Codenvy property value", required = false, multiValued = false)
    private String value;

    @Option(name = "--hostname", description = "New Codenvy hostname", required = false)
    private String hostname;

    @Option(name = "--im-cli", description = "To get installation manager CLI properties", required = false)
    private boolean imCli;

    private Artifact cdecArtifact;

    /** {@inheritDoc} */
    @Override
    protected void doExecuteCommand() throws Exception {
        cdecArtifact = createArtifact(CDECArtifact.NAME);

        if (imCli) {
            doGetConfig(createArtifact(InstallManagerArtifact.NAME));
            return;
        }

        if (!isNullOrEmpty(hostname)) {
            doUpdateCodenvyHostUrl();
            return;
        }

        if (!StringUtils.isEmpty(property)) {
            if (value != null) {
                doUpdateCdecConfigProperty(property, value);
                return;
            }

            doGetCdecConfigProperty(property);
            return;
        }

        doGetConfig(cdecArtifact);
    }

    private void doGetCdecConfigProperty(String property) throws IOException, JsonParseException {
        Map<String, String> properties = getFacade().getArtifactConfig(cdecArtifact);
        if (properties.containsKey(property)) {
            printProperties(ImmutableMap.of(property, properties.get(property)));
        } else {
            throw PropertyNotFoundException.from(property);
        }
    }

    private void doUpdateCdecConfigProperty(String property, String value) throws IOException, JsonParseException {
        String messageToConfirm = format("Do you want to update Codenvy property '%s' with new value '%s'?", property, value);
        if (! getConsole().askUser(messageToConfirm)) {
            return;
        }

        Map<String, String> properties = getFacade().getArtifactConfig(cdecArtifact);
        if (!properties.containsKey(property)) {
            throw PropertyNotFoundException.from(property);
        }

        getConsole().showProgressor();

        try {
            getFacade().updateArtifactConfig(cdecArtifact, ImmutableMap.of(property, value));
        } finally {
            getConsole().hideProgressor();
        }
    }

    private void doUpdateCodenvyHostUrl() throws IOException, JsonParseException {
        getConsole().showProgressor();
        try {
            getFacade().updateArtifactConfig(cdecArtifact, ImmutableMap.of(Config.HOST_URL, hostname));
            getConsole().printResponseExitInError(BasicResponse.ok());
        } finally {
            getConsole().hideProgressor();
        }
    }

    private void doGetConfig(Artifact artifact) throws JsonParseException, IOException {
        Map<String, String> sortedProps = new TreeMap<>(getFacade().getArtifactConfig(artifact));
        printProperties(sortedProps);
    }

    private void printProperties(Map<String, String> propertiesToDisplay) throws JsonParseException, JsonProcessingException {
        propertiesToDisplay.forEach((key, value) -> getConsole().printlnWithoutPrompt(ansi().fg(GREEN).a(key)
                                                                                       .fg(WHITE).a("=")
                                                                                       .fg(DEFAULT).a(value)
                                                                                       .reset())
        );
    }
}
