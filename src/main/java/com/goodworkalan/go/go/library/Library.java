package com.goodworkalan.go.go.library;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/**
 * A Jav-a-Go-Go format library.
 * <p>
 * Libraries can live only on the local file system, therefore a library id
 * uniquely identified by the directory where it lives.
 * <p>
 * FIXME Make the file a list to make this a library path.
 * 
 * @author Alan Gutierrez
 */
public class Library {
    /** The library directory. */
    private final File[] dirs;

    /**
     * Create a library with the given library directory search path. The
     * library uses this search path to locate artifacts. When asked for an
     * artifact path part the first library directory containing the artifact is
     * used to to create an artifact path part.
     * 
     * @param dir
     *            The library directory.
     */
    public Library(File...dirs) {
        this.dirs = dirs;
    }
    
    /**
     * Get the directories in the library search path.
     * 
     * @return The library search path.
     */
    public File[] getDirectories() {
        return dirs;
    }

    /**
     * Expand the given collection of path parts.
     * 
     * @param parts
     *            The collection of path parts.
     * @return An expanded collection of path parts.
     */
    public Collection<PathPart> resolve(Collection<PathPart> parts) {
        return resolve(parts, Collections.<Object>emptySet());
    }

    /**
     * Expand the given collection of path parts excluding artifacts whose
     * unversioned key is in the given set of excludes. The unversioned set is a
     * wildcard set so you can use either a set of excludes, which are string
     * lists, or a set of unversioned keys, which are objects.
     * 
     * @param parts
     *            The collection of path parts.
     * @param exclude
     *            A set of unversioned keys of artifacts to exclude.
     * @return An expanded collection of path parts.
     */
    public Collection<PathPart> resolve(Collection<PathPart> parts, Set<?> exclude) {
        Map<Object, PathPart> expanded = new LinkedHashMap<Object, PathPart>();
        Collection<PathPart> current = parts;
        Collection<PathPart> next = new ArrayList<PathPart>();
        while (!current.isEmpty()) {
            for (PathPart part : current) {
                Object key = part.getUnversionedKey();
                if (!(expanded.containsKey(key) || exclude.contains(key))) {
                    Collection<PathPart> expansions = part.expand(this, next);
                    for (PathPart expansion : expansions) {
                        expanded.put(expansion.getUnversionedKey(), expansion);
                    }
                }
            }
            current = next;
            next = new ArrayList<PathPart>();
        }
        return new ArrayList<PathPart>(expanded.values());
    }

    /**
     * Get an artifact part for the given artifact or null if the given artifact
     * cannot be found in the library search path. Each library in the search
     * path is searched in order. The first library to contain the artifact is
     * used to create the artifact part.
     * 
     * @param artifact
     *            An artifact.
     * @return An artifact part or null.
     */
    public ArtifactPart getPathPart(Artifact artifact) {
        for (File dir : dirs) {
            File deps = new  File(dir, artifact.getPath("dep"));
            if (deps.exists()) {
                return new ArtifactPart(dir, artifact);
            }
        }
        return null;
    }
    
    /**
     * This library is equal to the given object if it is also a library and the
     * library directories are equals.
     * 
     * @param object
     *            The object to test for equality.
     * @return True if this object is equal to the given object.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof Library) {
            return Arrays.asList(dirs).equals(Arrays.asList(((Library) object).dirs));
        }
        return false;
    }
    
    /**
     * The hash code of the library is the hash code of the library directory.
     * 
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Arrays.asList(dirs).hashCode();
    }
}
