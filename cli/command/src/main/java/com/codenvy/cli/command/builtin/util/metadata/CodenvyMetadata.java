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
package com.codenvy.cli.command.builtin.util.metadata;

import com.codenvy.cli.command.builtin.Constants;
import com.codenvy.cli.command.builtin.model.UserProjectReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Properties;

import static com.codenvy.cli.command.builtin.Constants.METADATA_FILENAME;

/**
 * This class is managing the codenvy metadata for a given directory
 *
 * @author Florent Benoit
 */
public class CodenvyMetadata {

    private static final String PROJECT_ID = "project-id";
    private String projectId;
    private File   metadataFile;

    private Properties properties;


    protected void init(File projectFolder) throws IOException {
        // Codenvy Folder
        File codenvyFolder = new File(projectFolder, Constants.CODENVY_FOLDERNAME);
        if (!codenvyFolder.exists()) {
            if (!codenvyFolder.mkdirs()) {
                throw new IllegalStateException("Unable to create Codenvy metadata folder in the directory" + projectFolder);
            }
        }

        this.metadataFile = new File(codenvyFolder, METADATA_FILENAME);
        loadProperties();
    }


    public CodenvyMetadata(File projectFolder) throws IOException {
        init(projectFolder);

        this.projectId = properties.getProperty(PROJECT_ID);
        if (projectId == null) {
            throw new IllegalArgumentException("Unable to detect codenvy project in the directory '" + projectFolder + "'.");
        }

    }

    public CodenvyMetadata(UserProjectReference project, File projectFolder) throws IOException {
        init(projectFolder);
        this.projectId = project.shortId();
        properties.put(PROJECT_ID, projectId);
    }

    protected void loadProperties() throws IOException {
        this.properties = new Properties();

        if (this.metadataFile.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(this.metadataFile), Charset.forName("UTF-8"))) {
                this.properties.load(reader);
            }
        }

    }


    public void write() throws IOException {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(metadataFile), Charset.forName("UTF-8"))) {
            properties.store(writer, "CLI checkout settings");
        }
    }

    public String getProjectId() {
        return projectId;
    }


}
