package com.goodworkalan.go.go;

import org.testng.annotations.Test;

import com.goodworkalan.go.go.CommandInterpreter;

public class CommandInterpreterTest {
    @Test
    public void constructor() {
        new CommandInterpreter().main("snap", "hello", "--greeting=Hello, World!");
    }
}
