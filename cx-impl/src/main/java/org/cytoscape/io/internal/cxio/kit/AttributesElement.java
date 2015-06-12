package org.cytoscape.io.internal.cxio.kit;

import java.util.List;
import java.util.SortedMap;

import org.cytoscape.io.internal.cxio.kit.CxConstants.ATTRIBUTE_TYPE;

public abstract class AttributesElement implements AspectElement {

    SortedMap<String, AttributeValues> attributes;
    String                             id;

    public final void addAttribute(final String key, final String value, final ATTRIBUTE_TYPE type) {
        if (Util.isEmpty(key)) {
            throw new IllegalArgumentException("attempt to use null or empty attribute key");
        }
        if (value == null) {
            throw new IllegalArgumentException("attempt to use null value");
        }
        if (!attributes.containsKey(key)) {
            attributes.put(key, new AttributeValues(type));
        }
        attributes.get(key).addValue(value);
    }
    
    public final List<String> get( final String key ) {
        return attributes.get(key).getValues();
    }

    public final SortedMap<String, AttributeValues> getAttributes() {
        return attributes;
    }

    public final String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}