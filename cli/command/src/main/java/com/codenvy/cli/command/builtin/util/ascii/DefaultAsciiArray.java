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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.codenvy.cli.command.builtin.util.ascii.FormatterMode.CSV;
import static com.codenvy.cli.command.builtin.util.ascii.FormatterMode.EIGHTIES;
import static com.codenvy.cli.command.builtin.util.ascii.FormatterMode.MODERN;

/**
 * This utility class is for allowing to get a nice ascii table from multi dimensional array
 *
 * @author Florent Benoit
 */
public class DefaultAsciiArray implements AsciiArray {

    /**
     * Columns of this array
     */
    private List<List<String>> columns;

    /**
     * Titles of each column (if any)
     */
    private List<String> titles;

    /**
     * Left Mode.
     */


    /**
     * Formatters.
     */
    private Map<FormatterMode, AsciiFormatter> formatters;

    /**
     * Formatter
     */
    private FormatterMode formatterMode;

    /**
     * Default constructor
     */
    public DefaultAsciiArray() {
        this.columns = new ArrayList<>();
        this.formatters = new HashMap<>();
        formatters.put(EIGHTIES, new EightiesFormatter());
        formatters.put(MODERN, new ModernFormatter());
        formatters.put(CSV, new CSVFormatter());

        this.formatterMode = EIGHTIES;
    }

    /**
     * Specify the titles for this array
     *
     * @param titles
     *         the given titles
     * @return the current array
     */
    @Override
    public DefaultAsciiArray withTitle(List<String> titles) {
        this.titles = titles;
        return this;
    }

    /**
     * Specify the titles for this array
     *
     * @param columnsTitle
     *         the given titles
     * @return the current array
     */
    @Override
    public DefaultAsciiArray withTitle(String... columnsTitle) {
        this.titles = Arrays.asList(columnsTitle);
        return this;
    }

    /**
     * Specify the columns (containing data) for this array
     *
     * @param columns
     *         the given data column
     * @return the current array
     */
    @Override
    public DefaultAsciiArray withColumns(List<String>... columns) {
        for (List<String> column : columns) {
            addColumn(column);
        }
        return this;
    }

    /**
     * Specify the columns (containing data) for this array
     *
     * @param columns
     *         the given data column
     * @return the current array
     */
    @Override
    public AsciiArray withColumns(String[]... columns) {
        for (String[] column : columns) {
            addColumn(Arrays.asList(column));
        }
        return this;
    }

    /**
     * Add the given column
     *
     * @param column
     *         the column
     */
    protected void addColumn(List<String> column) {
        this.columns.add(column);
    }

    /**
     * Override the current method and return ascii representation
     *
     * @return ascii table.
     */
    @Override
    public String toString() {
        return toAscii();
    }


    /**
     * Transform the given data into an ascii array
     *
     * @return stringified table of the array
     */
    @Override
    public String toAscii() {

        // check
        checkIntegrity();

        // handle empty
        if (columns.isEmpty()) {
            if (titles != null && titles.isEmpty()) {
                return "";
            }
            if (titles == null) {
                return "";
            }
        }


        // first line is the border
        String computeBorder = getBorderLine();
        String beginBorderLine;
        String endBorderLine;
        if (computeBorder != null) {
            beginBorderLine = new StringBuilder(computeBorder).append("\n").toString();
            endBorderLine = computeBorder;
        } else {
            beginBorderLine = "";
            endBorderLine = "";
        }

        StringBuilder buffer = new StringBuilder(beginBorderLine);
        String formatter = getFormatter();
        String titleFormatter = getTitleFormatter();

        // now add title
        if (titles != null) {
            buffer.append(String.format(titleFormatter, (Object[]) titles.toArray(new String[titles.size()])));
            buffer.append(beginBorderLine);
        }

        // data ?
        if (!columns.isEmpty()) {
            int nbRows = columns.get(0).size();
            for (int row = 0; row < nbRows; row++) {
                buffer.append(String.format(formatter, (Object[]) getRow(row)));
            }
            buffer.append(endBorderLine);
        }

        return buffer.toString();

    }

    /**
     * Get content of a selected row for the given array
     *
     * @param index
     *         the index in the columns
     * @return the content
     */
    protected String[] getRow(int index) {
        String[] row = new String[columns.size()];
        int i = 0;
        for (List<String> column : columns) {
            row[i++] = column.get(index);
        }

        return row;
    }

    /**
     * @return formatter
     */
    protected AsciiFormatter getFormatterMode() {
        return formatters.get(formatterMode);
    }

    protected AsciiArrayInfo getArrayInfo() {
        final DefaultAsciiArray array = this;

        return new MyAsciiArrayInfo(array);
    }

    /**
     * @return formatter used to format row content
     */
    protected String getFormatter() {
        return getFormatterMode().getFormatter(getArrayInfo());
    }

    /**
     * @return formatter used to format title content
     */
    protected String getTitleFormatter() {
        return getFormatterMode().getTitleFormatter(getArrayInfo());
    }


    /**
     * @return value used as border of the array
     */
    protected String getBorderLine() {
        return getFormatterMode().getBorderLine(getArrayInfo());
    }


    /**
     * @return the size of the column (by searching max size of each column, including title)
     */
    protected List<Integer> getColumnsSize() {
        List<Integer> lengths = new ArrayList<>();

        for (int column = 0; column < columns.size(); column++) {
            // for each column, set the max length
            int maxLength = 0;

            // for title
            if (titles != null) {
                int currentLength = titles.get(column).length();
                if (currentLength > maxLength) {
                    maxLength = currentLength;
                }
            }

            // for content
            List<String> columnData = columns.get(column);
            for (String row : columnData) {
                if (row == null) {
                    continue;
                }
                int currentLength = row.length();
                if (currentLength > maxLength) {
                    maxLength = currentLength;
                }
            }
            lengths.add(maxLength);
        }

        return lengths;

    }

    /**
     * Checks that the array is valid before trying to get its stringified version
     */
    protected void checkIntegrity() {
        // check that columns have the same row length
        if (!columns.isEmpty()) {
            int size = columns.get(0).size();
            Iterator<List<String>> it = columns.iterator();
            while (it.hasNext()) {
                List<String> currentColumn = it.next();
                if (currentColumn.size() != size) {
                    throw new IllegalArgumentException("The columns have not the same sized. : " + currentColumn.size() + " vs " + size);
                }
            }
        }

        // if there are titles check that we've the same number of columns
        if (titles != null && !titles.isEmpty()) {
            if (!columns.isEmpty()) {
                if (titles.size() != columns.size()) {
                    throw new IllegalArgumentException(
                            "Invalid expected titles. There are " + columns.size() + " while there are " + titles.size() + " titles.");
                }
            }
        }

    }

    @Override
    public AsciiArray withFormatter(FormatterMode formatterMode) {
        this.formatterMode = formatterMode;
        return this;
    }


    private static class MyAsciiArrayInfo implements AsciiArrayInfo {
        private final DefaultAsciiArray array;

        public MyAsciiArrayInfo(DefaultAsciiArray array) {
            this.array = array;
        }

        @Override
        public List<Integer> getColumnsSize() {
            return array.getColumnsSize();
        }
    }
}
