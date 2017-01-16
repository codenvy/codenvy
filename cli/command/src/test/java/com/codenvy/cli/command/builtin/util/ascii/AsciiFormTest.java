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

import org.fusesource.jansi.Ansi;
import org.testng.annotations.Test;

import static java.lang.String.format;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD_OFF;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Florent Benoit
 */
public class AsciiFormTest {

    @Test
    public void testEmptyForm() {
        AsciiForm asciiForm = new DefaultAsciiForm();
        String result = asciiForm.toAscii();
        assertTrue(result.length() == 0);
    }

    @Test
    public void testOneLineForm() {
        AsciiForm asciiForm = new DefaultAsciiForm().withEntry("id", "value1");
        String result = asciiForm.toAscii();
        assertTrue(result.length() > 0);

        assertEquals(result, format("id value1%n"));
    }

    @Test
    public void testThreeLinesFormUppercase() {
        AsciiForm asciiForm = new DefaultAsciiForm().withEntry("id", "value1").withEntry("a very long Id", "123456789").withEntry("short id", "abc").withUppercasePropertyName();
        String result = asciiForm.toAscii();
        assertTrue(result.length() > 0);

        assertEquals(result, format("ID             value1%n" +
                                    "A VERY LONG ID 123456789%n" +
                                    "SHORT ID       abc%n"));
    }

    protected String bold(String name) {
        return Ansi.ansi().a(INTENSITY_BOLD).a(name).a(INTENSITY_BOLD_OFF).toString();
    }

    @Test
    public void testAnsiThreeLines() {

        AsciiForm asciiForm = new DefaultAsciiForm().withEntry(bold("id"), "value1").withEntry(bold("a very long Id"), "123456789").withEntry(bold("short id"), "abc").withUppercasePropertyName();
        String result = asciiForm.toAscii();
        assertTrue(result.length() > 0);
        assertEquals(result, format("\u001B[1mID\u001B[22m             value1%n" +
                                    "\u001B[1mA VERY LONG ID\u001B[22m 123456789%n" +
                                    "\u001B[1mSHORT ID\u001B[22m       abc%n"));
    }


}
