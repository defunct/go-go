package com.goodworkalan.go.go.mix;

import com.goodworkalan.mix.ProjectModule;
import com.goodworkalan.mix.builder.Builder;
import com.goodworkalan.mix.cookbook.JavaProject;

/**
 * Build definition for Jav-a-Go-Go.
 *
 * @author Alan Gutierrez
 */
public class GoProject implements ProjectModule {
    /**
     * Build the project definition for Jav-a-Go-Go.
     *
     * @param builder
     *          The project builder.
     */
    public void build(Builder builder) {
        builder
            .cookbook(JavaProject.class)
                .produces("com.github.bigeasy.go-go/go-go/0.1.4.25")
                .depends()
                    .production("com.github.bigeasy.reflective/reflective-setter/0.+1")
                    .production("com.github.bigeasy.ilk/ilk/0.+1")
                    .production("com.github.bigeasy.class/class-boxer/0.+1")
                    .production("com.github.bigeasy.infuse/infuse/0.+1")
                    .development("com.github.bigeasy.comfort-io/comfort-io/0.+1.1")
                    .development("org.slf4j/slf4j-log4j12/1.4.2")
                    .development("org.testng/testng-jdk15/5.10")
                    .development("org.mockito/mockito-core/1.6")
                    .end()
                .end()
            .end();
    }
}
