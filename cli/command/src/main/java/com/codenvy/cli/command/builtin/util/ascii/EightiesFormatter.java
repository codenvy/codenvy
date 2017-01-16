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

import java.util.List;

/**
 * Eighties formatter with +---+ columns
 * @author Florent Benoit
 */
public class EightiesFormatter implements AsciiFormatter {


    @Override
    public String getBorderLine(AsciiArrayInfo asciiArrayInfo) {
        List<Integer> columnsSize = asciiArrayInfo.getColumnsSize();

        StringBuilder buffer = new StringBuilder("+");
        for (int i = 0; i < columnsSize.size(); i++) {
            for (int repeat = 0; repeat < columnsSize.get(i); repeat++) {
                buffer.append("-");
            }
            buffer.append("+");
        }
        return buffer.toString();
    }

    @Override
    public String getFormatter(AsciiArrayInfo asciiArrayInfo) {
        StringBuilder buffer = new StringBuilder("|");

        for (Integer columnSize : asciiArrayInfo.getColumnsSize()) {
            buffer.append("%" + columnSize + "s");
            buffer.append("|");
        }
        buffer.append("%n");

        return buffer.toString();

    }

    @Override
    public String getTitleFormatter(AsciiArrayInfo asciiArrayInfo) {
        StringBuilder buffer = new StringBuilder("|");

        for (Integer columnSize : asciiArrayInfo.getColumnsSize()) {
            // uppercase
            buffer.append("%" + columnSize + "S");
            buffer.append("|");
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
