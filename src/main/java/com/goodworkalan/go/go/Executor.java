package com.goodworkalan.go.go;

import java.util.HashMap;
import java.util.Map;

public class Executor {
    final Map<CommandKey, Map<Class<? extends Output>, Output>> outputCache = new HashMap<CommandKey, Map<Class<? extends Output>,Output>>();
    
    final CommandPart part;
    
    public Executor(CommandPart part) {
        this.part = part;
    }

    public void execute(CommandPart part, InputOutput io) {
        Execution execution = new Execution(this, part);
        execution.execute(io, part.getTaskClass());
    }
    
    public <T extends Arguable> T getArguments(Class<T> arguableClass) {
        return new Execution(this, part).getArguments(arguableClass);
    }
}
