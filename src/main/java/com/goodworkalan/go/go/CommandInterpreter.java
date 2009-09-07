package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.Casts.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Make sense of the command line.
 * 
 * @author Alan Gutierrez
 */
public final class CommandInterpreter {
    final TaskFactory taskFactory = new ReflectionTaskFactory();
    
    final Map<String, Responder> commands;

    final Map<Class<? extends Task>, Responder> tasks = new HashMap<Class<? extends Task>, Responder>();
    
    public CommandInterpreter(String artifactFile) {
        this.commands = new CommandTreeBuilder(artifactFile).getCommands();
        
        mapTasks(commands.values());
        
        for (Responder responder : tasks.values()) {
            responder.checkEndlessRecursion(tasks, new HashSet<Class<? extends Task>>());
        }
    }
    
    private void mapTasks(Collection<Responder> responders) {
        for (Responder responder : responders) {
            tasks.put(responder.getTaskClass(), responder);
            mapTasks(responder.getCommands());
        }
    }
    
    public void main(String...arguments) {
        Responder responder = commands.get(arguments[0]);
        if (responder == null) {
            System.out.println("Usage: " + commands.keySet());
            throw new RuntimeException("Usage: " + commands.keySet());
        }
        LinkedList<List<String>> contextualized = new LinkedList<List<String>>();
        LinkedList<Map<String, Object>> converted = new LinkedList<Map<String, Object>>();
        contextualized.addLast(new ArrayList<String>());
        converted.addLast(new HashMap<String, Object>());
        int i, stop;
        for (i = 1, stop = arguments.length; responder != null && i < stop; i++) {
            String argument = arguments[i];
            if (argument.startsWith("--")) {
                String[] pair = argument.substring(2).split("=", 2);
                String name = pair[0];
                if (name.indexOf(':') == -1) {
                    name = responder.getName() + ':' + name;
                }
                String[] qualified = name.split(":");
                if (qualified.length != 2) {
                    throw new GoException(0);
                }

                Class<? extends Task> parent = responder.getParentTaskClass();
                Responder consumer; 
                if (parent == null) {
                    consumer = commands.get(qualified[0]);
                } else {
                    consumer = tasks.get(parent).getCommand(qualified[0]);
                }
                
                if (consumer == null) {
                    throw new GoException(0);
                }
                
                Assignment assignment = null;

                String value = pair[1];
                // Check for a negated boolean flag.
                if (value.equals("")) {
                    // FIXME Ensure that there are no arguments beginning with no.
                    if (argument.startsWith("no-")) {
                        String negate = qualified[1].substring(3);
                        assignment = consumer.getAssignments().get(negate); 
                        if (assignment != null) {
                            if (objectify(assignment.getType()).equals(Boolean.class)) {
                                argument = negate;
                                value = "false";
                            } else {
                                assignment = null;
                            }
                        }
                    }
                }

                // If the value is not specified, but the type is boolean, then
                // the presence of the argument means true. 
                if (assignment == null) {
                    assignment = consumer.getAssignments().get(qualified[1]); 
                    if (assignment == null) {
                        throw new GoException(0);
                    }
                    if (value == null && objectify(assignment.getType()).equals(Boolean.class)) {
                        value = "true";
                    }
                }
                
                if (assignment.getType().isArray()) {
                    if (converted.getLast().containsKey(name)) {
                        List<Object> list = objectList((List<?>) converted.getLast().get(name));
                        list.add(assignment.convertValue(value));
                    } else {
                        List<Object> list = new ArrayList<Object>();
                        list.add(assignment.convertValue(value));
                        converted.getLast().put(name, list);
                    }
                } else {
                    converted.getLast().put(name, assignment.convertValue(value));
                }
                
                contextualized.getLast().add("--" + name + "=" + value);
            } else {
                responder = responder.getCommand(argument);
                if (responder == null) {
                    break;
                }
                contextualized.addLast(new ArrayList<String>());
                converted.addLast(new HashMap<String, Object>());
            }
        }
        Execution execution = new Execution(this, new ArrayList<Map<String,Object>>(converted), stringArrayArray(contextualized), remainingArguments(arguments, i));
        execution.execute(responder.getTaskClass());
    }
    
    private static String[] remainingArguments(String[] arguments, int start) {
        String[] remaining = new String[arguments.length - start];
        System.arraycopy(arguments, start, remaining, 0, arguments.length - start);
        return remaining;
    }

    private static String[][] stringArrayArray(List<List<String>> stringListList) {
        String[][] stringArrayArray = new String[stringListList.size()][];
        int index = 0;
        for (List<String> args : stringListList) {
            stringArrayArray[index++] = args.toArray(new String[args.size()]);
        }
        return stringArrayArray;
    }
}
