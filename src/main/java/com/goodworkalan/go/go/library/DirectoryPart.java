package com.goodworkalan.go.go.library;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.goodworkalan.go.go.GoException;


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
        if (!dir.isAbsolute()) {
            throw new GoException(0, dir);
        }
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
    public URL getURL() {
        return PathParts.toURL(dir.getAbsoluteFile().toURI().toString());
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
    public void expand(Library library, Collection<PathPart> expanded, Collection<PathPart> expand) {
        expanded.add(this);
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

    /**
     * Excludes do not effect directory path parts so the returned set is always
     * empty.
     * 
     * @return The empty set.
     */
    public Set<Exclude> getExcludes() {
        return Collections.<Exclude>emptySet();
    }
}
