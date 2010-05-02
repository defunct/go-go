package com.goodworkalan.go.go;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Retry {
    public interface Procedure {        
        public abstract void retry() throws InterruptedException;
    }

    public interface Function<T> {        
        public abstract T retry() throws InterruptedException;
    }
 
    public static void retry(Procedure retry) {
        for (;;) {
            try {
                retry.retry();
                break;
            } catch (InterruptedException e) {
                continue;
            }
        }
    }
    
    public static <T> T retry(Function<T> retry) {
        for (;;) {
            try {
                return retry.retry();
            } catch (InterruptedException e) {
                continue;
            }
        }
    }
    
    public static <T> T retry(FutureTask<T> future) throws ExecutionException {
        for (;;) {
            try {
                return future.get();
            } catch (InterruptedException e) {
                continue;
            }
        }
    }
}
