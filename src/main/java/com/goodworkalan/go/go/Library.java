package com.goodworkalan.go.go;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
     * @param extension
     *            The file extension for the specific artifact file.
     * @return True if the library contains the given artifact.
     */
    public boolean contains(Artifact artifact, String extension) {
        return new File(dir, artifact.getPath("", extension)).exists();
    }
    
    private void resolve(List<Repository> repositories, List<Artifact> artifacts, List<Artifact> dependencies, Set<String> seen) {
        if (!dependencies.isEmpty()) {
            List<Artifact> subDependencies = new ArrayList<Artifact>();
            for (Artifact dependency : dependencies) {
                if (!seen.contains(dependency.getKey())) {
                    seen.add(dependency.getKey());
                    artifacts.add(dependency);
                    if (!contains(dependency, "jar")) {
                        for (Repository repository : repositories) {
                            System.out.println(repository);
                        }
                    }
                    subDependencies.addAll(new POMReader(dir).getImmediateDependencies(dependency));
                }
            }
            resolve(repositories, artifacts, subDependencies, seen);
        }
    }

    public List<Artifact> resolve(List<Repository> repositories, List<Artifact> artifacts) {
        List<Artifact> dependencies = new ArrayList<Artifact>();
        resolve(repositories, dependencies, artifacts, new HashSet<String>());
        return dependencies;
    }
    
    public List<Artifact> resolve(List<Artifact> artifacts) {
        return resolve(Collections.<Repository>emptyList(), artifacts);
    }
    
    public ClassLoader getClassLoader(List<Artifact> artifacts, ClassLoader parent, Set<String> seen) {
        List<File> path = new ArrayList<File>();
        for (Artifact artifact : resolve(artifacts)) {
            if (!seen.contains(artifact.getKey())) {
                seen.add(artifact.getKey());
                path.add(new File(dir, artifact.getPath("", "jar")));
            }
        }
        if (path.isEmpty()) {
            return parent;
        }
        URL[] urls = new URL[path.size()];
        for (int i = 0, stop = path.size(); i < stop; i++) {
            try {
                urls[i] = new URL("jar:" + path.get(i).toURL().toExternalForm() + "!/");
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
