package com.goodworkalan.go.go;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class Redirection extends InputOutput {
    public Redirection() {
        super(new ByteArrayInputStream(new byte[0]), new PrintStream(new ByteArrayOutputStream()), new PrintStream(new ByteArrayOutputStream()));
    }
}
