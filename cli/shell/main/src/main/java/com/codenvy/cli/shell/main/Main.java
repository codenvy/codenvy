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
package com.codenvy.cli.shell.main;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.lang.reflect.Method;

/**
 * Class used to load the main class and also reset the colors
 * @author Florent Benoit
 */
public class Main {

    public static final String KARAF_CLASS = "org.apache.karaf.shell.console.impl.Main";

    public static void main(String[] args) throws Exception {

        // load the main class
        Class karafMainClass = Main.class.getClassLoader().loadClass(KARAF_CLASS);

        // invoke main
        Method mainMethod = karafMainClass.getMethod("main", String[].class);
        mainMethod.invoke(null, new Object[] {args});

        // reset the colors by using Ansi reset
        try {
            AnsiConsole.systemInstall();
            System.out.print(Ansi.ansi().reset());
            System.out.flush();
        } finally {
            AnsiConsole.systemUninstall();
        }


    }


}
