package com.goodworkalan.go.go;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
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
    /** Map of repostiory types to client download strategies. */
    private final Map<String, RepositoryClient> repositoryClients = new HashMap<String, RepositoryClient>();

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

    /**
     * Determine whether the library contains the given artifact.
     * 
     * @param artifact
     *            The artifact.
     * @param suffix
     *            The artifact file suffix.
     * @param extension
     *            The artifact file extension.
     * @return True if the library contains the given artifact.
     */
    public boolean contains(Artifact artifact, String suffix, String extension) {
        return getFile(artifact, suffix, extension).exists();
    }

    /**
     * Get the file for the given artifact file with the given suffix and given
     * extension.
     * 
     * @param artifact
     *            The artifact.
     * @param suffix
     *            The artifact file suffix.
     * @param extension
     *            The artifact file extension.
     * @return The file.
     */
    public File getFile(Artifact artifact, String suffix, String extension) {
        return new File(dir, artifact.getPath(suffix, extension));
    }
    
    public LibraryPath emptyPath(Set<Object> excludes) {
        return new LibraryPath(this, Collections.<PathPart>emptyList(), excludes);
    }
    
    public LibraryPath resolve(PathPart part, Set<Object> exclude, Catcher catcher) {
        return resolve(Collections.<PathPart>singletonList(part), exclude, catcher);
    }
    
    public LibraryPath resolve(Collection<PathPart> parts, Set<Object> exclude, Catcher catcher) {
        Map<Object, PathPart> expanded = new LinkedHashMap<Object, PathPart>();
        Collection<PathPart> current = parts;
        Collection<PathPart> next = new ArrayList<PathPart>();
        while (!current.isEmpty()) {
            for (PathPart part : current) {
                Collection<PathPart> expansions;
                try {
                    expansions = part.expand(this, next);
                } catch (GoException e) {
                    catcher.examine(e);
                    continue;
                }
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
    
    public LibraryEntry getEntry(Artifact artifact, List<Repository> repositories) {
        File deps = new  File(dir, artifact.getPath("", "dep"));
        if (!deps.exists()) {
            for (Repository repository : repositories) {
                RepositoryClient client = repositoryClients.get(repository.type);
                if (client != null) {
                    if (!deps.exists()) {
                        client.fetchDependencies(repository.uri, this, artifact);
                    }
                    if (!(new File(dir, artifact.getPath("", "jar"))).exists()) {
                        client.fetch(repository.uri, this, artifact, "", "jar");
                    }
                }
            }
        }
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
