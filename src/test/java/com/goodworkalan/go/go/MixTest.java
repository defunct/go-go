package com.goodworkalan.go.go;

import org.testng.annotations.Test;

public class MixTest {
    @Test
    public void test() {
        new CommandInterpreter("src/test/resources/go.go").main("mix", "javac");
    }
}
