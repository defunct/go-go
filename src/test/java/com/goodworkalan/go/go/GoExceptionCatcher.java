package com.goodworkalan.go.go;
import static org.testng.Assert.*;
public class GoExceptionCatcher {
    private final int code;
    private final Runnable runnable;
    public GoExceptionCatcher(int code, Runnable runnable) {
        this.code = code;
        this.runnable = runnable;
    }
    
    public void run() {
        try {
            runnable.run();
        } catch(GoException e) {
            assertEquals(e.getCode(), code);
            if (Integer.toString(e.getCode()).equals(e.getMessage())) {
                fail("No message for error code: " + e.getCode());
            }
            System.out.println(e.getMessage());
            return;
        }
        fail("Expected exception not thrown.");
    }
}
