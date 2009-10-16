package com.goodworkalan.go.go;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

/**
 * An expanding path part for a directory. 
 *
 * @author Alan Gutierrez
 */
public class DirectoryPart implements PathPart {
    /** The directory. */
    private final File dir;

    /**
     * Create a directory part with the given directory.
     * 
     * @param dir
     *            The directory.
     */
    public DirectoryPart(File dir) {
        this.dir = dir;
    }
    
    /**
     * Get the path part as a file.
     * 
     * @return The path part as a file.
     */
    public File getFile() {
        return dir;
    }

    /**
     * Get the path part as a URL.
     * 
     * @return The path part as a URL.
     */
    public URL getURL() throws MalformedURLException {
        return dir.toURI().toURL();
    }
    
    public Artifact getArtifact() {
        return null;
    }

    public Collection<PathPart> expand(Library library, Collection<PathPart> expand) {
        return Collections.<PathPart>singletonList(this);
    }

    public Object getKey() {
        return dir;
    }
}
