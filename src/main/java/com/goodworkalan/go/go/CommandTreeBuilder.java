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
final class CommandTreeBuilder {
    /** A set of keys of artifacts that have already been included. */
    private final Set<String> seen = new HashSet<String>();
    
    /** The set of class path URLs that have been inspected for commands. */
    private final Set<URL> urls = new HashSet<URL>();

    /**
     * The set of dependency specification that have been inspected for imports.
     */
    private final Set<Class<?>> dependenciesClasses = new HashSet<Class<?>>();
    
    /** The Java-a-Go-Go library. */
    private final Library library = new Library(new File(System.getProperty("user.home") + "/.m2/repository"));
    
    /** The root map of command names to command responders. */
    public final Map<String, Responder> commands = new TreeMap<String, Responder>();

    /** The map of tasks to responders. */
    public final Map<Class<? extends Task>, Responder> responders = new HashMap<Class<? extends Task>, Responder>();

    /**
     * Load the tasks found in the libraries specified in the given artifact
     * file. The library dependencies are also loaded.
     * 
     * @param artifactFile
     *            The artifact file.
     */
    public CommandTreeBuilder(String artifactFile) {
        seen.add("com.goodworkalan/go-go");
        List<Artifact> artifacts = new ArrayList<Artifact>();
        ArtifactsReader reader = new ArtifactsReader();
        if (artifactFile != null) {
            artifacts.addAll(library.resolve(reader, new File(artifactFile), new Catcher()));
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (!artifacts.isEmpty()) {
            System.out.println(artifacts);
            classLoader = library.getClassLoader(artifacts, classLoader, seen);
        }
        try {
            while ((classLoader = loadConfigurations(reader, classLoader)) != null) {
            }
        } catch (IOException e) {
            throw new GoException(0, e);
        }
    }

    /**
     * Create a class loader that includes any unseen libraries specified by the
     * given dependencies object.
     * 
     * @param reader
     *            The artifacts reader.
     * @param depenenciesClass
     *            The dependency class to instantiate and query for
     *            dependencies.
     * @param classLoader
     *            The current thread class loader, used as a parent class loader
     *            for any dependencies.
     * @return A class loader that includes any new libraries added by the
     *         dependencies object.
     */
    private ClassLoader resolve(ArtifactsReader reader, Class<?> depenenciesClass, ClassLoader classLoader) {
        Dependencies dependencies;
        try {
            dependencies = (Dependencies) depenenciesClass.newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new GoException(0, e);
        }
        Transaction transaction = new Transaction();
        dependencies.configure(transaction);
        library.resolve(transaction);
        // FIXME Not taking into account artifacts already loaded, but we
        // we can do so using the getURLs method, or maybe we need to provide
        // a the seen hash, since URLs include version.
        // FIXME Seen hash should come in with the constructor.
        return library.getClassLoader(transaction.getArtifacts(), classLoader, seen);
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
    private ClassLoader loadConfigurations(ArtifactsReader reader, ClassLoader classLoader) throws IOException {
        boolean classLoaderDirty = false;
        Enumeration<URL> resources = classLoader.getResources("META-INF/services/com.goodworkalan.go.go.CommandInterpreter");
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            if (!urls.contains(url)) {
                Set<Class<? extends Task>> tasks = new HashSet<Class<? extends Task>>();
                
                urls.add(url);
                BufferedReader lines = new BufferedReader(new InputStreamReader(url.openStream()));
                String className;
                try {
                    while ((className = lines.readLine()) != null) {
                        System.out.println(className);
                        Class<?> foundClass;
                        try {
                            foundClass = classLoader.loadClass(className);
                        } catch (ClassNotFoundException e) {
                            throw new GoException(0, e);
                        }
                        if (Dependencies.class.isAssignableFrom(foundClass)) {
                            if (!dependenciesClasses.contains(foundClass)) {
                                dependenciesClasses.add(foundClass);
                                classLoader = resolve(reader, foundClass, classLoader);
                                classLoaderDirty = true;
                            }
                        } else if (Task.class.isAssignableFrom(foundClass)) {
                            tasks.add(taskClass(foundClass));
                        } else {
                            throw new GoException(0);
                        }
                    }
                } catch (IOException e) {
                    throw new GoException(0, e);
                }
                for (Class<? extends Task> taskClass : tasks) {
                    if (!responders.containsKey(taskClass)) {
                        Responder responder = new Responder(taskClass);
                        responders.put(taskClass, responder);
                        Class<? extends Task> parentTaskClass = null;
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
        return classLoaderDirty ? classLoader : null;
    }
}
