package com.goodworkalan.go.go.mix;

import com.goodworkalan.mix.ProjectModule;
import com.goodworkalan.mix.builder.Builder;
import com.goodworkalan.mix.builder.JavaProject;

public class GoProject extends ProjectModule {
    @Override
    public void build(Builder builder) {
        builder
            .cookbook(JavaProject.class)
                .produces("com.github.bigeasy.go-go/go-go/0.1.4")
                .main()
                    .depends()
                        .include("com.github.bigeasy.reflective/reflective/0.1")
                        .include("com.github.bigeasy.danger/danger/0.1")
                        .include("com.github.bigeasy.retry/retry/0.1")
                        .include("com.github.bigeasy.class-boxer/class-boxer/0.1")
                        .include("com.github.bigeasy.infuse/infuse/0.1")
                        .end()
                    .end()
                .test()
                    .depends()
                        .include("com.github.bigeasy.comfort-io/comfort-io/0.1.1")
                        .include("org.slf4j/slf4j-log4j12/1.4.2")
                        .include("org.testng/testng-jdk15/5.10")
                        .include("org.mockito/mockito-core/1.6")
                        .end()
                    .end()
                .end()
            .end();
    }
}
