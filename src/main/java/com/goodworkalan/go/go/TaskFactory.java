package com.goodworkalan.go.go;

public interface TaskFactory {
    public Task newTask(Class<? extends Task> taskClass);
}
