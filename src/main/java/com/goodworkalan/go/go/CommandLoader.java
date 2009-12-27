package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.Casts.taskClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Load the tasks from the task specification files found in the class path.
 * <p>
 * The builder is only used internally in the constructor of the
 * {@link CommandInterpreter}. It is separate so that the command interpreter
 * class does not have to maintain the members used only during task loading.
 * 
 * 
 * @author Alan Gutierrez
 */
final class CommandLoader {
    /** A set of keys of artifacts that have already been included. */
    private final Set<Object> seen = new HashSet<Object>();
    
    /** The set of class path URLs that have been inspected for commands. */
    private final Set<URL> urls = new HashSet<URL>();

    /** The Java-a-Go-Go library. */
    public final Library library = new Library(new File(System.getProperty("user.home") + "/.m2/repository"));
    
    /** The root map of command names to command responders. */
    public final Map<String, Responder> commands = new TreeMap<String, Responder>();

    /** The map of tasks to responders. */
    public final Map<Class<? extends Commandable>, Responder> responders = new HashMap<Class<? extends Commandable>, Responder>();

    private LibraryPath libraryPath;

    /**
     * Load the tasks found in the libraries specified in the given artifact
     * file. The library dependencies are also loaded.
     * 
     * @param artifactFile
     *            The artifact file.
     */
    public CommandLoader() {
        // FIXME Remove one of these.
        seen.add(new Artifact("com.goodworkalan/go-go").getKey().subList(0, 2));
        seen.add(new Artifact("com.goodworkalan/reflective").getKey().subList(0, 2));
        seen.add(new Artifact("com.goodworkalan/cassandra").getKey().subList(0, 2));
        
        libraryPath = library.emptyPath(seen);
        
        readConfigurations();
    }

    /**
     * Iterate through all of the command interpreter task and dependencies
     * definition files that can be found using the given class loader if they
     * definition files have not already been loaded. Any dependency classes
     * that specify new artifacts will have those artifacts loaded with the
     * given artifacts reader.
     * 
     * @param reader
     *            The artifacts reader.
     * @param classLoader
     *            The parent class loader.
     * @return A class loader the includes any newly specified artifacts or null
     *         if no new artifacts were specified.
     * @throws IOException
     *             For any I/O error while reading the command interpreter
     *             definition files.
     */
    public void addArtifacts(Artifact...artifacts) {
        List<Artifact> unseen = new ArrayList<Artifact>();
        for (Artifact artifact : artifacts) {
            Object key = artifact.getKey().subList(0, 2);
            if (!seen.contains(key)) {
                seen.add(key);
                unseen.add(artifact);
            }
        }
        if (!unseen.isEmpty()) {
            List<PathPart> parts = new ArrayList<PathPart>();
            for (Artifact artifact : unseen) {
                parts.add(new ResolutionPart(artifact));
            }
            libraryPath = libraryPath.extend(parts);
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            classLoader = libraryPath.getClassLoader(classLoader);
            Thread.currentThread().setContextClassLoader(classLoader);
            readConfigurations();
        }
    }

    private void readConfigurations() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources("META-INF/services/com.goodworkalan.go.go.Commandable");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (!urls.contains(url)) {
                    Set<Class<? extends Commandable>> tasks = new HashSet<Class<? extends Commandable>>();
                    
                    urls.add(url);
                    BufferedReader lines = new BufferedReader(new InputStreamReader(url.openStream()));
                    String className;
                    try {
                        while ((className = lines.readLine()) != null) {
                            if (className.trim().equals("")) {
                                continue;
                            }
                            Class<?> foundClass;
                            try {
                                foundClass = classLoader.loadClass(className);
                            } catch (ClassNotFoundException e) {
                                throw new GoException(0, e);
                            }
                            if (Commandable.class.isAssignableFrom(foundClass)) {
                                tasks.add(taskClass(foundClass));
                            } else {
                                throw new GoException(0);
                            }
                        }
                    } catch (IOException e) {
                        throw new GoException(0, e);
                    }
                    for (Class<? extends Commandable> taskClass : tasks) {
                        if (!responders.containsKey(taskClass)) {
                            Responder responder = new Responder(taskClass);
                            responders.put(taskClass, responder);
                            Class<? extends Commandable> parentTaskClass = null;
                            while ((parentTaskClass = responder.getParentTaskClass()) != null) {
                                Responder parent = responders.get(parentTaskClass);
                                if (parent == null) {
                                    parent = new Responder(parentTaskClass);
                                    responders.put(parentTaskClass, parent);
                                }
                                parent.addCommand(responder);
                                responder = parent;
                            }
                            // Will reassign sometimes, but that's okay.
                            commands.put(responder.getName(), responder);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new GoException(0, e);
        }
        for (Responder responder : responders.values()) {
            responder.checkEndlessRecursion(responders, new HashSet<Class<? extends Commandable>>());
        }
    }
}
