/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.plugin.urlfactory;

/**
 * Parameters holder when wanting to create a factory
 * @author Florent Benoit
 */
public class CreateFactoryParams {

    /**
     * Location (url) to the json file.
     */
    private String codenvyJsonFileLocation;

    /**
     * Use the create method
     */
    private CreateFactoryParams() {
    }

    /**
     * Creates and returns arguments holder.
     */
    public static CreateFactoryParams create() {
        return new CreateFactoryParams();
    }


    /**
     * Defines the path to the codenvy json file location
     * @param codenvyJsonFileLocation the url to grab the json file location
     * @return the current instance.
     */
    public CreateFactoryParams codenvyJsonFileLocation(String codenvyJsonFileLocation) {
        this.codenvyJsonFileLocation = codenvyJsonFileLocation;
        return this;
    }

    /**
     * Defines the path to the codenvy json file location
     * @return the url to grab the json file location
     */
    public String codenvyJsonFileLocation() {
        return this.codenvyJsonFileLocation;
    }

}
