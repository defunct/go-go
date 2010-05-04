package com.goodworkalan.go.go.commands;

import static com.goodworkalan.go.go.GoError.CANNOT_CREATE_BOOT_CONFIGURATION_DIRECTORY;
import static com.goodworkalan.go.go.GoError.CANNOT_WRITE_BOOT_CONFIGURATION;
import static com.goodworkalan.go.go.GoException.CANNOT_FIND_RESPONDER;

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

import com.goodworkalan.go.go.Commandable;
import com.goodworkalan.go.go.Environment;
import com.goodworkalan.go.go.GoError;
import com.goodworkalan.go.go.GoException;
import com.goodworkalan.go.go.MetaCommand;
import com.goodworkalan.go.go.library.Artifact;
import com.goodworkalan.go.go.library.ArtifactPart;

public class WriteInstallCommand implements Commandable {
    private final Artifact artifact;
    
    public WriteInstallCommand(Artifact artifact) {
        this.artifact = artifact;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> extendsClassCast(Class<T> targetClass, Class<?> unknownClass) { 
        if (!targetClass.isAssignableFrom(unknownClass)) {
            throw new ClassCastException();
        }
        return (Class<? extends T>) unknownClass;
    }

    public void execute(Environment env) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ArtifactPart found = env.library.getArtifactPart(artifact);
        List<String> commands = new ArrayList<String>();
        try {
            ZipFile zip = new ZipFile(new File(found.getLibraryDirectory(), found.getArtifact().getPath("jar")));
            ZipEntry entry = zip.getEntry("META-INF/services/com.goodworkalan.go.go.Commandable");
            if (entry != null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(zip.getInputStream(entry)));
                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.length() != 0) {
                        Class<? extends Commandable> commandableClass;
                        try {
                            commandableClass = extendsClassCast(Commandable.class, classLoader.loadClass(line));
                        } catch (ClassNotFoundException e) {
                            throw new GoException(0, e);
                        } catch (ClassCastException e) {
                            throw new GoException(0, e);
                        }
                        MetaCommand responder = env.getMetaCommand(commandableClass);
                        if (responder == null) {
                            throw new GoException(CANNOT_FIND_RESPONDER, commandableClass.getCanonicalName());
                        }
                        LinkedList<String> path = new LinkedList<String>();
                        for (;;) {
                            path.addFirst(responder.getName());
                            commandableClass = responder.getParentCommandClass();
                            if (commandableClass == null) {
                                break; 
                            }
                            responder = env.getMetaCommand(commandableClass);
                            if (responder == null) {
                                throw new GoException(CANNOT_FIND_RESPONDER, commandableClass.getCanonicalName());
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
        File gogo = new File(env.library.getDirectories()[0], "go-go");
        File group = new File(gogo, artifact.getGroup());
        if (!group.isDirectory() && !group.mkdirs()) {
            throw new GoError('a', CANNOT_CREATE_BOOT_CONFIGURATION_DIRECTORY, group);
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
            throw new GoError('a', CANNOT_WRITE_BOOT_CONFIGURATION, e, configuration);
        }
        env.io.out.println(line);
    }
}
