package com.goodworkalan.go.go;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class ProgramQueue {
    private final LinkedList<FutureTask<Integer>> programs = new LinkedList<FutureTask<Integer>>();
    
    private final Object monitor = new Object();
    
    private int processCount;
    
    public int fork(final Program program) {
        FutureTask<Integer> future = null;
        synchronized (monitor) {
            future = addProgram(program);
            monitor.notify();
        }
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new GoException(0, e);
        } catch (ExecutionException e) {
            throw new GoException(0, e);
        }
    }
    
    private FutureTask<Integer> addProgram(final Program program) {
        FutureTask<Integer> future = new FutureTask<Integer>(new Callable<Integer>() {
            public Integer call() {
                // Exit codes really don't belong here. Put them in GoError.
                // Then return an exception or terminal status.
                int code = 1;
                try {
                    code = program.run(ProgramQueue.this);
                } catch (Throwable e) { 
                    e.printStackTrace();
                }
                synchronized (monitor) {
                    processCount--;
                    monitor.notify();
                }
                return code;
            }
        });
        programs.add(future);
        return future;
    }

    int start(Program program) {
        FutureTask<Integer> future = addProgram(program);
        loop();
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new GoException(0, e);
        } catch (ExecutionException e) {
            throw new GoException(0, e);
        }
    }

    void loop() {
        synchronized (monitor) {
            while (processCount > 0 || !programs.isEmpty()) {
                if (!programs.isEmpty()) {
                    processCount++;
                    new Thread(programs.removeFirst()).start();
                }
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    throw new GoException(0, e);
                }
            }
        }
    }
}
