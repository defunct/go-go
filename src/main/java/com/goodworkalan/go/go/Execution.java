package com.goodworkalan.go.go;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Execution {
    private final CommandInterpreter commandInterpreter;
    
    private final List<Map<String, Object>> conversions;
    
    private final Map<Class<? extends Task>, Map<Class<? extends Output>, Output>> outputs = new HashMap<Class<? extends Task>, Map<Class<? extends Output>,Output>>();
    
    private final String[][] arguments;
    
    private final String[] remaining;
    
    Execution(CommandInterpreter commandInterpreter,  List<Map<String, Object>> converted, String[][] arguments, String[] remaining) {
        this.commandInterpreter = commandInterpreter;
        this.arguments = arguments;
        this.remaining = remaining;
        this.conversions = converted;
    }

    /**
     * Execute the given task class.
     * 
     * @param taskClass
     *            A task to execute.
     */
    public void execute(Class<? extends Task> taskClass) {
        // Short circuit this method if the given task has already been run.
        if (outputs.containsKey(taskClass)) {
            return;
        }
        Task task = commandInterpreter.taskFactory.newTask(taskClass);
        Responder responder = commandInterpreter.responders.get(taskClass);
        if (responder == null) {
            throw new GoException(0);
        }
        for (Class<? extends Arguable> argument : responder.getArguables()) {
            Responder container = commandInterpreter.responders.get(taskClass(argument.getDeclaringClass()));
            // FIXME Assert that arguable is a static class.
            Arguable arguable = commandInterpreter.taskFactory.newArguable(argument);
            int index = getDepth(container);
            if (index < conversions.size()) {
                for (Map.Entry<String, Object> conversion : conversions.get(index).entrySet()) {
                    String[] pair = conversion.getKey().split(":");
                    if (pair[0].equals(container.getName())) {
                        Assignment assignment = container.getAssignments().get(pair[1]);
                        if (assignment.getDeclaringClass().equals(argument)) {
                            assignment.setValue(arguable, conversion.getValue());
                        }
                    }
                }
            }
            responder.setArguable(task, argument, arguable);
        }
        for (Class<? extends Output> input : responder.getInputs()) {
            Responder container = commandInterpreter.responders.get(taskClass(input.getDeclaringClass()));
            execute(container.getTaskClass());
            responder.setInput(task, input, outputs.get(container.getTaskClass()).get(input));
        }
        int index = getDepth(responder);
        if (index < conversions.size()) {
            for (Map.Entry<String, Object> conversion : conversions.get(index).entrySet()) {
                String[] pair = conversion.getKey().split(":");
                if (pair[0].equals(responder.getName())) {
                    Assignment assignment = responder.getAssignments().get(pair[1]);
                    if (assignment.getDeclaringClass().equals(taskClass)) {
                        assignment.setValue(task, conversion.getValue());
                    }
                }
            }
        }
        Environment env = new Environment(commandInterpreter, System.in, System.err, System.out, arguments, remaining);
        task.execute(env);
        outputs.put(taskClass, new HashMap<Class<? extends Output>, Output>());
        for (Class<? extends Output> output : responder.getOutputs()) {
            outputs.get(taskClass).put(output, responder.getOutput(task, output));
        }
    }
    
    @SuppressWarnings("unchecked")
    private Class<? extends Task> taskClass(Class taskClass) {
        return taskClass;
    }
    
    /**
     * Get the depth of the given task class in the task hierarchy.
     * 
     * @param taskClass The task class.
     * @return The depth of the task class in the task hierarchy.
     */
    private int getDepth(Responder responder) {
        Class<? extends Task> parent = responder.getParentTaskClass();
        if (parent == null) {
            return 0;
        }
        return getDepth(commandInterpreter.responders.get(parent)) + 1;
    }
    
    public <T extends Arguable> T getArguable(Class<T> arguable) {
        return null;
    }
}
