package com.goodworkalan.go.go.library;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A Jav-a-Go-Go format library.
 * <p>
 * Libraries can live only on the local file system, therefore a library id
 * uniquely identified by the directory where it lives.
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
     * @param dirs
     *            The library directories.
     */
    public Library(File... dirs) {
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
    // FIXME Rename expand.
    // FIXME Why a collection? Why not return a set or a list (probably list)?
    // FIXME Collection kind of does make sense, though. You're not going to random access this.
    public Collection<PathPart> resolve(Collection<PathPart> parts) {
        return resolve(parts, Collections.<Object> emptySet());
    }

    /**
     * Expand the given collection of path parts, creating a collection
     * containing only expanded path parts that map to an actual resource,
     * excluding artifacts whose unversioned key is in the given set of
     * excludes.
     * 
     * @param parts
     *            The collection of path parts.
     * @param exclude
     *            A set of unversioned keys of artifacts to exclude.
     * @return An expanded collection of path parts.
     * @see PathPart#expand(Library, Collection, Collection)
     */
    public Collection<PathPart> resolve(Collection<PathPart> parts, Set<?> exclude) {
        Map<Object, PathPart> expanded = new LinkedHashMap<Object, PathPart>();
        Collection<PathPart> current = parts;
        Collection<PathPart> next = new ArrayList<PathPart>();
        while (!current.isEmpty()) {
            for (PathPart part : current) {
                Object key = part.getUnversionedKey();
                if (!(expanded.containsKey(key) || exclude.contains(key))) {
                    Collection<PathPart> expansions = new ArrayList<PathPart>();
                    part.expand(this, expansions, next);
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
    public ArtifactPart getArtifactPart(Artifact artifact) {
        for (File dir : dirs) {
            File deps = new File(dir, artifact.getPath("dep"));
            if (deps.exists()) {
                return new ArtifactPart(dir, artifact, Collections.<Exclude>emptySet());
            }
        }
        return null;
    }

    /**
     * Get an artifact part for the given artifact or null if the given artifact
     * cannot be found in the library search path. Each library in the search
     * path is searched in order. The first library to contain the artifact is
     * used to create the artifact part.
     * 
     * @param include
     *            An artifact it include with its dependency exclusions.
     * @param suffixes
     *            The suffixes of files that must be present to consider the
     *            artifact present.
     * @return An artifact part or null.
     */
    public ArtifactPart getArtifactPart(Include include, String...suffixes) {
        Artifact artifact = include.getArtifact();
        Map<String, File> versions = new HashMap<String, File>();
        for (int i = dirs.length - 1, stop = -1; i != stop; i--) {
            File dir = dirs[i];
            File unversioned = new File(dir, artifact.getUnversionedDirectoryPath());
            if (unversioned.isDirectory()) {
                FILES: for (String fileName : unversioned.list()) {
                    Artifact candidate = new Artifact(artifact.getGroup(),  artifact.getName(), fileName);
                    for (String suffix : suffixes) {
                        File file = new File(dir, candidate.getPath(suffix));
                        if (!file.exists()) {
                            continue FILES;
                        }
                    }
                    versions.put(fileName, dir);
                }
            }
        }
        String version = include.getVersionSelector().select(versions.keySet());
        if (version == null) {
            return null;
        }
        Artifact selected = new Artifact(artifact.getGroup(), artifact.getName(), version);
        return new ArtifactPart(versions.get(version), selected, include.getExcludes());
    }

    /**
     * The the library directory that contains a file with the given suffix for
     * the given artifact.
     * 
     * @param artifact
     *            The artifact.
     * @param suffix
     *            The slash delimited file suffix.
     * @return The first library directory that contains the file or null if the
     *         file is not found.
     */
    public File getArtifactDirectory(Artifact artifact, String suffix) {
        for (File dir : dirs) {
            File deps = new File(dir, artifact.getPath(suffix));
            if (deps.exists()) {
                return dir;
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
            return asList(dirs).equals(asList(((Library) object).dirs));
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
        return asList(dirs).hashCode();
    }
}
