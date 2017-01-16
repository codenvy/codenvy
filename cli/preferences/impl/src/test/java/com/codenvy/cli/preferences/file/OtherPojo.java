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
package com.codenvy.cli.preferences.file;

/**
 * @author Stéphane Daviet
 */
public class OtherPojo {
    public static final String OTHER_POJO_NAME = "Other pojo";

    private String             name;

    private String             anotherProperty;

    public OtherPojo() {

    }

    public OtherPojo(String name, String anotherProperty) {
        this.name = name;
        this.anotherProperty = anotherProperty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAnotherProperty() {
        return anotherProperty;
    }

    public void setAnotherProperty(String anotherProperty) {
        this.anotherProperty = anotherProperty;
    }

    public OtherPojo withName(String name) {
        this.name = name;
        return this;
    }

    public OtherPojo withAnotherProperty(String anotherProperty) {
        this.anotherProperty = anotherProperty;
        return this;
    }

    public static final OtherPojo getInstance() {
        return new OtherPojo().withName(OTHER_POJO_NAME)
                              .withAnotherProperty("No interested in");
    }
}
