package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoException.UNRESOLVED_ARTIFACT;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ResolutionPart implements PathPart {
    private final Include include;
    
    public ResolutionPart(Include include) {
        this.include = include;
    }

    public ResolutionPart(Artifact artifact) {
        this(new Include(artifact));
    }

    public ResolutionPart(Artifact include, Set<Artifact> excludes) {
        this(new Include(include, excludes));
    }
    
    public File getFile() {
        throw new UnsupportedOperationException();
    }
    
    public URL getURL() throws MalformedURLException {
        throw new UnsupportedOperationException();
    }
    
    public Artifact getArtifact() {
        return include.getArtifact();
    }
    
    public Collection<PathPart> expand(Library library, Collection<PathPart> additional) {
        LibraryEntry entry = library.getEntry(include.getArtifact());
        if (entry == null) {
            throw new GoException(UNRESOLVED_ARTIFACT).put("artifact", include.getArtifact());
        }
        for (Include include : Artifacts.read(new File(entry.getDirectory(), entry.getArtifact().getPath("dep")))) {
            if (!this.include.getExcludes().contains(include.getArtifact())) {
                Set<Artifact> subExcludes = new HashSet<Artifact>();
                subExcludes.addAll(this.include.getExcludes());
                subExcludes.addAll(include.getExcludes());
                additional.add(new ResolutionPart(include.getArtifact(), subExcludes));
            }
        }
        return Collections.<PathPart>singletonList(new ArtifactPart(entry.getDirectory(), entry.getArtifact()));
    }
    
    public Object getKey() {
        throw new UnsupportedOperationException();
    }
}
