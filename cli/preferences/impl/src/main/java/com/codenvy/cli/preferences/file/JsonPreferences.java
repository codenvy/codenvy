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

import com.codenvy.cli.preferences.Preferences;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.type.MapLikeType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.codenvy.cli.preferences.file.LifecycleEvent.CREATE;
import static com.codenvy.cli.preferences.file.LifecycleEvent.DELETE;
import static com.codenvy.cli.preferences.file.LifecycleEvent.MERGE;

/**
 * Real implementation of {@link Preferences} based on Jackson to be able to map and unmap stored objects to a preferences tree.
 *
 * @author Stéphane Daviet
 */
public class JsonPreferences implements Preferences, LifecycleCallback {

    private List<LifecycleCallback> callbackList;

    private final ConcurrentMap<String, Object> innerPreferences;

    private final ObjectMapper mapper;

    private final MapLikeType mapType;

    private final Collection<Class<?>> simpleTypes = Arrays.asList(new Class<?>[]{
            Byte.class,
            Character.class,
            Short.class,
            Integer.class,
            Long.class,
            Boolean.class,
            Float.class,
            Double.class,
            Void.class
    });

    protected JsonPreferences() {
        this(new HashMap<String, Object>());
    }

    @JsonCreator
    protected JsonPreferences(Map<String, Object> innerPreferences) {
        this.callbackList = new ArrayList<>();
        this.innerPreferences = new ConcurrentHashMap<String, Object>(innerPreferences);

        this.mapper = new ObjectMapper();
        this.mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        this.mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        this.mapper.setVisibility(PropertyAccessor.GETTER, Visibility.PUBLIC_ONLY);
        this.mapper.setVisibility(PropertyAccessor.IS_GETTER, Visibility.PUBLIC_ONLY);

        this.mapType = this.mapper.getTypeFactory()
                                  .constructMapType(ConcurrentMap.class, String.class, Object.class);
    }

    @JsonValue
    public Map<String, Object> getInnerPreferences() {
        return innerPreferences;
    }

    protected Object get(String key) {
        return innerPreferences.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key, Class<T> clazz) {
        if (!shouldMapUnmap(clazz)) {
            return (T)innerPreferences.get(key);
        }
        return mapper.convertValue(innerPreferences.get(key), clazz);
    }

    @Override
    public void put(String key, Object value) {
        put(key, value, true);
        notify(CREATE);
    }

    @Override
    public void merge(String key, Object value) {
        put(key, value, false);
        notify(MERGE);
    }

    protected void put(String key, Object value, boolean overwrite) {
        Object previousValue = innerPreferences.get(key);
        if (previousValue != null && !overwrite) {
            ObjectReader updater = mapper.readerForUpdating(previousValue);
            try {
                updater.readValue(this.mapper.valueToTree(value));
            } catch (IllegalArgumentException | IOException e) {
                throw new IllegalStateException("Unable to put the value", e);
            }
        } else {
            if (!shouldMapUnmap(value.getClass())) {
                innerPreferences.put(key, value);
            } else {
                innerPreferences.put(key, this.mapper.convertValue(value, mapType));
            }
        }
    }

    @Override
    public void delete(String key) {
        innerPreferences.remove(key);
        notify(DELETE);
    }

    @Override
    public Preferences path(String key) {
        return path(key, true);
    }

    @Override
    public Preferences walk(String key) {
        return path(key, false);
    }

    @Override
    public boolean pathExists(String key) {
        return innerPreferences.containsKey(key);
    }

    protected Preferences path(String key, boolean create) {
        Object value = innerPreferences.get(key);
        if (value == null) {
            if (!create) {
                return null;
            }
            value = new JsonPreferences();
            ((JsonPreferences) value).addCallback(this);
            innerPreferences.put(key, value);
            return (JsonPreferences)value;
        } else if (value instanceof JsonPreferences) {
            return (JsonPreferences)value;
        } else if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            JsonPreferences deepInnerPreferences = new JsonPreferences((Map<String, Object>)value);
            deepInnerPreferences.addCallback(this);
            innerPreferences.put(key, deepInnerPreferences);
            return deepInnerPreferences;
        } else {
            return this;
        }
    }

    private boolean shouldMapUnmap(Class< ? > clazz) {
        return !(simpleTypes.contains(clazz)
                 || clazz.isPrimitive()
                 || clazz.isArray()
                 || Enum.class.isAssignableFrom(clazz)
                 || Collection.class.isAssignableFrom(clazz));
    }


    protected void addCallback(LifecycleCallback lifecycleCallback) {
        callbackList.add(lifecycleCallback);
    }

    protected void sendNotification(LifecycleEvent lifecycleEvent) {
        for (LifecycleCallback callback : callbackList) {
            callback.notify(lifecycleEvent);
        }
    }

    @Override
    public void notify(LifecycleEvent lifecycleEvent) {
        // notify listeners
        sendNotification(lifecycleEvent);
    }
}
