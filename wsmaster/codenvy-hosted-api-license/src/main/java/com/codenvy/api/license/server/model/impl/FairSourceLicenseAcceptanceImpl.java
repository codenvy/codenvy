/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.api.license.server.model.impl;

import com.codenvy.api.license.model.FairSourceLicenseAcceptance;

import java.util.Objects;

/**
 * @author Anatolii Bazko
 */
public class FairSourceLicenseAcceptanceImpl implements FairSourceLicenseAcceptance {
    private final String firstName;
    private final String lastName;
    private final String email;

    public FairSourceLicenseAcceptanceImpl(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FairSourceLicenseAcceptanceImpl)) {
            return false;
        }
        final FairSourceLicenseAcceptanceImpl that = (FairSourceLicenseAcceptanceImpl)obj;
        return Objects.equals(firstName, that.firstName)
               && Objects.equals(lastName, that.lastName)
               && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(firstName);
        hash = 31 * hash + Objects.hashCode(lastName);
        hash = 31 * hash + Objects.hashCode(email);
        return hash;
    }

    @Override
    public String toString() {
        return "FairSourceLicenseAcceptanceImpl{" +
               "firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", email='" + email + '\'' +
               '}';
    }
}
