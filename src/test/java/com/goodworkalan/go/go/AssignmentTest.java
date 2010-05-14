package com.goodworkalan.go.go;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.goodworkalan.infuse.StringInfuser;
import com.goodworkalan.reflective.ReflectiveException;
import com.goodworkalan.reflective.ReflectiveFactory;
import com.goodworkalan.reflective.setter.MethodSetter;

public class AssignmentTest {
    @Test
    public void getType() throws ReflectiveException {
        Assignment assignment = new Assignment(new MethodSetter(new ReflectiveFactory().getMethod(Dubious.class, "setSomething", String.class)), StringInfuser.INSTNACE);
        assertEquals(assignment.setter.getType(), String.class);
    }
    
//    @Test
//    public void invocationTargetException() throws ReflectiveException {
//        final Method method = new ReflectiveFactory().getMethod(Dubious.class, "setSomething", String.class);
//        new GoExceptionCatcher(GoException.ASSIGNMENT_EXCEPTION_THROWN, new Runnable() {
//            public void run() {
//                Assignment assignment = new Assignment(new MethodSetter(method), StringInfuser.INSTNACE);
//                assignment.setValue(new Dubious(), "");
//            }
//        }).run();
//    }
//    
//    @Test
//    public void reflectionExcpetion() throws SecurityException {
//        for (final java.lang.reflect.Method method : Dubious.class.getDeclaredMethods()) {
//            if (method.getName().equals("setPrivate")) {
//                new GoExceptionCatcher(GoException.ASSIGNMENT_FAILED, new Runnable() {
//                    public void run() {
//                        Assignment assignment = new Assignment(new MethodSetter(new Method(method)), StringInfuser.INSTNACE);
//                        assignment.setValue(new Dubious(), "");
//                    }
//                }).run();
//            }
//        }
//    }
}
