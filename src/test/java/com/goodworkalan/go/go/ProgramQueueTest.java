package com.goodworkalan.go.go;
import java.io.File;
import java.util.Collections;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ProgramQueueTest {
    @Test
    public void run() {
        assertEquals(new ProgramQueue(Collections.singletonList(new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository")), "go", "hello").start(), 0);
    }
}
