package com.goodworkalan.reflective.mix;

import com.goodworkalan.go.go.Environment;
import com.goodworkalan.go.go.library.Include;
import com.goodworkalan.go.go.library.ArtifactPart;
import com.goodworkalan.go.go.library.ResolutionPart;
import com.goodworkalan.go.go.library.PathPart;
import com.goodworkalan.mix.Mix;
import com.goodworkalan.mix.Project;
import com.goodworkalan.mix.ProjectModule;
import com.goodworkalan.mix.builder.Executable;
import com.goodworkalan.mix.builder.Builder;
import com.goodworkalan.mix.builder.JavaProject;
import com.goodworkalan.comfort.io.Files;
import java.util.Collections;
import java.util.TreeSet;
import java.util.Set;
import java.io.File;

public class GoGoBootProject extends ProjectModule {
    @Override
    public void build(Builder builder) {
        builder
            .cookbook(JavaProject.class)
                .produces("com.github.bigeasy.go-go/go-go-boot/0.1.2.12")
                .development("org.testng/testng-jdk15/5.10")
                .end()
            .end();
        builder
            .recipe("dependencies")
                .executable(new Executable() {
                    public void execute(Environment env) {
                        ArtifactPart artifactPart = env.library.getArtifactPart(new Include("com.github.bigeasy.go-go/go-go/+0"), "dep", "jar");
                        Set<String> artifacts = new TreeSet<String>();
                        for (PathPart part : env.library.resolve(Collections.<PathPart>singleton(new ResolutionPart(artifactPart.getArtifact())))) {
                            artifacts.add(part.getArtifact().toString()); 
                        }
                        File dependencies = new File("src/main/resources/go/dependencies.txt");
                        dependencies.getParentFile().mkdirs();
                        Files.pour(dependencies, artifacts);
                        env.io.out.println(artifacts);
                    }
                })
                .end()
            .end();
    }
}
