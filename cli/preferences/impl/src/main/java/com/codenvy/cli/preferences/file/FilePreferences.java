/*******************************************************************************
 * Copyright (c) [2012] - [2017] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.cli.preferences.file;

import com.codenvy.cli.preferences.Preferences;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

/**
 * Entry point to globally manage all cli preferences. Load a preferences file in JSON format in memory and dump it when preferences are
 * modified. This class store a inner {@link Preferences} root node and act as an adapter to it, decorating it with its specific file
 * persisting action when needed.
 *
 * @author Stéphane Daviet
 */
public class FilePreferences implements Preferences, LifecycleCallback {
    private JsonPreferences    rootNode;

    private final ObjectMapper mapper;

    private final File         preferencesFile;

    /**
     * Create a {@link FilePreferences} linked to the specified {@link File}.
     *
     * @param preferencesFile the {@link File} to link to this new {@link FilePreferences} instance.
     */
    public FilePreferences(File preferencesFile) {
        this.mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.preferencesFile = preferencesFile;
        loadFile();
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        return rootNode.get(key, clazz);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Write the file back on the file system./
     * <p>
     */
    @Override
    public void put(String key, Object value) {
        rootNode.put(key, value);
        dumpFile();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Write the file back on the file system./
     * <p>
     */
    @Override
    public void merge(String key, Object value) {
        rootNode.merge(key, value);
        dumpFile();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Write the file back on the file system./
     * <p>
     */
    @Override
    public void delete(String key) {
        rootNode.delete(key);
        dumpFile();
    }

    /**
     * {@inheritDoc}
     * <p>
     * As a void preferences node is not significant, choice has been made to not write back on the file system.
     * </p>
     */
    @Override
    public Preferences path(String key) {
        return rootNode.path(key);
    }

    @Override
    public Preferences walk(String key) {
        return rootNode.walk(key);
    }

    @Override
    public boolean pathExists(String key) {
        return rootNode.pathExists(key);
    }

    /**
     * Load the file where preferences are stored.
     *
     * @return the current {@link FilePreferences} to chain commands.
     */
    protected FilePreferences loadFile() {
        try {
            if (!preferencesFile.exists()) {
                if (!preferencesFile.createNewFile()) {
                    throw new RuntimeException("Can not create file.");
                }
                this.rootNode = new JsonPreferences();
            } else {
                this.rootNode = mapper.readValue(preferencesFile, JsonPreferences.class);
            }
        } catch (JsonMappingException e) {
            this.rootNode = new JsonPreferences();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.rootNode.addCallback(this);
        return this;
    }

    /**
     * Dump the in-memory cache into the file.
     *
     * @return the current {@link FilePreferences} to chain commands.
     */
    protected FilePreferences dumpFile() {
        synchronized (preferencesFile) {
            try {
                mapper.writeValue(preferencesFile, rootNode);
                return this;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void notify(LifecycleEvent lifecycleEvent) {
        // we dump the file for each event
        if (!disableSaveOnChanges) {
            dumpFile();
        }
    }

    private boolean disableSaveOnChanges = false;

    public FilePreferences setDisableSaveOnChanges() {
        disableSaveOnChanges = true;
        return this;
    }

}
