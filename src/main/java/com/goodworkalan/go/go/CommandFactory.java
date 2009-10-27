package com.goodworkalan.go.go;

public interface CommandFactory {
    public Commandable newTask(Class<? extends Commandable> taskClass);
    
    public Arguable newArguable(Class<? extends Arguable> arguableClass);
}
