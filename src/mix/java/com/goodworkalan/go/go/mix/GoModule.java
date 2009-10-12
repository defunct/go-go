package com.goodworkalan.go.go.mix;

import com.goodworkalan.go.go.Artifact;
import com.goodworkalan.mix.BasicJavaModule;

public class GoModule extends BasicJavaModule {
    public GoModule() {
        super(new Artifact("com.goodworkalan", "go-go", "0.1"));
        addDependency(new Artifact("com.goodworkalan", "cassandra", "0.7"));
        addDependency(new Artifact("com.goodworkalan", "glob", "0.1"));
        addDependency(new Artifact("com.goodworkalan", "reflective", "0.1"));
        addTestDependency(new Artifact("org.slf4j", "slf4j-log4j12", "1.4.2"));
        addTestDependency(new Artifact("org.testng", "testng", "5.10"));
        addTestDependency(new Artifact("org.mockito", "mockito-core", "1.6"));
    }
}
