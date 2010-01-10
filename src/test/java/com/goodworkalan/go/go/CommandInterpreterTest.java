package com.goodworkalan.go.go;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

public class CommandInterpreterTest {
    @Test
    public void constructor() {
        new CommandInterpreter(Collections.<List<String>, Artifact>emptyMap(), null, new ErrorCatcher(), Collections.<File>emptyList()).execute("boot", "hello");
    }
}
