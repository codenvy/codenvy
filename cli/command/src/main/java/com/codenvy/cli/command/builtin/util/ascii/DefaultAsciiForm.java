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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codenvy.cli.command.builtin.util.ascii.AnsiHelper.removeAnsi;
import static com.codenvy.cli.command.builtin.util.ascii.FormatterMode.CSV;
import static com.codenvy.cli.command.builtin.util.ascii.FormatterMode.EIGHTIES;
import static com.codenvy.cli.command.builtin.util.ascii.FormatterMode.MODERN;

/**
 * @author Florent Benoit
 */
public class DefaultAsciiForm implements AsciiForm {

    private List<AsciiFormEntry> entries;

    private boolean alphabeticalSort = false;

    private boolean uppercasePropertyName = false;

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
    public DefaultAsciiForm() {
        this.entries = new ArrayList<>();
        this.formatters = new HashMap<>();
        formatters.put(EIGHTIES, new EightiesFormatter());
        formatters.put(MODERN, new ModernFormatter());
        formatters.put(CSV, new CSVFormatter());

        this.formatterMode = MODERN;
    }


    /**
     * Adds a new entry in the form
     *
     * @param propertyName
     *         the name of the property
     * @param propertyValue
     *         the value of the property
     * @return {@link com.codenvy.cli.command.builtin.util.ascii.AsciiForm}
     */
    @Override
    public AsciiForm withEntry(String propertyName, String propertyValue) {
        entries.add(new AsciiFormEntry(propertyName, propertyValue));
        return this;
    }

    /**
     * Order all properties by using alphabetical order.
     *
     * @return {@link com.codenvy.cli.command.builtin.util.ascii.AsciiForm}
     */
    @Override
    public AsciiForm alphabeticalSort() {
        this.alphabeticalSort = true;
        return this;
    }

    /**
     * Use uppercase for the property name
     *
     * @return {@link com.codenvy.cli.command.builtin.util.ascii.AsciiForm}
     */
    @Override
    public AsciiForm withUppercasePropertyName() {
        this.uppercasePropertyName = true;
        return this;
    }

    @Override
    public AsciiForm withFormatter(FormatterMode formatterMode) {
        this.formatterMode = formatterMode;
        return this;
    }

    /**
     * Transform the given form into an ascii form
     *
     * @return stringified table of the form
     */
    @Override
    public String toAscii() {
        // compute each line

        // sort entries if alphabetical sort
        if (alphabeticalSort) {
            Collections.sort(entries);
        }


        StringBuilder sb = new StringBuilder();
        for (AsciiFormEntry entry : entries) {
            // first get title
            String title = getFormatterMode().formatFormTitle(entry.getName(), new MyAsciiFormInfo(this));
            String value = getFormatterMode().formatFormValue(entry.getValue(), new MyAsciiFormInfo(this));
            sb.append(String.format("%s%s%n", title, value));
        }
        return sb.toString();

    }

    /**
     * @return formatter
     */
    protected AsciiFormatter getFormatterMode() {
        return formatters.get(formatterMode);
    }


    protected int getTitleColumnSize() {
        int length = 0;
        for (AsciiFormEntry entry : entries) {
            // length is without ansi
            length = Math.max(length, removeAnsi(entry.getName()).length());
        }
        return length;
    }

    protected int getValueColumnSize() {
        int length = 0;
        for (AsciiFormEntry entry : entries) {
            // length is without ansi
            length = Math.max(length, removeAnsi(entry.getValue()).length());
        }
        return length;
    }

    private static class MyAsciiFormInfo implements AsciiFormInfo {
        private final DefaultAsciiForm form;

        public MyAsciiFormInfo(DefaultAsciiForm form) {
            this.form = form;
        }


        @Override
        public int getTitleColumnSize() {
            return form.getTitleColumnSize();
        }

        @Override
        public int getValueColumnSize() {
            return form.getValueColumnSize();
        }

        @Override
        public boolean isUppercasePropertyName() {
            return form.uppercasePropertyName;
        }
    }
}
