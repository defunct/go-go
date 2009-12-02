package com.goodworkalan.go.go;
import java.io.File;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ProgramQueueTest {
    @Test
    public void run() {
        assertEquals(new ProgramQueue().start(new Program(new File("."), "src/test/resources/blank.txt", "snap", "welcome", "--greeting=Hello, World!")), 0);
    }
}
