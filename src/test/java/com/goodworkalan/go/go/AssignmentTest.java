package com.goodworkalan.go.go;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.goodworkalan.reflective.Method;
import com.goodworkalan.reflective.ReflectiveException;
import com.goodworkalan.reflective.ReflectiveFactory;

public class AssignmentTest {
    @Test
    public void getType() throws ReflectiveException {
        Assignment assignment = new Assignment(Dubious.class, new ReflectiveFactory().getMethod(Dubious.class, "setSomething", String.class), new StringConverter());
        assertEquals(assignment.getType(), String.class);
    }
    
    @Test
    public void invocationTargetException() throws ReflectiveException {
        final Method method = new ReflectiveFactory().getMethod(Dubious.class, "setSomething", String.class);
        new GoExceptionCatcher(GoException.ASSIGNMENT_EXCEPTION_THROWN, new Runnable() {
            public void run() {
                Assignment assignment = new Assignment(Dubious.class, method, new StringConverter());
                assignment.setValue(new Dubious(), "");
            }
        }).run();
    }
    
    @Test
    public void reflectionExcpetion() throws SecurityException {
        for (final java.lang.reflect.Method method : Dubious.class.getDeclaredMethods()) {
            if (method.getName().equals("setPrivate")) {
                new GoExceptionCatcher(GoException.ASSIGNMENT_FAILED, new Runnable() {
                    public void run() {
                        Assignment assignment = new Assignment(Dubious.class, new Method(method), new StringConverter());
                        assignment.setValue(new Dubious(), "");
                    }
                }).run();
            }
        }
    }
}
