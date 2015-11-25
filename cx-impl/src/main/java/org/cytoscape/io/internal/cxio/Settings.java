package org.cytoscape.io.internal.cxio;

public final class Settings {

    public final static Settings INSTANCE                            = new Settings();

    private static final boolean IGNORE_SELECTED_COLUMN_DEFAULT      = false;
    private static final boolean IGNORE_SUID_COLUMN_DEFAULT          = true;
    private static final boolean WRITE_SELECTED_ONLY_IF_TRUE_DEFAULT = false;
    private static final boolean DEBUG_DEFAULT                       = true;

    private boolean              _debug                              = DEBUG_DEFAULT;
    private boolean              _ignore_selected_column             = IGNORE_SELECTED_COLUMN_DEFAULT;
    private boolean              _write_selected_only_if_true        = WRITE_SELECTED_ONLY_IF_TRUE_DEFAULT;
    private boolean              _ignore_suid_column                 = IGNORE_SUID_COLUMN_DEFAULT;

    public boolean isDebug() {
        return _debug;
    }

    public void setDebug(final boolean debug) {
        _debug = debug;
    }

    public boolean isWriteSelectedOnlyIfTrue() {
        return _write_selected_only_if_true;
    }

    public void setWriteSelectedOnlyIfTrue(final boolean write_selected_only_if_true) {
        _write_selected_only_if_true = write_selected_only_if_true;
    }

    public boolean isIgnoreSelectedColumn() {
        return _ignore_selected_column;
    }

    public void setIgnoreSelectedColumn(final boolean ignore_selected_column) {
        _ignore_selected_column = ignore_selected_column;
    }

    public boolean isIgnoreSuidColumn() {
        return _ignore_suid_column;
    }

    public void setIgnoreSuidColumn(final boolean ignore_suid_column) {
        _ignore_suid_column = ignore_suid_column;
    }

    private Settings() {
        // hidden constructor
    }

}
