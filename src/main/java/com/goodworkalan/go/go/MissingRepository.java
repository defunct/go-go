package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoException.MISSING_REPOSITORY_IMPLEMENTATION;

import java.net.URI;

public class MissingRepository implements RepositoryClient {
    private final String type;
    private final URI uri;
    public MissingRepository(String type, URI uri) {
        this.type = type;
        this.uri = uri;
    }
    public void fetchDependencies(Library library, Artifact artifact) {
        throw new GoException(MISSING_REPOSITORY_IMPLEMENTATION)
            .put("type", type)
            .put("uri", uri);
    }

    public void fetch(Library library, Artifact artifact, String suffix, String extension) {
        throw new GoException(MISSING_REPOSITORY_IMPLEMENTATION)
            .put("type", type)
            .put("uri", uri);
    }
}
