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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Stéphane Daviet
 */
public class FakePojo {
    public static final String DUMB_POJO_NAME  = "Dumb pojo";

    private String             name;

    private String             leitmotiv;

    private List<String>       characteristics = new ArrayList<>();

    public FakePojo() {
    }

    public FakePojo(String name, String leitmotiv, String... characteristics) {
        this.name = name;
        this.leitmotiv = leitmotiv;
        this.characteristics = new ArrayList<>(Arrays.asList(characteristics));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLeitmotiv() {
        return leitmotiv;
    }

    public void setLeitmotiv(String leitmotiv) {
        this.leitmotiv = leitmotiv;
    }

    public List<String> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(List<String> characteristics) {
        this.characteristics = characteristics;
    }

    public FakePojo withName(String name) {
        this.name = name;
        return this;
    }

    public FakePojo withLeitmotiv(String leitmotiv) {
        this.leitmotiv = leitmotiv;
        return this;
    }

    public FakePojo withCharacteristic(String characteristic) {
        this.characteristics.add(characteristic);
        return this;
    }

    public FakePojo withCharacteristics(String... characteristics) {
        setCharacteristics(Arrays.asList(characteristics != null ? characteristics : new String[0]));
        return this;
    }

    public static FakePojo getDumbInstance() {
        return new FakePojo().withName(DUMB_POJO_NAME)
                             .withLeitmotiv("No leitmotiv: I’m dumb.")
                             .withCharacteristic("fake")
                             .withCharacteristic("dumb")
                             .withCharacteristic("useless");
    }
}
