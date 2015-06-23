package org.cytoscape.io.internal.cxio;

import java.io.OutputStream;

public final class CxOutput {

    private final OutputStream _os;
    private Status             _status;
    private Error              _error;

    public CxOutput(final OutputStream os, final Status status) {
        _os = os;
        _status = status;
    }

    public OutputStream getOutputStream() {
        return _os;
    }

    public Status getStatus() {
        return _status;
    }

    public void setStatus(final Status status) {
        _status = status;
    }

    public Error getError() {
        return _error;
    }

    public void setError(final Error error) {
        _error = error;
    }

    public enum Status {
        OK, NOT_OK
    }

}
