package com.goodworkalan.go.go;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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
     * Create a library with the given library directory.
     * 
     * @param dir
     *            The library directory.
     */
    public Library(File...dirs) {
        this.dirs = dirs;
    }
    
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
        return resolve(parts, Collections.<List<String>>emptySet());
    }

    /**
     * Expand the given collection of path parts excluding artifacts whose
     * unversioned key is in the given set of excludes.
     * 
     * @param parts
     *            The collection of path parts.
     * @param exclude
     *            A set of unversioned keys of artifacts to exclude.
     * @return An expanded collection of path parts.
     */
    public Collection<PathPart> resolve(Collection<PathPart> parts, Set<List<String>> exclude) {
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
