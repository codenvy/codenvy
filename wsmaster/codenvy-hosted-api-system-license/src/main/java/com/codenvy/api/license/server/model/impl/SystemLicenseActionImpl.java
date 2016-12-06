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

import com.codenvy.api.license.shared.model.SystemLicenseAction;
import com.codenvy.api.license.shared.model.Constants;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data object of {@link SystemLicenseAction}.
 *
 * @author Anatolii Bazko
 */
@Entity(name = "LicenseAction")
@NamedQueries(
        {
                @NamedQuery(name = "LicenseAction.getByLicenseAndAction",
                            query = "SELECT l " +
                                    "FROM LicenseAction l " +
                                    "WHERE :license_type = l.licenseType AND :action_type = l.actionType")
        }
)
@Table(name = "license_action")
public class SystemLicenseActionImpl implements SystemLicenseAction {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "license_type", nullable = false)
    private Constants.License licenseType;

    @Column(name = "license_qualifier")
    private String licenseQualifier;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private Constants.Action actionType;

    @Column(name = "action_timestamp", nullable = false)
    private long actionTimestamp;

    @ElementCollection
    @MapKeyColumn(name = "name")
    @Column(name = "value", nullable = false)
    @CollectionTable(name = "license_action_attributes",
                     joinColumns = {
                             @JoinColumn(name = "license_type", referencedColumnName = "license_type"),
                             @JoinColumn(name = "action_type", referencedColumnName = "action_type")
                     })
    private Map<String, String> attributes;

    public SystemLicenseActionImpl() { }

    public SystemLicenseActionImpl(Constants.License licenseType,
                                   Constants.Action actionType,
                                   long actionTimestamp,
                                   String licenseQualifier,
                                   Map<String, String> attributes) {
        this.licenseType = licenseType;
        this.licenseQualifier = licenseQualifier;
        this.actionType = actionType;
        this.actionTimestamp = actionTimestamp;
        this.attributes = attributes == null ? new HashMap<>() : new HashMap<>(attributes);
    }

    @Override
    public Constants.License getLicenseType() {
        return licenseType;
    }

    @Override
    public String getLicenseQualifier() {
        return licenseQualifier;
    }

    @Override
    public Constants.Action getActionType() {
        return actionType;
    }

    @Override
    public long getActionTimestamp() {
        return actionTimestamp;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SystemLicenseActionImpl)) {
            return false;
        }
        final SystemLicenseActionImpl that = (SystemLicenseActionImpl)obj;
        return Objects.equals(licenseType, that.licenseType)
               && Objects.equals(licenseQualifier, that.licenseQualifier)
               && Objects.equals(actionType, that.actionType)
               && Objects.equals(actionTimestamp, that.actionTimestamp)
               && getAttributes().equals(that.getAttributes());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(licenseType);
        hash = 31 * hash + Objects.hashCode(licenseQualifier);
        hash = 31 * hash + Objects.hashCode(actionType);
        hash = 31 * hash + Objects.hashCode(actionTimestamp);
        hash = 31 * hash + getAttributes().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "CodenvyLicenseActionImpl{" +
               "licenseType=" + licenseType +
               ", licenseQualifier='" + licenseQualifier + '\'' +
               ", actionType=" + actionType +
               ", actionTimestamp=" + actionTimestamp +
               ", attributes=" + attributes +
               '}';
    }
}
