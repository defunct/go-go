package com.goodworkalan.go.go;

import java.util.HashMap;
import java.util.Map;

public class Executor {
    final Map<CommandKey, Map<Class<? extends Output>, Output>> outputCache = new HashMap<CommandKey, Map<Class<? extends Output>,Output>>();

    public void execute(CommandPart part) {
        execute(new InputOutput(), part);
    }
    
    public void execute(InputOutput io, CommandPart part) {
        Execution execution = new Execution(this, io, part);
        execution.execute(part.getTaskClass());
    }
}
