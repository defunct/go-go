package com.goodworkalan.go.go;

import org.testng.annotations.Test;

/**
 * Unit tests for the {@link Retry} class.
 *
 * @author Alan Gutierrez
 */
public class RetryTest {
    /** Test retry. */
    @Test
    public void retry() throws InterruptedException {
        Thread thread = new Thread() {
            public void run() {
                Retry.retry(new Retry.Procedure() {
                    int count;
                    public void retry() throws InterruptedException {
                        if (count == 0) {
                            count++;
                            wait();
                        }
                    }
                });
            }
        };
        thread.start();
        thread.interrupt();
        thread.join();
    }
}
