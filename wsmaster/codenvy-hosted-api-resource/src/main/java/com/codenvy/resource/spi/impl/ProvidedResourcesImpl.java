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

import com.codenvy.resource.model.ProvidedResources;
import com.google.common.base.Objects;

import java.util.List;

/**
 * @author Sergii Leschenko
 */
public class ProvidedResourcesImpl implements ProvidedResources {
    private final String                 provider;
    private final String                 id;
    private final String                 owner;
    private final Long                   startTime;
    private final Long                   endTime;
    private final List<AbstractResource> resources;

    public ProvidedResourcesImpl(String provider,
                                 String id,
                                 String owner,
                                 Long startTime,
                                 Long endTime,
                                 List<AbstractResource> resources) {
        this.provider = provider;
        this.id = id;
        this.owner = owner;
        this.startTime = startTime;
        this.endTime = endTime;
        this.resources = resources;
    }

    @Override
    public String getProviderId() {
        return provider;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public Long getStartTime() {
        return startTime;
    }

    @Override
    public Long getEndTime() {
        return endTime;
    }

    @Override
    public List<AbstractResource> getResources() {
        return resources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProvidedResourcesImpl)) return false;
        ProvidedResourcesImpl that = (ProvidedResourcesImpl)o;
        return Objects.equal(provider, that.provider) &&
               Objects.equal(id, that.id) &&
               Objects.equal(owner, that.owner) &&
               Objects.equal(startTime, that.startTime) &&
               Objects.equal(endTime, that.endTime) &&
               Objects.equal(resources, that.resources);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(provider, id, owner, startTime, endTime, resources);
    }

    @Override
    public String toString() {
        return "GrantedResourceImpl{" +
               "provider='" + provider + '\'' +
               ", id='" + id + '\'' +
               ", owner='" + owner + '\'' +
               ", startTime=" + startTime +
               ", endTime=" + endTime +
               ", resources=" + resources +
               '}';
    }
}
