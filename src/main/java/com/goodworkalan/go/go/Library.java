package com.goodworkalan.go.go;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A Jav-a-Go-Go format library. Libraries can live only on the local file
 * system, therefore a library id uniquely identified by the directory where it
 * lives.
 * <p>
 * Simple enough to make it a list of directories, to implement a library path.
 * 
 * @author Alan Gutierrez
 */
public class Library {
    /** The library directory. */
    private final File dir;
    
    /**
     * Create a library with the given library directory.
     * 
     * @param dir
     *            The library directory.
     */
    public Library(File dir) {
        this.dir = dir;
    }

    public LibraryPath emptyPath(Set<Object> excludes) {
        return new LibraryPath(this, Collections.<PathPart>emptyList(), excludes);
    }
    
    public LibraryPath resolve(PathPart part) {
        return resolve(Collections.<PathPart>singletonList(part));
    }
    
    public LibraryPath resolve(Collection<PathPart> parts) {
        return resolve(parts, new HashSet<Object>());
    }
    
    public LibraryPath resolve(Collection<PathPart> parts, Set<Object> exclude) {
        Map<Object, PathPart> expanded = new LinkedHashMap<Object, PathPart>();
        Collection<PathPart> current = parts;
        Collection<PathPart> next = new ArrayList<PathPart>();
        while (!current.isEmpty()) {
            for (PathPart part : current) {
                Collection<PathPart> expansions = part.expand(this, next);
                for (PathPart expansion : expansions) {
                    Object key = expansion.getKey();
                    if (!(exclude.contains(key) || expanded.containsKey(key))) {
                        expanded.put(expansion.getKey(), expansion);
                    }
                }
            }
            current = next;
            next = new ArrayList<PathPart>();
        }
        return new LibraryPath(this, expanded.values(), new HashSet<Object>(exclude));
    }
    
    public LibraryEntry getEntry(Artifact artifact) {
        File deps = new  File(dir, artifact.getPath("dep"));
        if (deps.exists()) {
            return new LibraryEntry(dir, artifact);
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
            return dir.equals(((Library) object).dir);
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
        return dir.hashCode();
    }
}
