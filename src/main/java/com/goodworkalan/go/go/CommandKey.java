package com.goodworkalan.go.go;

import java.util.List;

public class CommandKey {
    private final Class<? extends Commandable> taskClass;
    
    private final List<String> arguments;
    
    private final List<String> remaining;
    
    public CommandKey(Class<? extends Commandable> taskClass, List<String> arguments, List<String> remaining) {
        this.taskClass = taskClass;
        this.arguments = arguments;
        this.remaining = remaining;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object instanceof CommandKey) {
            CommandKey commandKey = (CommandKey) object;
            return taskClass.equals(commandKey.taskClass)
                && arguments.equals(commandKey.arguments)
                && remaining.equals(commandKey.remaining);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 37 + taskClass.hashCode();
        hashCode = hashCode * 37 + arguments.hashCode();
        hashCode = hashCode * 37 + remaining.hashCode();
        return hashCode;
    }
}
