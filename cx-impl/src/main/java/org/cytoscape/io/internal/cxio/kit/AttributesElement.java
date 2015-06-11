package org.cytoscape.io.internal.cxio.kit;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.cytoscape.io.internal.cxio.kit.CxConstants.ATTRIBUTE_TYPE;

public abstract class AttributesElement implements AspectElement {

    SortedMap<String, List<String>> attributes;
    String                          id;
    CxConstants.ATTRIBUTE_TYPE      type;

    public final void addAttribute(final String key, final String value) {
        if (Util.isEmpty(key)) {
            throw new IllegalArgumentException("attempt to use null or empty attribute key");
        }
        if (value == null) {
            throw new IllegalArgumentException("attempt to use null value");
        }
        if (!attributes.containsKey(key)) {
            attributes.put(key, new ArrayList<String>());
        }
        attributes.get(key).add(value);
    }

    public final String getId() {
        return id;
    }

    public final ATTRIBUTE_TYPE getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public final SortedMap<String, List<String>> getAttributes() {
        return attributes;
    }

}