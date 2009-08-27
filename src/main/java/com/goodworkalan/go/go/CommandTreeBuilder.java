package com.goodworkalan.go.go;

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

public final class CommandTreeBuilder {
    private final Set<String> seen = new HashSet<String>();
    private final Map<String, Responder> commands = new TreeMap<String, Responder>();
    private final Set<Class<? extends Task>> tasks = new HashSet<Class<? extends Task>>();
    private final Set<URL> urls = new HashSet<URL>();
    private final Set<Class<?>> dependenciesClasses = new HashSet<Class<?>>();
    private final Map<Class<? extends Task>, Responder> responders = new HashMap<Class<? extends Task>, Responder>();
    private final Library library = new Library(new File(System.getProperty("user.home") + "/.m2/repository"));
    
    public CommandTreeBuilder(String artifactFile) {
        seen.add("com.goodworkalan/go-go");
        List<Artifact> artifacts = new ArrayList<Artifact>();
        if (artifactFile != null) {
            for (Transaction transaction : ArtifactsReader.read(new File(artifactFile))) {
                transaction.resolve(library);
                artifacts.addAll(transaction.getArtifacts());
            }
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (!artifacts.isEmpty()) {
            classLoader = library.getClassLoader(artifacts, classLoader, seen);
        }
        try {
            while ((classLoader = loadConfigurations(classLoader)) != null) {
            }
        } catch (IOException e) {
            throw new GoException(0, e);
        }
    }
    
    private ClassLoader resolve(Class<?> depenenciesClass, ClassLoader classLoader) {
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
        transaction.resolve(library);
        return library.getClassLoader(transaction.getArtifacts(), classLoader, seen);
    }
    
    private ClassLoader loadConfigurations(ClassLoader classLoader) throws IOException {
        boolean classLoaderDirty = false;
        Enumeration<URL> resources = classLoader.getResources("META-INF/services/com.goodworkalan.go.go.CommandInterpreter");
        while (resources.hasMoreElements())
        {
            URL url = resources.nextElement();
            if (!urls.contains(url)) {
                urls.add(url);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String className;
                try {
                    while ((className = reader.readLine()) != null) {
                        Class<?> foundClass;
                        try {
                            foundClass = classLoader.loadClass(className);
                        } catch (ClassNotFoundException e) {
                            throw new GoException(0, e);
                        }
                        if (Dependencies.class.isAssignableFrom(foundClass)) {
                            if (!dependenciesClasses.contains(foundClass)) {
                                dependenciesClasses.add(foundClass);
                                classLoader = resolve(foundClass, classLoader);
                                classLoaderDirty = true;
                            }
                        } else if (Task.class.isAssignableFrom(foundClass)) {
                            System.out.println(getClass().getClassLoader());
                            System.out.println(foundClass.getClassLoader());
                            System.out.println(classLoader);
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

    @SuppressWarnings("unchecked")
    private static Class<? extends Task> taskClass(Class taskClass) {
        return taskClass;
    }
    
    public Map<String, Responder> getCommands() {
        return commands;
    }
}
