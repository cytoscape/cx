package org.cytoscape.io.internal.cxio.kit;

public final class VisualProperty {

    private final String name;
    private final String applies_to;
    private final String default_value;
    private final String value;

    public VisualProperty(final String name, final String applies_to, final String default_value,
            final String value) {
        this.name = name;
        this.applies_to = applies_to;
        this.default_value = default_value;
        this.value = value;
    }

    public final String getAppliesTo() {
        return applies_to;
    }

    public final String getDefaultValue() {
        return default_value;
    }

    public final String getName() {
        return name;
    }

    public final String getValue() {
        return value;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("name: ");
        sb.append(name);
        sb.append(", applies to: ");
        sb.append(applies_to);
        sb.append(", default: ");
        sb.append(default_value);
        sb.append(", value: ");
        sb.append(value);
        return sb.toString();
    }
}
