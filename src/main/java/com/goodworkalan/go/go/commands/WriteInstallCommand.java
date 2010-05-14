package com.goodworkalan.go.go.commands;

import static com.goodworkalan.go.go.GoError.CANNOT_CREATE_BOOT_CONFIGURATION_DIRECTORY;
import static com.goodworkalan.go.go.GoError.CANNOT_WRITE_BOOT_CONFIGURATION;

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
import com.goodworkalan.go.go.MetaCommand;
import com.goodworkalan.go.go.library.Artifact;
import com.goodworkalan.go.go.library.ArtifactPart;
import com.goodworkalan.ilk.Ilk;

public class WriteInstallCommand implements Commandable {
    /** The default constructor. */
    public WriteInstallCommand() {
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> extendsClassCast(Class<T> targetClass, Class<?> unknownClass) { 
        if (!targetClass.isAssignableFrom(unknownClass)) {
            throw new ClassCastException();
        }
        return (Class<? extends T>) unknownClass;
    }

    public void execute(Environment env) {
        List<ArtifactPart> artifactParts = env.get(new Ilk<List<ArtifactPart>>() {}, 1);
        for (ArtifactPart artifactPart : artifactParts) {
            record(env, artifactPart);
        }
    }
    
    public void record(Environment env, ArtifactPart artifactPart) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Artifact artifact = artifactPart.getArtifact();
        List<String> commands = readCommands(env, classLoader, new File(artifactPart.getLibraryDirectory(), artifact.getPath("jar")));
        File gogo = new File(env.library.getDirectories()[0], "go-go");
        File group = new File(gogo, artifact.getGroup());
        if (!group.isDirectory() && !group.mkdirs()) {
            throw new GoError(CANNOT_CREATE_BOOT_CONFIGURATION_DIRECTORY, group);
        }
        StringBuilder line = new StringBuilder();
        line.append(artifact).append(" ");
        String separator = "";
        for (String command : commands) {
            line.append(separator).append(command);
            separator = ", ";
        }
        File configuration = new File(group, artifact.getName() + ".go");
        writeCommands(line.toString(), configuration);
        env.io.out.println(line);
    }

    static void writeCommands(String line, File configuration) {
        try {
            Writer writer = new FileWriter(configuration);
            writer.write(line);
            writer.write("\n");
            writer.close();
        } catch (IOException e) {
            throw new GoError(CANNOT_WRITE_BOOT_CONFIGURATION, e, configuration);
        }
    }

    /**
     * Read the commandable classes provided by the given jar file and use the
     * meta command information to create a list of available command paths.
     * 
     * @param env
     *            The I/O bouquet for debugging.
     * @param classLoader
     *            The class loader to use to load commandable classes.
     * @param jar
     *            The jar file to read.
     * @return
     */
    static List<String> readCommands(Environment env, ClassLoader classLoader, File jar) {
        List<String> commands = new ArrayList<String>();
        try {
            ZipFile zip = new ZipFile(jar);
            ZipEntry entry = zip.getEntry("META-INF/services/com.goodworkalan.go.go.Commandable");
            if (entry != null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(zip.getInputStream(entry)));
                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.length() != 0) {
                        Class<? extends Commandable> commandableClass = loadCommandable(classLoader, line);
                        MetaCommand responder = env.getMetaCommand(commandableClass);
                        LinkedList<String> path = new LinkedList<String>();
                        for (;;) {
                            path.addFirst(responder.getName());
                            commandableClass = responder.getParentCommandClass();
                            if (commandableClass == null) {
                                break; 
                            }
                            responder = env.getMetaCommand(commandableClass);
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
            throw new GoError(GoError.CANNOT_READ_JAR_ARCHIVE, e, jar);
        }
        return commands;
    }

    static Class<? extends Commandable> loadCommandable(ClassLoader classLoader, String line) {
        try {
            return extendsClassCast(Commandable.class, classLoader.loadClass(line));
        } catch (ClassNotFoundException e) {
            // We know that the class has been loaded because
            // the artifact has been added to the class path.
            throw new RuntimeException(e);
        }
    }
}
