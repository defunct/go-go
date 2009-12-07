package com.goodworkalan.go.go;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Command(parent = GoCommand.class)
public class InstallCommand implements Commandable {
    private Artifact artifact;
    
    @Argument
    public void addArtifact(Artifact artifact) {
        this.artifact = artifact;
    }
    
    public void execute(Environment env) {
        CommandLoader loader = new CommandLoader();
        loader.addArtifacts(artifact);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        LibraryEntry found = env.part.getCommandInterpreter().getLibrary().getEntry(artifact);
        List<String> commands = new ArrayList<String>();
        try {
            ZipFile zip = new ZipFile(new File(found.getDirectory(), found.getArtifact().getPath("jar")));
            ZipEntry entry = zip.getEntry("META-INF/services/com.goodworkalan.go.go.CommandInterpreter");
            if (entry != null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(zip.getInputStream(entry)));
                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.length() != 0) {
                        Class<?> taskClass;
                        try {
                            taskClass = classLoader.loadClass(line);
                        } catch (ClassNotFoundException e) {
                            throw new GoException(0, e);
                        }
                        Responder responder = loader.responders.get(taskClass);
                        if (responder == null) {
                            throw new GoError(0);
                        }
                        LinkedList<String> path = new LinkedList<String>();
                        for (;;) {
                            path.addFirst(responder.getName());
                            taskClass = responder.getParentTaskClass();
                            if (taskClass == null) {
                                break; 
                            }
                            responder = loader.responders.get(taskClass);
                            if (responder == null) {
                                throw new GoError(0);
                            }
                        }
                        StringBuilder command = new StringBuilder();
                        String separator = "";
                        for (String part : path) {
                            command.append(separator).append(part);
                            separator = " ";
                        }
                        commands.add(command.toString());
                    }
                }
            }
        } catch (IOException e) {
            throw new GoException(0, e);
        }
        Library library = env.part.getCommandInterpreter().getLibrary();
        File gogo = new File(library.getDirectory(), "go-go");
        File group = new File(gogo, artifact.getGroup());
        if (!group.isDirectory() && !group.mkdirs()) {
            throw new GoError(0);
        }
        StringBuilder line = new StringBuilder();
        line.append(artifact).append(" ");
        String separator = "";
        for (String command : commands) {
            line.append(separator).append(command);
            separator = ", ";
        }
        File configuration = new File(group, artifact.getName() + ".go");
        try {
            Writer writer = new FileWriter(configuration);
            writer.write(line.toString());
            writer.write("\n");
            writer.close();
        } catch (IOException e) {
            throw new GoError(0, e);
        }
    }
}
