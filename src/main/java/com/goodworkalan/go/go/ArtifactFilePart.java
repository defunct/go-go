package com.goodworkalan.go.go;

import java.io.File;
import java.util.Collection;

public class ArtifactFilePart extends ExpandingPathPart {
    private final File file;

    public ArtifactFilePart(File file) {
        this.file = file;
    }

    public Collection<PathPart> expand(Library library, Collection<PathPart> expand) {
        return new TransactionsPart(Artifacts.read(file)).expand(library, expand);
    }
}
