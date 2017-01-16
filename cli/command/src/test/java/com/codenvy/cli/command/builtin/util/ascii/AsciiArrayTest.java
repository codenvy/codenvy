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

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static com.codenvy.cli.command.builtin.util.ascii.FormatterMode.CSV;
import static com.codenvy.cli.command.builtin.util.ascii.FormatterMode.MODERN;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Florent Benoit
 */
public class AsciiArrayTest {

    @Test
    public void testEmptyArray() {
        AsciiArray asciiArray = new DefaultAsciiArray();
        String result = asciiArray.toAscii();
        assertTrue(result.length() == 0);
    }

    @Test
    public void testColumnsSize() {
        List<String> column1 = Arrays.asList("a", "ab", "abc", "a");
        DefaultAsciiArray asciiArray = new DefaultAsciiArray().withColumns(column1);
        List<Integer> columnsSize = asciiArray.getColumnsSize();
        assertNotNull(columnsSize);
        assertEquals(columnsSize.size(), 1);
        assertEquals(columnsSize.get(0), Integer.valueOf(3));
    }

    @Test
    public void testColumnsSizeTwoColumns() {
        List<String> column1 = Arrays.asList("a", "ab", "abcdef", " abcdef ");
        List<String> column2 = Arrays.asList("defgh", "d", "e", "f");
        DefaultAsciiArray asciiArray = new DefaultAsciiArray().withColumns(column1, column2);
        List<Integer> columnsSize = asciiArray.getColumnsSize();
        assertNotNull(columnsSize);
        assertEquals(columnsSize.size(), 2);
        assertEquals(columnsSize.get(0), Integer.valueOf(8));
        assertEquals(columnsSize.get(1), Integer.valueOf(5));
    }

    @Test
    public void testColumnsSizeWihTitle() {
        List<String> titles = Arrays.asList("Col1", " My Column 2");
        List<String> column1 = Arrays.asList("a", "ab", "abcdef", " abcdef ");
        List<String> column2 = Arrays.asList("defgh", "d", "e", "f");
        DefaultAsciiArray asciiArray = new DefaultAsciiArray().withColumns(column1, column2).withTitle(titles);
        List<Integer> columnsSize = asciiArray.getColumnsSize();
        assertNotNull(columnsSize);
        assertEquals(columnsSize.size(), 2);
        assertEquals(columnsSize.get(0), Integer.valueOf(8));
        assertEquals(columnsSize.get(1), Integer.valueOf(12));
    }

    @Test
    public void testGetBorderLine() {
        List<String> column1 = Arrays.asList("a", "ab", "abc", "abcd");
        DefaultAsciiArray asciiArray = new DefaultAsciiArray().withColumns(column1);

        String line = asciiArray.getBorderLine();

        // 4 spaces for the column
        assertEquals(line, "+----+");

    }

    @Test
    public void testGetBorderLineWithTitle() {
        List<String> column1 = Arrays.asList("a", "ab", "abc", "abcd");
        DefaultAsciiArray asciiArray = new DefaultAsciiArray().withColumns(column1).withTitle("Identifier");

        String line = asciiArray.getBorderLine();

        // 10 - for the title
        assertEquals(line, "+----------+");

    }

    @Test
    public void testOneColumn() {
        List<String> column1 = Arrays.asList("row1", "row2", "row3");
        AsciiArray asciiArray = new DefaultAsciiArray().withColumns(column1);
        String result = asciiArray.toAscii();
        assertEquals(result.replaceAll("\\r|\\n", ""), "+----+|row1||row2||row3|+----+");
    }

    @Test
    public void testTwoColumns() {
        List<String> column1 = Arrays.asList("row1", "row2", "row3");
        List<String> column2 = Arrays.asList("1", "2", "3");
        AsciiArray asciiArray = new DefaultAsciiArray().withColumns(column1, column2);
        String result = asciiArray.toAscii();
        assertEquals(result.replaceAll("\\r|\\n", ""), format("+----+-+|row1|1||row2|2||row3|3|+----+-+"));
    }

    @Test
    public void testTwoColumnsWithTitle() {
        List<String> column1 = Arrays.asList("row1", "row2", "row3");
        List<String> column2 = Arrays.asList("1", "2", "3");
        List<String> titles = Arrays.asList("name", "id");
        AsciiArray asciiArray = new DefaultAsciiArray().withColumns(column1, column2).withTitle(titles);
        String result = asciiArray.toAscii();
        assertEquals(result.replaceAll("\\r|\\n", ""), format("+----+--+|NAME|ID|+----+--+|row1| 1||row2| 2||row3| 3|+----+--+"));
    }

    @Test
    public void testTwoColumnsWithTitleModernFormatter() {
        List<String> column1 = Arrays.asList("row1", "row2", "row3");
        List<String> column2 = Arrays.asList("1", "2", "3");
        List<String> titles = Arrays.asList("name", "id");
        AsciiArray asciiArray = new DefaultAsciiArray().withColumns(column1, column2).withTitle(titles).withFormatter(MODERN);
        String result = asciiArray.toAscii();
        assertEquals(result, format("NAME  ID  %n" +
                                    "row1  1   %n" +
                                    "row2  2   %n" +
                                    "row3  3   %n"));
    }

    @Test
    public void testTwoColumnsWithTitleCsvFormatter() {
        List<String> column1 = Arrays.asList("row1", "row2", "row3");
        List<String> column2 = Arrays.asList("1", "2", "3");
        List<String> titles = Arrays.asList("name", "id");
        AsciiArray asciiArray = new DefaultAsciiArray().withColumns(column1, column2).withTitle(titles).withFormatter(CSV);
        String result = asciiArray.toAscii();
        assertEquals(result, format("NAME,ID%n" +
                                    "row1,1%n" +
                                    "row2,2%n" +
                                    "row3,3%n"));
    }

}
