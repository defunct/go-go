package com.goodworkalan.go.go;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Make sense of the command line.
 * 
 * @author Alan Gutierrez
 */
public class CommandInterpreter {
    private final TaskFactory taskFactory = new ReflectionTaskFactory();
    private final Map<String, Responder> commands;
    
    public CommandInterpreter(String artifactFile) {
        this.commands = new CommandTreeBuilder(artifactFile).getCommands();
    }

    public void main(String...arguments) {
        Responder responder = commands.get(arguments[0]);
        if (responder == null) {
            throw new GoException(0);
        }
        LinkedList<List<String>> contextualized = new LinkedList<List<String>>();
        contextualized.add(new ArrayList<String>());
        Task task = taskFactory.newTask(responder.getTaskClass());
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
                if (qualified[0].equals(responder.getName())) {
                    responder.setArgument(task, qualified[1], pair[1]);
                }
                contextualized.getLast().add("--" + name);
            } else {
                responder = responder.getCommand(argument);
                if (responder != null) {
                    Task subTask = taskFactory.newTask(responder.getTaskClass());
                    responder.setParent(subTask, task);
                    task = subTask;
                    contextualized.addLast(new ArrayList<String>());
                }
            }
        }
        Environment env = new Environment(System.in, System.err, System.out, stringArrayArray(contextualized), remainingArguments(arguments, i));
        task.execute(env);
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
