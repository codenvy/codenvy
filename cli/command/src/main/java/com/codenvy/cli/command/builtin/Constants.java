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
package com.codenvy.cli.command.builtin;

import java.io.File;

/**
 * Define some constants used in the commands.
 *
 * @author Florent Benoit
 * @author Stéphane Daviet
 */
public class Constants {

    /**
     * CLI metadata filename
     */
    public static final String METADATA_FILENAME = "cli";

    /**
     * Codenvy foldername.
     */
    public static final String CODENVY_FOLDERNAME = ".codenvy";

    /**
     * Preferences folder
     */
    public static final String PREFERENCES_FOLDER = System.getProperty("user.home") + File.separator + ".codenvy";

    /**
     * Default preferences resource name
     */
    public static final String DEFAULT_PREFERENCES_RESOURCENAME = "/" + Constants.class.getPackage().getName().replace(".", "/") + "/" + "default-preferences.json";

    /**
     * Default template factory
     */
    public static final String TEMPLATE_PROJECT_FACTORY = "/" + Constants.class.getPackage().getName().replace(".", "/") + "/" + "template-project-factory.json";

    /**
     * Path to the configuration file of Codenvy.
     */
    public static final String PREFERENCES_STORE_FILE = PREFERENCES_FOLDER + File.separator + "preferences.json";

    /**
     * Default project type for creating projects.
     */
    public static final String DEFAULT_CREATE_PROJECT_TYPE = "blank";
}
