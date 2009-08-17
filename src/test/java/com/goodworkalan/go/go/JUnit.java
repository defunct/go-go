package com.goodworkalan.go.go;

public class JUnit extends Task {
    public JUnit() {
    }

    @Override
    public void execute(CommandLine commandLine, int index) {
        new Compile().execute(commandLine.rename(index, "compile"), index);
    }
}
