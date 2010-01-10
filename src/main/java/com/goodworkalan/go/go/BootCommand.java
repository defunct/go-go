package com.goodworkalan.go.go;

@Command
public class BootCommand implements Commandable {
    public void execute(Environment env) {
        System.out.println(env.part.getRemaining());
    }
}
