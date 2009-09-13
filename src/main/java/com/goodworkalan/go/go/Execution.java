package com.goodworkalan.go.go;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Execution {
    private final Executor executor;
    
    private final Map<Class<? extends Task>, Map<Class<? extends Output>, Output>> outputs = new HashMap<Class<? extends Task>, Map<Class<? extends Output>,Output>>();
    
    private final CommandPart commandPart;
    
    public Execution(Executor executor, CommandPart commandPart) {
        this.commandPart = commandPart;
        this.executor = executor;
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
        CommandInterpreter ci = commandPart.getCommandInterpreter();
        Responder responder = ci.responders.get(taskClass);
        if (responder == null) {
            throw new GoException(0);
        }
        CommandPart part = commandPart.task(taskClass);
        CommandKey key = part.getKey_();
        if (!executor.outputCache.containsKey(key)) {
            List<CommandPart> parts = part.getCommandPath();
            Task task = ci.taskFactory.newTask(taskClass);
            for (Class<? extends Arguable> argument : responder.getArguables()) {
                Responder container = ci.responders.get(taskClass(argument.getDeclaringClass()));
                // FIXME Assert that arguable is a static class.
                Arguable arguable = ci.taskFactory.newArguable(argument);
                int index = getDepth(container);
                if (index < parts.size()) {
                    for (Conversion conversion : parts.get(index).getConversions()) {
                        String[] pair = conversion.getName().split(":");
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
                Responder container = ci.responders.get(taskClass(input.getDeclaringClass()));
                execute(container.getTaskClass());
                responder.setInput(task, input, outputs.get(container.getTaskClass()).get(input));
            }
            int index = getDepth(responder);
            if (index < parts.size()) {
                for (Conversion conversion : parts.get(index).getConversions()) {
                    String[] pair = conversion.getName().split(":");
                    if (pair[0].equals(responder.getName())) {
                        Assignment assignment = responder.getAssignments().get(pair[1]);
                        if (assignment.getDeclaringClass().equals(taskClass)) {
                            assignment.setValue(task, conversion.getValue());
                        }
                    }
                }
            }
            Environment env = new Environment(System.in, System.err, System.out, part, executor);
            task.execute(env);
            executor.outputCache.put(key, new HashMap<Class<? extends Output>, Output>());
            for (Class<? extends Output> output : responder.getOutputs()) {
                executor.outputCache.get(key).put(output, responder.getOutput(task, output));
            }
        }
        outputs.put(taskClass, new HashMap<Class<? extends Output>, Output>(executor.outputCache.get(key)));
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
        return getDepth(commandPart.getCommandInterpreter().responders.get(parent)) + 1;
    }
}
