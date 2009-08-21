package com.goodworkalan.go.go;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * Make sense of the command line.
 * 
 * @author Alan Gutierrez
 */
public class CommandInterpreter {
    private final TaskFactory taskFactory = new ReflectionTaskFactory();

    public CommandInterpreter() {
    }
    
    @SuppressWarnings("unchecked")
    private static Class<? extends Task> taskClass(Class taskClass) {
        return taskClass;
    }

    public void main(String...arguments) {
        Set<Class<? extends Task>> tasks = new HashSet<Class<? extends Task>>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/snap.go.go")));
        String className;
        try {
            while ((className = reader.readLine()) != null) {
                Class<?> foundClass;
                try {
                    foundClass = getClass().getClassLoader().loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new GoException(0, e);
                }
                if (!Task.class.isAssignableFrom(foundClass)) {
                    throw new GoException(0);
                }
                tasks.add(taskClass(foundClass));
            }
        } catch (IOException e) {
            throw new GoException(0, e);
        }
        Map<Class<? extends Task>, Responder> responders = new HashMap<Class<? extends Task>, Responder>();
        Map<String, Responder> commands = new TreeMap<String, Responder>();
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
        Responder responder = commands.get(arguments[0]);
        if (responder == null) {
            throw new GoException(0);
        }
        LinkedList<List<String>> unusedArguments = new LinkedList<List<String>>();
        unusedArguments.add(new ArrayList<String>());
        Task task = taskFactory.newTask(responder.getTaskClass());
        int i, stop;
        for (i = 1, stop = arguments.length; responder != null && i < stop; i++) {
            String argument = arguments[i];
            if (argument.startsWith("--")) {
                String[] pair = argument.substring(2).split("=", 2);
                String[] qualified = pair[0].split(":");
                if (qualified.length > 2) {
                    throw new GoException(0);
                }
                if (qualified.length == 1 || qualified[0].equals(responder.getName())) {
                    responder.setArgument(task, qualified[qualified.length - 1], pair[1]);
                } else {
                    unusedArguments.getLast().add(argument);
                }
            } else {
                responder = responder.getCommand(argument);
                if (responder != null) {
                    Task subTask = taskFactory.newTask(responder.getTaskClass());
                    responder.setParent(subTask, task);
                    task = subTask;
                    unusedArguments.addLast(new ArrayList<String>());
                }
            }
        }
        task.execute(stringArrayArray(unusedArguments), remainingArguments(arguments, i));
    }
    
    private static String[] remainingArguments(String[] arguments, int start) {
        String[] remaining = new String[arguments.length - start];
        System.arraycopy(arguments, start, remaining, 0, arguments.length - start);
        return remaining;
    }

    private static String[][] stringArrayArray(List<List<String>> unusedArguments) {
        String[][] unused = new String[unusedArguments.size()][];
        int index = 0;
        for (List<String> args : unusedArguments) {
            unused[index++] = args.toArray(new String[args.size()]);
        }
        return unused;
    }
}
