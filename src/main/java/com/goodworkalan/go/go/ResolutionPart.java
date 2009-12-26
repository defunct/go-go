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

    public ResolutionPart(Artifact artifact, Set<Artifact> excludes) {
        this(new Include(artifact, excludes));
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
        for (Include subInclude : Artifacts.read(new File(entry.getDirectory(), entry.getArtifact().getPath("dep")))) {
            if (!include.getExcludes().contains(subInclude.getArtifact())) {
                Set<Artifact> subExcludes = new HashSet<Artifact>();
                subExcludes.addAll(include.getExcludes());
                subExcludes.addAll(subInclude.getExcludes());
                additional.add(new ResolutionPart(subInclude.getArtifact(), subExcludes));
            }
        }
        return Collections.<PathPart>singletonList(new ArtifactPart(entry.getDirectory(), entry.getArtifact()));
    }
    
    public Object getUnversionedKey() {
        return include.getArtifact().getKey().subList(0, 2);
    }
}
