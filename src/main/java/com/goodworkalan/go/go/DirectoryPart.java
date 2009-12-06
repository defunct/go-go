package com.goodworkalan.go.go;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

/**
 * A path part that maps to a class directory on the file system. 
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

    /**
     * Return null since this class represents a specific directory on the file
     * system and not an artifact that is looked up in a library.
     * 
     * @return The artifact backing this <code>PathPart</code>.
     */
    public Artifact getArtifact() {
        return null;
    }

    /**
     * Returns a collection that contains a this directory path part since this
     * directory path part is already resolved.
     * 
     * @return A collection containing this directory path part.
     */
    public Collection<PathPart> expand(Library library, Collection<PathPart> expand) {
        return Collections.<PathPart>singletonList(this);
    }

    /**
     * The unversioned key for a directory path part is simply the directory
     * file. We don't really know the version number of the software it
     * contains.
     * 
     * @return A unique, unversioned key for the path part.
     */
    public Object getUnversionedKey() {
        return dir;
    }
}
