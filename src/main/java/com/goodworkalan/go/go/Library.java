package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoException.REPOSITORY_HAS_NO_URI_CONSTRUCTOR;
import static com.goodworkalan.go.go.GoException.UNABLE_TO_CONSTRUCT_REPOSITORY;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.goodworkalan.cassandra.Report;
import com.goodworkalan.reflective.ReflectiveException;
import com.goodworkalan.reflective.ReflectiveFactory;

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
    private final ReflectiveFactory reflectiveFactory = new ReflectiveFactory();

    private final Map<String, Class<? extends RepositoryClient>> repositoryClasses = new HashMap<String, Class<? extends RepositoryClient>>();

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

    RepositoryClient getRepositoryClient(RepositoryLine repository) {
        Class<? extends RepositoryClient> repositoryClass = repositoryClasses.get(repository.type);
        if (repositoryClass == null) {
        } 
        try {
            return reflectiveFactory.getConstructor(repositoryClass, URI.class).newInstance(repository.uri);
        } catch (ReflectiveException e) {
            switch (e.getCode() / 100) {
            case ReflectiveException.CANNOT_FIND:
                throw new GoException(REPOSITORY_HAS_NO_URI_CONSTRUCTOR, new Report(), e);
            default:
                throw new GoException(UNABLE_TO_CONSTRUCT_REPOSITORY, new Report(), e);
            }
        }
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
    
    private Collection<PathPart> resolve(Set<Artifact> artifacts, Catcher catcher) {
        Collection<PathPart> path = new ArrayList<PathPart>();
        for (Artifact artifact : artifacts) {
            path.add(new ResolutionPart(artifact));
        }
        return resolve(path, catcher);
    }

    private Collection<PathPart> resolve(Collection<PathPart> parts, Catcher catcher) {
        Map<Object, PathPart> expanded = new LinkedHashMap<Object, PathPart>();
        Collection<PathPart> current = parts;
        Collection<PathPart> next = new ArrayList<PathPart>();
        while (!current.isEmpty()) {
            for (PathPart part : current) {
                PathPart resolved;
                try {
                    resolved = part.expand(this, next);
                } catch (GoException e) {
                    catcher.examine(e);
                    continue;
                }
                if (!expanded.containsKey(resolved.getKey())) {
                    expanded.put(resolved.getKey(), resolved);
                }
            }
            current = next;
            next = new ArrayList<PathPart>();
        }
        return expanded.values();
    }

    public Set<Artifact> resolve(List<Transaction> transactions, Catcher fetchFailure) {
        Collection<PathPart> pathParts = new ArrayList<PathPart>();
        for (Transaction transaction : transactions) {
            pathParts.addAll(transaction.getPathParts());
        }
        Set<Artifact> artifacts = new LinkedHashSet<Artifact>();
        for (PathPart part : resolve(pathParts, fetchFailure)) {
            if (part.getArtifact() != null) {
                artifacts.add(part.getArtifact());
            }
        }
        return artifacts;
    }
    
    public Set<File> getFiles(Set<Artifact> artifacts, Set<String> seen) {
        Set<File> files = new LinkedHashSet<File>();
        for (PathPart part : resolve(artifacts, new Catcher())) {
            files.add(part.getFile());
        }
        return files;
    }
    
    public ClassLoader getClassLoader(Set<Artifact> artifacts, ClassLoader parent, Set<String> seen) {
        Collection<PathPart> path = resolve(artifacts, new Catcher());
        if (path.isEmpty()) {
            return parent;
        }
        URL[] urls = new URL[path.size()];
        int index = 0;
        for (PathPart part : path) {
            try {
                urls[index++] = part.getURL();
            } catch (MalformedURLException e) {
                throw new GoException(0, e);
            }
        }
        return new URLClassLoader(urls, parent);
    }
    
    public LibraryEntry getEntry(Artifact artifact, List<RepositoryLine> repositories) {
        File deps = new  File(dir, artifact.getPath("", "dep"));
        if (!deps.exists()) {
            for (RepositoryLine repository : repositories) {
                RepositoryClient repositoryClient = getRepositoryClient(repository);
                if (!deps.exists()) {
                    repositoryClient.fetchDependencies(this, artifact);
                }
                if (!(new File(dir, artifact.getPath("", "jar"))).exists()) {
                    repositoryClient.fetch(this, artifact, "", "jar");
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
