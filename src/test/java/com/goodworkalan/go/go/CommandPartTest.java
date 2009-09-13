package com.goodworkalan.go.go;

public class CommandPartTest {
    public void commands() {
        CommandInterpreter ci = new CommandInterpreter(null);
        CommandPart hello = ci.command("hello");
        hello.execute();
    }
}
