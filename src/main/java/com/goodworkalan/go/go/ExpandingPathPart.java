package com.goodworkalan.go.go;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class ExpandingPathPart implements PathPart {
    public Object getKey() {
        throw new UnsupportedOperationException();
    }

    public Artifact getArtifact() {
        throw new UnsupportedOperationException();
    }

    public File getFile() {
        throw new UnsupportedOperationException();
    }

    public URL getURL() throws MalformedURLException {
        throw new UnsupportedOperationException();
    }
}
