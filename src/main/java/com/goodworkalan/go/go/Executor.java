package com.goodworkalan.go.go;

import java.util.HashMap;
import java.util.Map;

public class Executor {
    final Map<CommandKey, Map<Class<? extends Output>, Output>> outputCache = new HashMap<CommandKey, Map<Class<? extends Output>,Output>>();

    public void execute(CommandPart commandPart) {
        Execution execution = new Execution(this, commandPart);
        execution.execute(commandPart.getTaskClass());
    }
}
