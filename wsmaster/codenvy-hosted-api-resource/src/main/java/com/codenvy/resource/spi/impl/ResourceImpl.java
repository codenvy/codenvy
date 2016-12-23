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
package com.codenvy.resource.spi.impl;

import com.codenvy.resource.model.Resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

/**
 * @author Sergii Leschenko
 */
@Entity(name = "Resource")
@Table(name = "resource")
public class ResourceImpl implements Resource {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "amount", nullable = false)
    private long amount;

    @Column(name = "unit", nullable = false)
    private String unit;

    public ResourceImpl() {}

    public ResourceImpl(String type, long amount, String unit) {
        this.amount = amount;
        this.type = type;
        this.unit = unit;
    }

    public ResourceImpl(Resource resource) {
        this(resource.getType(),
             resource.getAmount(),
             resource.getUnit());
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public String getUnit() {
        return unit;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ResourceImpl)) {
            return false;
        }
        final ResourceImpl that = (ResourceImpl)obj;
        return amount == that.amount
               && Objects.equals(id, that.id)
               && Objects.equals(type, that.type)
               && Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(id);
        hash = 31 * hash + Objects.hashCode(type);
        hash = 31 * hash + Long.hashCode(amount);
        hash = 31 * hash + Objects.hashCode(unit);
        return hash;
    }

    @Override
    public String toString() {
        return "ResourceImpl{" +
               "id=" + id +
               ", type='" + type + '\'' +
               ", amount=" + amount +
               ", unit='" + unit + '\'' +
               '}';
    }
}
