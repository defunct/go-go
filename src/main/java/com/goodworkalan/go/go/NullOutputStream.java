package com.goodworkalan.go.go;

import java.io.IOException;
import java.io.OutputStream;

// TODO Document.
public class NullOutputStream extends OutputStream {
    // TODO Document.
    public static OutputStream nullStream() {
        return new NullOutputStream();
    }
    
    // TODO Document.
    @Override
    public void write(int b) throws IOException {
    }
}
