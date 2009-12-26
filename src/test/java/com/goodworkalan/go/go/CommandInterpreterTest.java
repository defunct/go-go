package com.goodworkalan.go.go;

import java.io.File;
import java.util.Collections;

import org.testng.annotations.Test;

public class CommandInterpreterTest {
    @Test
    public void constructor() {
        new CommandInterpreter(new ErrorCatcher(), Collections.<File>emptyList()).execute(new File("."), "go", "hello");
    }
}
