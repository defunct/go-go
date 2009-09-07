package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoException.*;

import com.goodworkalan.reflective.ReflectiveException;
import com.goodworkalan.reflective.ReflectiveFactory;

public class ReflectionTaskFactory implements TaskFactory {
    private final ReflectiveFactory reflectiveFactory;

    ReflectionTaskFactory(ReflectiveFactory reflectiveFactory) {
        this.reflectiveFactory = reflectiveFactory;
    }

    public ReflectionTaskFactory() {
        this(new ReflectiveFactory());
    }

    public Task newTask(Class<? extends Task> taskClass) {
        try {
            return reflectiveFactory.getConstructor(taskClass).newInstance();
        } catch (ReflectiveException e) {
            throw new GoException(CANNOT_CREATE_TASK, e);
        }
    }
    
    public Arguable newArguable(Class<? extends Arguable> arguableClass) {
        try {
            return reflectiveFactory.getConstructor(arguableClass).newInstance();
        } catch (ReflectiveException e) {
            throw new GoException(0, e);
        }
    }
}
