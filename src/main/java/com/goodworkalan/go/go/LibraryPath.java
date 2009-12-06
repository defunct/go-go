package com.goodworkalan.go.go;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class LibraryPath {
    private final Library library;
    
    private final Collection<PathPart> parts;
    
    private final Set<Object> excludes;
    
    public LibraryPath(Library library, Collection<PathPart> parts, Set<Object> excludes) {
        this.library = library;
        this.parts = parts;
        this.excludes = excludes;
    }
    
    public Set<File> getFiles() {
        Set<File> files = new LinkedHashSet<File>();
        for (PathPart part : parts) {
            files.add(part.getFile());
        }
        return files;
    }
    
    public URL[] getURLs() {
        Set<URL> files = new LinkedHashSet<URL>();
        for (PathPart part : parts) {
            try {
                files.add(part.getURL());
            } catch (MalformedURLException e) {
                throw new GoException(0, e);
            }
        }
        return files.toArray(new URL[files.size()]);
    }
    
    public ClassLoader getClassLoader(ClassLoader parent) {
        if (parts.isEmpty()) {
            return parent;
        }
        return new URLClassLoader(getURLs(), parent);
    }
    
    public List<Artifact> getArtifacts() {
        List<Artifact> artifacts = new ArrayList<Artifact>();
        for (PathPart part : parts) {
            if (part.getArtifact() != null) {
                artifacts.add(part.getArtifact());
            }
        }
        return artifacts;
    }
    
    public LibraryPath extend(PathPart part) {
        return extend(Collections.singletonList(part));
    }
    
    public LibraryPath extend(Collection<PathPart> parts) {
        return extend(parts, Collections.emptySet());
    }
    
    public LibraryPath extend(Collection<PathPart> parts, Set<Object> excludes) {
        Set<Object> subExcludes = new HashSet<Object>();
        subExcludes.add(this.excludes);
        subExcludes.add(excludes);
        Collection<PathPart> subParts = new ArrayList<PathPart>();
        subParts.addAll(this.parts);
        subParts.addAll(parts);
        return library.resolve(subParts, subExcludes);
    }
    
    public Set<Object> getExcludes() {
        return Collections.unmodifiableSet(excludes);
    }
}
