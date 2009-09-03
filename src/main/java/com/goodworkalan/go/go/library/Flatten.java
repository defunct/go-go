package com.goodworkalan.go.go.library;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.goodworkalan.glob.Find;
import com.goodworkalan.go.go.Artifact;
import com.goodworkalan.go.go.Command;
import com.goodworkalan.go.go.Environment;
import com.goodworkalan.go.go.POMReader;
import com.goodworkalan.go.go.Task;

/**
 * Convert a Maven repository into a Jav-a-Go-Go library by converting Maven POM
 * files into Jav-a-Go-Go dependency files.
 */
@Command(parent = Lib.class)
public class Flatten extends Task {
    /**
     * Recurse the Maven repository structure converting Maven POM files into
     * Jav-a-Go-Go depenecy files if the Jav-a-Go-Go file does not already
     * exist.
     * 
     * @param environment
     *            The execution enviornment.
     */
    @Override
    public void execute(Environment environment) {
        for (String repository : environment.remaining) {
            File directory = new File(repository);
            POMReader reader = new POMReader(directory);
            if (directory.isDirectory()) {
                for (File file : new Find().include("**/*.pom").find(directory)) {
                    Artifact artifact = Artifact.parse(file);
                    if (artifact != null) {
                        File deps = new File(directory, artifact.getPath("", "dep"));
                        if (artifact.getPath("", "pom").equals(file.toString()) && !deps.exists()) {
                            flatten(environment, reader, artifact, deps);
                        }
                    }
                }
            }
        }
    }

    /**
     * Write the artifact dependencies to the given dependency file. This method
     * was extracted to test I/O failure.
     * 
     * @param environment
     *            The execution environment.
     * @param reader
     *            The POM reader.
     * @param artifact
     *            The artifact to whose dependencies will be read from a Maven
     *            POM.
     * @param deps
     *            The dependency file.
     */
    void flatten(Environment environment, POMReader reader, Artifact artifact, File deps) {
        try {
            FileWriter writer = new FileWriter(deps);
            for (Artifact dependency : reader.getImmediateDependencies(artifact)) {
                writer.write("+ ");
                writer.write(dependency.getGroup());
                writer.write(" ");
                writer.write(dependency.getName());
                writer.write(" ");
                writer.write(dependency.getVersion());
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            environment.err.println("Unable to flatten POM for artifact " + artifact.toString());
            e.printStackTrace(environment.err);
        }
    }
}
