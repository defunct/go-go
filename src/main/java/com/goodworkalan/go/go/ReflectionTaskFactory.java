package com.goodworkalan.go.go;

public class ReflectionTaskFactory implements TaskFactory {
    public Task newTask(Class<? extends Task> taskClass) {
        try {
            return taskClass.newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new GoException(0, e);
        }
    }
}
