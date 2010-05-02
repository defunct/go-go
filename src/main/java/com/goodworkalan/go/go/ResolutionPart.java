package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoException.UNRESOLVED_ARTIFACT;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResolutionPart extends ExpandingPathPart {
    private final Include include;
    
    public ResolutionPart(Include include) {
        this.include = include;
    }

    public ResolutionPart(Artifact artifact) {
        this(new Include(artifact));
    }

    public ResolutionPart(Artifact artifact, Set<List<String>> excludes) {
        this(new Include(artifact, excludes));
    }
    
    public Artifact getArtifact() {
        return include.getArtifact();
    }
    
    public Collection<PathPart> expand(Library library, Collection<PathPart> additional) {
        ArtifactPart entry = library.getPathPart(include.getArtifact());
        if (entry == null) {
            throw new GoException(UNRESOLVED_ARTIFACT, include.getArtifact());
        }
        for (Include subInclude : Artifacts.read(new File(entry.getLibraryDirectory(), entry.getArtifact().getPath("dep")))) {
            if (!include.getExcludes().contains(subInclude.getArtifact().getUnversionedKey())) {
                Set<List<String>> subExcludes = new HashSet<List<String>>();
                subExcludes.addAll(include.getExcludes());
                subExcludes.addAll(subInclude.getExcludes());
                additional.add(new ResolutionPart(subInclude.getArtifact(), subExcludes));
            }
        }
        return Collections.<PathPart>singletonList(new ArtifactPart(entry.getLibraryDirectory(), entry.getArtifact()));
    }
    
    public Object getUnversionedKey() {
        return include.getArtifact().getKey().subList(0, 2);
    }
}
