package com.goodworkalan.go.go;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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
    
    private void resolve(ArtifactsReader reader, List<Artifact> artifacts, List<Transaction> dependencies, Set<String> seen, Catcher fetchFailure) {
        if (!dependencies.isEmpty()) {
            List<Transaction> subDependencies = new ArrayList<Transaction>();
            for (Transaction transaction : dependencies) {
                for (Artifact dependency : transaction.getArtifacts()) {
                    if (!seen.contains(dependency.getKey())) {
                        // FIXME Excludes go here, as a separate hash.
                        seen.add(dependency.getKey());
                        artifacts.add(dependency);
                        try {
                            if (!getFile(dependency, "", "jar").exists()) {
                                for (Repository repository : transaction.getRepositories()) {
                                    if (!getFile(dependency, "", "dep").exists()) {
                                        repository.fetchDependencies(this, dependency);
                                    }
                                    if (!getFile(dependency, "", "jar").exists()) {
                                        repository.fetch(this, dependency, "", "jar");
                                    }
                                }
                            }
                        } catch (GoException e) {
                            fetchFailure.examine(e);
                            continue;
                        }
                        subDependencies.addAll(reader.read(getFile(dependency, "", "dep")));
                    }
                }
            }
            resolve(reader, artifacts, subDependencies, seen, fetchFailure);
        }
    }

    public List<Artifact> resolve(Transaction transaction) {
        ArtifactsReader reader = new ArtifactsReader();
        List<Artifact> dependencies = new ArrayList<Artifact>();
        resolve(reader, dependencies, Collections.singletonList(transaction), new HashSet<String>(), new Catcher());
        return dependencies;
    }

    public List<Artifact> resolve(ArtifactsReader reader, File file, Catcher fetchFailure) {
        List<Artifact> dependencies = new ArrayList<Artifact>();
        resolve(reader, dependencies, reader.read(file), new HashSet<String>(), fetchFailure);
        return dependencies;
    }
    
    public Set<File> getFiles(List<Artifact> artifacts, Set<String> seen) {
        Catcher catcher = new Catcher();
        ArtifactsReader reader = new ArtifactsReader();
        Set<File> path = new LinkedHashSet<File>();
        for (Artifact resolve : artifacts) {
            List<Artifact> resolved = new ArrayList<Artifact>();
            resolved.add(resolve);
            resolved.addAll(resolve(reader, new File(dir, resolve.getPath("", "dep")), catcher));
            for (Artifact artifact : resolved) {
                if (!seen.contains(artifact.getKey())) {
                    seen.add(artifact.getKey());
                    path.add(new File(dir, artifact.getPath("", "jar")));
                }
            }
        }
        return path;
    }
    
    public ClassLoader getClassLoader(List<Artifact> artifacts, ClassLoader parent, Set<String> seen) {
        Set<File> path = getFiles(artifacts, seen);
        if (path.isEmpty()) {
            return parent;
        }
        URL[] urls = new URL[path.size()];
        int index = 0;
        for (File part : path) {
            try {
                urls[index++] = new URL("jar:" + part.toURL().toExternalForm() + "!/");
            } catch (MalformedURLException e) {
                throw new GoException(0, e);
            }
        }
        return new URLClassLoader(urls, parent);
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
