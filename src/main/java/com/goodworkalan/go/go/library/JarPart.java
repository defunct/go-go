package com.goodworkalan.go.go.library;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;


/**
 * A path part that is a specific jar in the file system. This class is used to
 * indicate a specific jar in the file system.
 * <p>
 * Because the jar is not looked up in a library, there is no associated
 * {@link Artifact} and {@link #getArtifact()} will return null.
 * 
 * @author Alan Gutierrez
 */
public class JarPart implements PathPart {
    /** The jar file. */
    private File file;

    /**
     * Create a jar part with the given jar file.
     * 
     * @param file
     *            The jar file.
     */
    public JarPart(File file) {
        this.file = file;
    }
    
    // TODO Document.
    public File getFile() {
        return file;
    }
    
    /**
     * Get the jar protocol URL for this artifact part.
     * 
     * @return The URL.
     */
    public URL getURL() {
        return PathParts.toURL("jar:" + PathParts.toURL(file.toURI().toString()).toExternalForm() + "!/");
    }

    /**
     * Return null since this class represents a particular jar on the file
     * system and not an artifact that is looked up in a library.
     * 
     * @return The artifact backing this <code>PathPart</code>.
     */
    public Artifact getArtifact() {
        return null;
    }

    /**
     * Returns a collection that contains a this directory path part since this
     * jar path part is already resolved.
     * 
     * @param library
     *            The library.
     * @param expanded
     *            The list of expended path parts.
     * @param expand
     *            The list of path parts to expand.
     */
    public void expand(Library library, Collection<PathPart> expanded, Collection<PathPart> expand) {
        expanded.add(this);
    }

    /**
     * The unversioned key for a jar is simply the jar file. We don't really
     * know the version number of the software it contains.
     * 
     * @return A unique, unversioned key for the <code>PathPart</code>.
     */
    public Object getUnversionedKey() {
        return file;
    }
    
    /**
     * Excludes do not effect jar path parts so the returned set is always empty.
     * 
     * @return The empty set.
     */
    public Set<Exclude> getExcludes() {
        return Collections.<Exclude> emptySet();
    }
}
