package com.goodworkalan.go.go;

import java.util.List;

public class HelloDependencies extends Dependencies {
    @Override
    public List<Artifact> getArtifacts() {
        List<Artifact> artifacts = super.getArtifacts();
        artifacts.add(new Artifact("org.mockito", "mockito-core", "1.8"));
        return artifacts;
    }
}
