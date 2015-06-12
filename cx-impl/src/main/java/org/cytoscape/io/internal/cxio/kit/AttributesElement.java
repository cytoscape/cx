package org.cytoscape.io.internal.cxio.kit;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.cytoscape.io.internal.cxio.kit.CxConstants.ATTRIBUTE_TYPE;

public abstract class AttributesElement implements AspectElement {

    final SortedMap<String, List<String>>   attributes       = new TreeMap<String, List<String>>();
    final SortedMap<String, ATTRIBUTE_TYPE> attributes_types = new TreeMap<String, ATTRIBUTE_TYPE>();
    String                                  id;

    public final SortedMap<String, List<String>> getAttributes() {
        return attributes;
    }

    public final SortedMap<String, ATTRIBUTE_TYPE> getAttributesTypes() {
        return attributes_types;
    }

    public final String getId() {
        return id;
    }

    public final ATTRIBUTE_TYPE getType(final String key) {
        return attributes_types.get(key);
    }

    public final List<String> getValues(final String key) {
        return attributes.get(key);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public final void put(final String key, final String value, final ATTRIBUTE_TYPE type) {
        if (!attributes.containsKey(key)) {
            attributes.put(key, new ArrayList<String>());
            if (type != ATTRIBUTE_TYPE.STRING) {
                attributes_types.put(key, type);
            }
        }
        attributes.get(key).add(value);
    }

    public final void put(final String key, final String value, final String type) {
        put(key, value, Util.toType(type));
    }

    public final void putType(final String key, final ATTRIBUTE_TYPE type) {
        if (type != ATTRIBUTE_TYPE.STRING) {
            attributes_types.put(key, type);
        }
    }

    public final void putType(final String key, final String type) {
        putType(key, Util.toType(type));
    }

    public final void putValue(final String key, final Object value) {
        if (!attributes.containsKey(key)) {
            attributes.put(key, new ArrayList<String>());
            final ATTRIBUTE_TYPE t = Util.determineType(value);
            if (t != ATTRIBUTE_TYPE.STRING) {
                attributes_types.put(key, t);
            }
        }
        attributes.get(key).add(String.valueOf(value));
    }

    public final void putValue(final String key, final String value) {
        if (!attributes.containsKey(key)) {
            attributes.put(key, new ArrayList<String>());
        }
        attributes.get(key).add(value);
    }

    public final void putValues(final String key, final List<String> values) {
        if (!attributes.containsKey(key)) {
            attributes.put(key, new ArrayList<String>());
        }
        attributes.get(key).addAll(values);
    }

    public final void setId(final String id) {
        this.id = id;
    }

}