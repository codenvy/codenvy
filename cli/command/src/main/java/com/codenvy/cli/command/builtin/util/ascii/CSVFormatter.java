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

/**
 * CSV Formatter
 * @author Florent Benoit
 */
public class CSVFormatter implements AsciiFormatter {


    /**
     * No border for CSV formatter
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
        int size = asciiArrayInfo.getColumnsSize().size();
        for (int c = 1; c <= size; c++) {
            buffer.append("%s");
            if (c < size ) {
                buffer.append(",");
            }
        }
        buffer.append("%n");

        return buffer.toString();

    }

    @Override
    public String getTitleFormatter(AsciiArrayInfo asciiArrayInfo) {
        StringBuilder buffer = new StringBuilder();
        int size = asciiArrayInfo.getColumnsSize().size();
        for (int c = 1; c <= size; c++) {
            // uppercase
            buffer.append("%S");
            if (c < size ) {
                buffer.append(",");
            }
        }
        buffer.append("%n");

        return buffer.toString();

    }

    @Override
    public String formatFormTitle(String name, AsciiFormInfo asciiFormInfo) {
        return null;
    }

    @Override
    public String formatFormValue(String value, AsciiFormInfo asciiFormInfo) {
        return null;
    }

}