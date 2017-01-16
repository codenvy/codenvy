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
 * @author Florent Benoit
 */
public class AsciiFormEntry implements Comparable<AsciiFormEntry> {

    String name;

    String value;

    public AsciiFormEntry(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getValue() {
        return value;
    }


    public String getName() {

        return name;
    }


    /**
     */
    @Override
    public int compareTo(AsciiFormEntry o) {
        return name.compareTo(o.getName());
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof AsciiFormEntry)) {
            return false;
        }

        AsciiFormEntry other = (AsciiFormEntry) o;
        if (!name.equals(other.getName())) {
            return false;
        }
        if (!value.equals(other.getValue())) {
            return false;
        }

        return true;


    }
}
