package com.goodworkalan.go.go;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

public class JarPart implements PathPart {
    /** The jar file. */
    private File file;

    /**
     * Create a jar part with the given jar fiel.
     * 
     * @param file
     *            The jar file.
     */
    public JarPart(File file) {
        this.file = file;
    }
    
    public File getFile() {
        return file;
    }
    
    public URL getURL() throws MalformedURLException {
        return new URL("jar:" + file.toURI().toURL().toExternalForm() + "!/");
    }
    
    public Artifact getArtifact() {
        return null;
    }

    public Collection<PathPart> expand(Library library, Collection<PathPart> expand) {
        return Collections.<PathPart>singletonList(this);
    }

    public Object getKey() {
        return file;
    }
}
