package com.goodworkalan.go.go;

import java.io.File;
import java.util.Collections;

import org.testng.annotations.Test;

public class CommandInterpreterTest {
    @Test
    public void constructor() {
        new CommandInterpreter(null, new ErrorCatcher(), Collections.<File>emptyList()).execute("go", "hello");
    }
}
