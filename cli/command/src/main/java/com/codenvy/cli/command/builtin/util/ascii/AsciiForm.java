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
 * This interface represents
 * @author Florent Benoit
 */
public interface AsciiForm {

    /**
     * Adds a new entry in the form
     * @param propertyName the name of the property
     * @param propertyValue the value of the property
     * @return {@link com.codenvy.cli.command.builtin.util.ascii.AsciiForm}
     */
    AsciiForm withEntry(String propertyName, String propertyValue);

    /**
     * Order all properties by using alphabetical order.
     * @return {@link com.codenvy.cli.command.builtin.util.ascii.AsciiForm}
     */
    AsciiForm alphabeticalSort();


    /**
     * Use uppercase for the property name
     * @return {@link com.codenvy.cli.command.builtin.util.ascii.AsciiForm}
     */
    AsciiForm withUppercasePropertyName();


    AsciiForm withFormatter(FormatterMode formatterMode);

    /**
     * Transform the given form into an ascii form
     *
     * @return stringified table of the form
     */
    String toAscii();

}
