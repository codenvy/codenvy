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

import java.util.Locale;

import static com.codenvy.cli.command.builtin.util.ascii.AnsiHelper.removeAnsi;

/**
 * @author Florent Benoit
 */
public class ModernFormatter implements AsciiFormatter {


    /**
     * No border for modern formatter
     * @param asciiArrayInfo
     * @return empty
     */
    @Override
    public String getBorderLine(AsciiArrayInfo asciiArrayInfo) {
        return null;
    }

    @Override
    public String getFormatter(AsciiArrayInfo asciiArrayInfo) {
        StringBuilder buffer = new StringBuilder();

        for (Integer columnSize : asciiArrayInfo.getColumnsSize()) {
            buffer.append("%-" + columnSize + "s");
            buffer.append("  ");
        }
        buffer.append("%n");

        return buffer.toString();

    }

    public String getTitleFormatter(AsciiArrayInfo asciiArrayInfo) {
        StringBuilder buffer = new StringBuilder();

        for (Integer columnSize : asciiArrayInfo.getColumnsSize()) {
            // uppercase
            buffer.append("%-" + columnSize + "S");
            buffer.append("  ");
        }
        buffer.append("%n");

        return buffer.toString();

    }

    @Override
    public String formatFormTitle(String name, AsciiFormInfo asciiFormInfo) {
        // ok we are expecting to have full form rendering :
        // ENTRY1:........value1
        // ANOTHER ENTRY:.value2

        String withoutAnsi = removeAnsi(name);
        String entryName = name;//.concat(":");

        // format it
        String line = String.format("%-" + (asciiFormInfo.getTitleColumnSize()) + "s", withoutAnsi);

        // replace value by the the ansi version
        if (asciiFormInfo.isUppercasePropertyName()) {
            // replace word by ansi line
            line = line.replace(withoutAnsi, entryName);

            // replace lowercase by uppercase
            return line.replace(withoutAnsi, withoutAnsi.toUpperCase(Locale.getDefault()));
        } else {
            return line.replace(withoutAnsi, entryName);
        }

    }

    @Override
    public String formatFormValue(String value, AsciiFormInfo asciiFormInfo) {
        if (value == null) {
            return " ";
        }

        // just adding text after adding a space

        return " ".concat(value);
    }


}