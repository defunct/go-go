package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.Casts.objectify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CommandPart {
    private final CommandInterpreter commandInterpreter;
    
    private final CommandPart parent;
    
    private final Responder responder;
    
    private final List<String> arguments;
    
    private final List<Conversion> conversions;
    
    private final List<String> remaining;
    
    CommandPart(CommandInterpreter commandInterpreter, Responder responder, CommandPart parent) {
        this.commandInterpreter = commandInterpreter;
        this.parent = parent;
        this.responder = responder;
        this.arguments = new ArrayList<String>();
        this.conversions = new ArrayList<Conversion>();
        this.remaining = new ArrayList<String>();
    }
    
    CommandPart(CommandPart copy) {
        this.commandInterpreter = copy.commandInterpreter;
        this.parent = copy.parent;
        this.responder = copy.responder;
        this.arguments = new ArrayList<String>(copy.arguments);
        this.conversions = new ArrayList<Conversion>(copy.conversions);
        this.remaining = new ArrayList<String>(copy.remaining);
    }
    
    public CommandInterpreter getCommandInterpreter() {
        return commandInterpreter;
    }
    
    public int getDepth() {
        if (parent == null)  {
            return 0;
        }
        return parent.getDepth() + 1;
    }
    
    public CommandPart getRoot() {
        if (parent == null) {
            return this;
        }
        return parent.getRoot();
    }
    
    public CommandPart getParent() {
        return parent;
    }
    
    private void getCommandPath(List<CommandPart> path) {
        if (parent != null) {
            parent.getCommandPath(path);
        }
        path.add(this);
    }
    
    public List<CommandPart> getCommandPath() {
        List<CommandPart> path = new ArrayList<CommandPart>();
        getCommandPath(path);
        return Collections.unmodifiableList(path);
    }
    
    public List<Conversion> getConversions() {
        return Collections.unmodifiableList(conversions);
    }
    
    public List<String> getRemaining() {
        return Collections.unmodifiableList(remaining);
    }
    
    public List<String> getArguments() {
        return Collections.unmodifiableList(arguments);
    }
    
    public Class<? extends Task> getTaskClass() {
        return responder.getTaskClass();
    }
    
    public Map<String, Class<?>> getArgumentTypes() {
        return responder.getArguments();
    }
    
    public CommandPart command(String name) {
        Responder child = responder.getCommand(name);
        if (child == null) {
            throw new GoException(0);
        }
        return new CommandPart(commandInterpreter, child, this);
    }
    
    public CommandPart command(Class<? extends Task> taskClass) {
        Responder child = commandInterpreter.responders.get(taskClass);
        if (!child.getParentTaskClass().equals(responder.getTaskClass())) {
            throw new GoException(0);
        }
        return new CommandPart(commandInterpreter, child, this);
    }
    
    public CommandPart command(Class<? extends Task> taskClass, List<String> arguments) {
        return command(taskClass).arguments(arguments);
    }
    
    public CommandPart arguments(List<String> arguments) {
        CommandPart part = this;
        for (String argument : arguments) {
            part = part.argument(argument);
        }
        return this;
    }
    
    public CommandPart argument(String argument) {
        if (!argument.startsWith("--")) {
            throw new GoException(0);
        }
        String[] pair = argument.substring(2).split("=", 2);
        String name = pair[0];
        String value = pair.length == 1 ? null : pair[1]; 
        return argument(name, value);
    }
    
    public CommandPart extend(String...arguments) {
        return extend(arguments, 0);
    }
    
    public CommandPart extend(List<String> arguments) {
        return extend(arguments.toArray(new String[arguments.size()]));
    }
    
    CommandPart extend(String[] arguments, int offset) {
        CommandPart part = this;
        boolean remaining = false;
        for (int i = offset, stop = arguments.length; i < stop; i++) {
            String argument = arguments[i];
            if (remaining) {
                part.remaining.add(argument);
            } else {
                if (argument.equals("--")) {
                    remaining = true;
                } else if (argument.startsWith("--")) {
                    String[] pair = argument.substring(2).split("=", 2);
                    String name = pair[0];
                    String value = pair.length == 1 ? null : pair[1]; 
                    part = part.argument(name, value);
                } else {
                    Responder child = part.responder.getCommand(argument);
                    if (child == null) {
                        part.remaining.add(argument);
                        remaining = true;
                    } else {
                        part = new CommandPart(commandInterpreter, child, part);
                    }
                }
            }
        }
        return part;
    }

    public CommandPart remaining(String...remaining) {
        CommandPart part = new CommandPart(this);
        for (String argument : remaining) {
            part.remaining.add(argument);
        }
        return part;
    }
    
    public CommandPart remaining(List<String> remaining) {
        CommandPart part = new CommandPart(this);
        part.remaining.addAll(remaining);
        return part;
    }
    
    public CommandPart task(Class<? extends Task> taskClass) {
        if (taskClass.equals(responder.getTaskClass())) {
            return this;
        }
        Responder responder = commandInterpreter.responders.get(taskClass);
        if (responder == null) {
            throw new GoException(0);
        }
        CommandPart part = getRoot();
        List<CommandPart> parts = getCommandPath();
        List<Class<? extends Task>> tasks = getTaskClassPath(responder);
        int i = 0, stop = Math.min(tasks.size(), parts.size());
        for (; i < stop && tasks.get(i).equals(parts.get(i).getTaskClass()); i++);
        for (; i < stop; i++) {
            part = part.command(tasks.get(i), part.getArguments());
        }
        for (; i < tasks.size(); i++) {
            part = part.command(tasks.get(i));
        }
        if (parts.size() == tasks.size()) {
            part = part.remaining(getRemaining());
        }
        return part;
    }

    public CommandPart argument(String name, String value) {
        if (name.indexOf(':') == -1) {
            name = responder.getName() + ':' + name;
        }
        String[] qualified = name.split(":");
        if (qualified.length != 2) {
            throw new GoException(0);
        }

        Class<? extends Task> parent = responder.getParentTaskClass();
        Responder responder; 
        if (parent == null) {
            responder = commandInterpreter.commands.get(qualified[0]);
        } else {
            responder = commandInterpreter.responders.get(parent).getCommand(qualified[0]);
        }
        
        if (responder == null) {
            throw new GoException(0);
        }

        Assignment assignment = null;

        // Check for a negated boolean flag.
        if (value == null) {
            // FIXME Ensure that there are no arguments beginning with no.
            if (qualified[1].startsWith("no-")) {
                String negate = qualified[1].substring(3);
                assignment = responder.getAssignments().get(negate); 
                if (assignment != null) {
                    if (objectify(assignment.getType()).equals(Boolean.class)) {
                        name = responder.getName() + ':' + negate;
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
            assignment = responder.getAssignments().get(qualified[1]); 
            if (assignment == null) {
                throw new GoException(0);
            }
            if (value == null && objectify(assignment.getType()).equals(Boolean.class)) {
                value = "true";
            }
        }
        
        CommandPart copy = new CommandPart(this);
        
        copy.conversions.add(new Conversion(name, assignment.convertValue(value)));
        copy.arguments.add("--" + qualified[0] + ':' + qualified[1] + '=' + value);

        return copy;
    }
    
    
    private void getTaskClassPath(List<Class<? extends Task>> path, Responder responder) {
        if (responder.getParentTaskClass() != null) {
            getTaskClassPath(path, commandInterpreter.responders.get(responder.getParentTaskClass()));
        }
        path.add(responder.getTaskClass());
    }
    
    private List<Class<? extends Task>> getTaskClassPath(Responder responder) {
        List<Class<? extends Task>> path = new ArrayList<Class<? extends Task>>();
        getTaskClassPath(path, responder);
        return Collections.unmodifiableList(path);
    }


    public void execute() {
        new Executor().execute(this);
    }

    public CommandKey getKey_() {
        List<String> filtered = new ArrayList<String>();
        String prefix = "--" + responder.getName() + ':';
        for (String argument : arguments) {
            if (argument.startsWith(prefix)) {
                filtered.add(argument);
            }
        }
        return new CommandKey(responder.getTaskClass(), filtered, remaining);
    }
}
