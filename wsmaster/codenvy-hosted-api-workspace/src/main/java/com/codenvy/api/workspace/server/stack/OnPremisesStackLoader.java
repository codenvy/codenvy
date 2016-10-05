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
package com.codenvy.api.workspace.server.stack;

import com.codenvy.api.workspace.server.spi.jpa.JpaStackPermissionsDao;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.stack.image.StackIcon;
import org.eclipse.che.api.workspace.shared.stack.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

/**
 * Class for loading list predefined {@link Stack} to the {@link StackDao}
 * and set {@link StackIcon} to the predefined stack, with public permissions.
 *
 * @author Anton Korneta.
 */
@Singleton
public class OnPremisesStackLoader {

    private static final Logger LOG = LoggerFactory.getLogger(OnPremisesStackLoader.class);

    private final Gson GSON;

    private final Path                   stackJsonPath;
    private final Path                   stackIconFolderPath;
    private final StackDao               stackDao;
    private final JpaStackPermissionsDao permissionsDao;

    @Inject
    public OnPremisesStackLoader(@Named("che.stacks.storage") String stacksPath,
                                 @Named("che.stacks.images") String stackIconFolder,
                                 JpaStackPermissionsDao permissionsDao,
                                 StackDao stackDao) {

        this.stackJsonPath = Paths.get(stacksPath);
        this.stackIconFolderPath = Paths.get(stackIconFolder);
        this.permissionsDao = permissionsDao;
        this.stackDao = stackDao;
        GSON = new GsonBuilder().create();
    }

    /**
     * Load predefined stacks with their icons to the {@link StackDao}.
     */
    @PostConstruct
    public void start() {
        if (Files.exists(stackJsonPath) && Files.isRegularFile(stackJsonPath)) {
            try (BufferedReader reader = Files.newBufferedReader(stackJsonPath)) {
                List<StackImpl> stacks = GSON.fromJson(reader, new TypeToken<List<StackImpl>>() {}.getType());
                stacks.forEach(this::loadStack);
            } catch (Exception e) {
                LOG.error("Failed to store stacks ", e);
            }
        }
    }

    private void doCreate(StackImpl stack) throws ConflictException, ServerException {
        try {
            stackDao.update(stack);
        } catch (NotFoundException e) {
            stackDao.create(stack);
        }
    }

    private void loadStack(StackImpl stack) {
        setIconData(stack, stackIconFolderPath);
        try {
            doCreate(stack);
            permissionsDao.store(new StackPermissionsImpl("*",
                                                          stack.getId(),
                                                          singletonList("search")));
        } catch (ServerException | ConflictException ex) {
            LOG.warn(format("Failed to load stack with id '%s' ", stack.getId()), ex.getMessage());
        }
    }

    private void setIconData(StackImpl stack, Path stackIconFolderPath) {
        StackIcon stackIcon = stack.getStackIcon();
        if (stackIcon == null) {
            return;
        }
        try {
            Path stackIconPath = stackIconFolderPath.resolve(stackIcon.getName());
            if (Files.exists(stackIconPath) && Files.isRegularFile(stackIconPath)) {
                stackIcon = new StackIcon(stackIcon.getName(), stackIcon.getMediaType(), Files.readAllBytes(stackIconPath));
                stack.setStackIcon(stackIcon);
            } else {
                throw new IOException("Stack icon is not a file or doesn't exist by path: " + stackIconPath);
            }
        } catch (IOException e) {
            stack.setStackIcon(null);
            LOG.error(format("Failed to load stack icon data for the stack with id '%s'", stack.getId()), e);
        }
    }
}
