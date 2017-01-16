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
package com.codenvy.cli.command.builtin.util.ascii;

import org.fusesource.jansi.AnsiOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Florent Benoit
 */
public class AnsiHelper {

    public static String removeAnsi(final String content) {
        if (content == null) {
            return null;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); AnsiOutputStream aos = new AnsiOutputStream(baos)) {
            aos.write(content.getBytes(Charset.defaultCharset()));
            aos.flush();
            return baos.toString("UTF-8");
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to remove ansi", e);
        }
    }
}
