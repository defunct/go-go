package com.goodworkalan.go.go;

import static org.testng.Assert.*;
import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class AssignmentTest {
    @Test
    public void getType() throws SecurityException, NoSuchMethodException {
        Method method = Dubious.class.getMethod("setSomething", String.class);
        Assignment assignment = new Assignment(method, new StringConverter());
        assertEquals(assignment.getType(), String.class);
    }
    
    @Test
    public void invocationTargetException() throws SecurityException, NoSuchMethodException {
        final Method method = Dubious.class.getMethod("setSomething", String.class);
        new GoExceptionCatcher(GoException.ASSIGNMENT_EXCEPTION_THROWN, new Runnable() {
            public void run() {
                Assignment assignment = new Assignment(method, new StringConverter());
                assignment.setValue(new Dubious(), "");
            }
        }).run();
    }
    
    @Test
    public void reflectionExcpetion() throws SecurityException, NoSuchMethodException {
        for (final Method method : Dubious.class.getDeclaredMethods()) {
            if (method.getName().equals("setPrivate")) {
                new GoExceptionCatcher(GoException.ASSIGNMENT_FAILED, new Runnable() {
                    public void run() {
                        Assignment assignment = new Assignment(method, new StringConverter());
                        assignment.setValue(new Dubious(), "");
                    }
                }).run();
            }
        }
    }
}
