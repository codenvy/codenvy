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
 * @author Florent Benoit
 */
public interface AsciiArray {
    /**
     * Specify the titles for this array
     *
     * @param titles
     *         the given titles
     * @return the current array
     */
    AsciiArray withTitle(List<String> titles);

    /**
     * Specify the titles for this array
     *
     * @param columnsTitle
     *         the given titles
     * @return the current array
     */
    AsciiArray withTitle(String... columnsTitle);

    /**
     * Specify the columns (containing data) for this array
     *
     * @param columns
     *         the given data column
     * @return the current array
     */
    AsciiArray withColumns(List<String>... columns);

    /**
     * Specify the columns (containing data) for this array
     *
     * @param columns
     *         the given data column
     * @return the current array
     */
    AsciiArray withColumns(String[]... columns);

    /**
     * Transform the given data into an ascii array
     *
     * @return stringified table of the array
     */
    String toAscii();

    AsciiArray withFormatter(FormatterMode formatterMode);
}
