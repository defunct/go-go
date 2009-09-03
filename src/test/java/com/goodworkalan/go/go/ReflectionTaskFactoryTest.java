package com.goodworkalan.go.go;

import org.testng.annotations.Test;

import com.goodworkalan.reflective.Constructor;
import com.goodworkalan.reflective.ReflectiveException;
import com.goodworkalan.reflective.ReflectiveFactory;

public class ReflectionTaskFactoryTest {
    @Test
    public void constructor() {
        new ReflectionTaskFactory();
    }
    
    @Test
    public void exception() {
        new GoExceptionCatcher(GoException.CANNOT_CREATE_TASK, new Runnable() {
            public void run() {
                new ReflectionTaskFactory(new ReflectiveFactory() {
                    public <T> Constructor<T> getConstructor(Class<T> type, java.lang.Class<?>...initargs) throws com.goodworkalan.reflective.ReflectiveException {
                        throw new ReflectiveException(ReflectiveException.SECURITY, new SecurityException("Go away."));
                    }
                }).newTask(Welcome.class);
            }
        }).run();
    }
    
    @Test
    public void newTask() {
        new ReflectionTaskFactory().newTask(Welcome.class);
    }
}
